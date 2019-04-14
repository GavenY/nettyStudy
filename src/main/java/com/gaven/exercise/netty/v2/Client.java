package com.gaven.exercise.netty.v2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());
            Channel ch = b.connect("127.0.0.1",8888).sync().channel();

          /*  ChannelFuture channelFuture = ch.writeAndFlush("Hello World");

            //当触发关闭事件时才调用
            channelFuture.channel().closeFuture().sync();*/

            ChannelFuture lastWriteFuture = null;

            BufferedReader in = new BufferedReader(new
                    InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();

                System.out.println(line);
                if (line == null) {
                    break;
                }
                // Sends the received line to the server.
                lastWriteFuture = ch.writeAndFlush(line + "/r/n");
                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }
            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }

}
