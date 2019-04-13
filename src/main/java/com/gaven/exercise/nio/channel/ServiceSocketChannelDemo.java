package com.gaven.exercise.nio.channel;

import com.gaven.exercise.nio.buffer.Buffers;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class ServiceSocketChannelDemo {

    public static class  TCPServer implements  Runnable{

        /*客户端线程名*/
        private String name;
        private Random rnd = new Random();

        //服务器地址
        private InetSocketAddress inetSocketAddress;

        public TCPServer(int port){
            this.inetSocketAddress = new InetSocketAddress(port);
        }

        @Override
        public void run() {

            Charset charset = Charset.forName("UTF-8");

            Selector selector = null;

            ServerSocketChannel ssc = null;

            try{

                //创建选择器
                selector = Selector.open();

                //创建服务器端通道
                ssc = ServerSocketChannel.open();

                /*设置监听服务器的端口，设置最大连接缓冲数为100*/
                ssc.bind(inetSocketAddress,100);

                //设置非阻塞
                ssc.configureBlocking(false);

                /*服务器通道只能对tcp链接事件感兴趣*/
                ssc.register(selector, SelectionKey.OP_ACCEPT);



            }catch (Exception e){
                e.printStackTrace();
                System.out.println("server start fail !!");

                return;
            }


            System.out.println("server start with address : " + inetSocketAddress);

            // 开始循环查看是否有可就绪事件


            try {
                //线程中断时推出
                while(!Thread.currentThread().isInterrupted()){


                    if( selector.select() == 0) continue;

                    Set<SelectionKey> selectionKeys =  selector.selectedKeys();
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    SelectionKey key = null;
                    while(it.hasNext()){
                        key =  it.next();
                        /*防止下次select方法返回已处理过的通道*/
                        it.remove();

                        //z在这个try里报错。是客户端出现问题，服务器正常
                        try{
                            //ssc通道只能对链接事件感兴趣 ,当可以连接时
                            if(key.isAcceptable()){
                                 /*accept方法会返回一个普通通道，
                                     每个通道在内核中都对应一个socket缓冲区*/
                               SocketChannel socketChannel = ssc.accept();

                               socketChannel.configureBlocking(false);

                                /*向选择器注册这个通道和普通通道感兴趣的事件，同时提供这个新通道相关的缓冲区*/
                                int interestSet = SelectionKey.OP_READ;
                                socketChannel.register(selector,interestSet,new Buffers(256,256));
                            }

                            /*（普通）通道感兴趣读事件且有数据可读*/
                            if (key.isReadable()){

                                //获取普通通道
                                SocketChannel socketChannel = (SocketChannel) key.channel();

                                //获取附件
                                Buffers buffers = (Buffers) key.attachment();

                                //获取buffer
                                ByteBuffer readBuffer = buffers.getReadBuffer();
                                ByteBuffer writeBuffer = buffers.gerWriteBuffer();

                                /*从底层socket读缓冲区中读入数据*/
                                socketChannel.read(readBuffer);

                                //调整水位limit 到达内容处，使buffer可读可写
                                readBuffer.flip();

                                /*解码显示，客户端发送来的信息*/
                                CharBuffer charBuffer = charset.decode(readBuffer);
                                System.out.println(charBuffer.array());

                                //调整水位limit 到达capacity最大值处
                                readBuffer.rewind();

                                /*准备好向客户端发送的信息*/
                                /*先写入"echo:"，再写入收到的信息*/
                                writeBuffer.put("echo from service:".getBytes("UTF-8"));
                                writeBuffer.put(readBuffer);

                                readBuffer.clear();

                                //向select注册 感兴趣写事件
                                //socketChannel.register(selector,SelectionKey.OP_WRITE,buffers);

                                //如果注册了，直接添加，要不然会将之前 读事件覆盖 , 相当springmvc 保存映射关系的handler
                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

                            }

                            /*通道感兴趣写事件且底层缓冲区有空闲*/
                            if(key.isWritable()){
                                //获取普通通道
                                SocketChannel socketChannel = (SocketChannel) key.channel();

                                //获取附件
                                Buffers buffers = (Buffers) key.attachment();

                                //获取buffer
                                ByteBuffer writeBuffer = buffers.gerWriteBuffer();
                                writeBuffer.flip();

                                int len =0;
                                while(writeBuffer.hasRemaining()){
                                    len = socketChannel.write(writeBuffer);

                                    /*说明底层的socket写缓冲已满*/
                                    if(len == 0){
                                        break;
                                    }
                                }

                                //将position写入位置设置到remaing剩余存储空间  将水位limit 返回capecity，
                                writeBuffer.compact();

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            /*若客户端连接出现异常，从Seletcor中移除这个key*/
                            key.cancel();
                            key.channel().close();
                            System.out.println("Client Error");
                        }
                    }


                    Thread.sleep(rnd.nextInt(500));

                }
            }catch (Exception e){
                e.printStackTrace();
            }




        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new TCPServer(8080));
        thread.start();
        Thread.sleep(100000);
        /*结束服务器线程*/
        thread.interrupt();
    }
}
