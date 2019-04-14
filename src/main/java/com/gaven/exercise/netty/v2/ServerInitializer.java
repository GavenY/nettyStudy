package com.gaven.exercise.netty.v2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import io.netty.util.CharsetUtil;

public class ServerInitializer extends ChannelInitializer<Channel> {

    private static final StringDecoder DECODER = new StringDecoder(CharsetUtil.UTF_8);
    private static final StringEncoder ENCODER = new StringEncoder(CharsetUtil.UTF_8);
    private static final ServerHandler SERVER_HANDLER = new ServerHandler();






    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // 添加帧限定符来防⽌止粘包现象
    /*    pipeline.addLast(new DelimiterBasedFrameDecoder(8192,
                Delimiters.lineDelimiter()));
        // 解码和编码，应和客户端⼀一致
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);*/

        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));   //解码后才能 翻译byteBuffer
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));  //编码byteBuffer后才能发送
        // 业务逻辑实现类
        pipeline.addLast(SERVER_HANDLER);
    }
}
