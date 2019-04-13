package com.gaven.exercise.nio.demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CodeUtil {



    public static ByteBuffer read(SocketChannel clientSocketChannel) {

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //从socket缓冲区 复制到  应用程序缓冲区
        try {
            int count = clientSocketChannel.read(byteBuffer);

            if (count == -1) {
                return null;
            }

            return byteBuffer;

        } catch (IOException e) {
           throw new  RuntimeException(e);
        }

    }

    public static String newString(ByteBuffer buffer) {

        buffer.flip();

        byte[] bytes = new byte[buffer.remaining()];
        //复制到一个字节数组
        System.arraycopy(buffer,buffer.position(),bytes,0,buffer.remaining());

        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(SocketChannel clientSocketChannel, String content) {

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            buffer.put(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        buffer.flip();

        //将程序缓冲区类容 复制到 socket缓冲区
        try {
            clientSocketChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
