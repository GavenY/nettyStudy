package com.gaven.exercise.netty.v1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyServer {

    private static final String IP = "127.0.0.1";
    private static final int port = 6666;


    //bossGroup NioEventLoop 的数量
    private static final int BIZGROUPSIZE =  Runtime.getRuntime().availableProcessors() * 2;

    private static final int BIZTHREADSIZE = 100;

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    private static final EventLoopGroup workGroup = new NioEventLoopGroup(BIZTHREADSIZE);

    public static  void  start() throws InterruptedException {
        ServerBootstrap serverBootstrap  =initServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap.bind(IP,port).sync();

        channelFuture.channel().closeFuture().sync();
        System.out.println("server start");
    };

    private static ServerBootstrap initServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(bossGroup,workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));   //解码后才能 翻译byteBuffer
                        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));  //编码byteBuffer后才能发送
                        pipeline.addLast(new TcpServerHandler());
                    }
                });

        return serverBootstrap;
    }

    protected static void shutdown(){
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("启动Server...");
        NettyServer.start();
    }
}
