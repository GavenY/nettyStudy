package com.gaven.exercise.netty.v2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class ClientInitializer extends ChannelInitializer<Channel> {

    private static final StringDecoder DECODER = new StringDecoder(CharsetUtil.UTF_8);
    private static final StringEncoder ENCODER = new StringEncoder(CharsetUtil.UTF_8);
    private static final ClientHandler CLIENT_HANDLER = new ClientHandler();


    @Override
    protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
      /*  pipeline.addLast(ENCODER);
        pipeline.addLast(DECODER);*/

        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("encode", new StringEncoder(CharsetUtil.UTF_8));//编码byteBuffer后才能发送
        pipeline.addLast("decode", new StringDecoder(CharsetUtil.UTF_8));//解码后才能 翻译byteBuffer
        pipeline.addLast(CLIENT_HANDLER);
    }
}
