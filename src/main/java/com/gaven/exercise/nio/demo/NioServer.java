package com.gaven.exercise.nio.demo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioServer {

    private InetSocketAddress inetSocketAddress;

    private Selector selector;

    private ServerSocketChannel ssc;

    public NioServer(int port){

        try {
            //打开服务端通道
            ssc = ServerSocketChannel.open();
            //设置非阻塞
            ssc.configureBlocking(false);

            //设置本地端口地址
            inetSocketAddress = new InetSocketAddress(port);

            //绑定监听
            ssc.bind(inetSocketAddress);

            //创建选择器
            selector = Selector.open();

            //服务端通道注册到到选择器，并设置对连接事件感兴趣
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server start Address is :" + inetSocketAddress);

            handelKeys();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server start fail !!");
        }


    }

    /**
     * 获取选择所有就绪事件
     */
    private void handelKeys() {


        try{

            while(true){
                //获取就绪的SelectionKey（channel & event） 阻塞模式
               int n =  selector.select(30 * 1000);

               if(n == 0){
                   continue;
               }

                System.out.println("选择 Channel 数量：" + n);

               //获取就绪的SelectionKeys
               Set<SelectionKey> selectionKeys = selector.selectedKeys();

               //遍历
               Iterator<SelectionKey> it = selectionKeys.iterator();
               while(it.hasNext()){

                   SelectionKey key = it.next();
                   //防止重复选择通道
                   it.remove();

                   if (!key.isValid()) { // 忽略无效的 SelectionKey
                       continue;
                   }

                   if(key.isAcceptable()){
                       handelKey(key);
                   }

               }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        }




    }

    /**
     * 委派到特定的处理handle
     * @param key
     */
    private void handelKey(SelectionKey key) throws ClosedChannelException {

        //链接就绪
        if(key.isAcceptable()){
            handelAcceptable(key);
        }

        //读就绪
        if(key.isReadable()){
            handleReadable(key);
        }

        //写就绪

        if(key.isWritable()){
            handelWritable(key);
        }
        
    }

    /**
     * 写处理
     * @param key
     */
    private void handelWritable(SelectionKey key) throws ClosedChannelException {

        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();

        List<String> responseQueue = (List<String>) key.attachment();

        //遍历响应队列
        for (String content : responseQueue) {
            // 打印数据
            System.out.println("写入数据：" + content);
            // 返回
            CodeUtil.write(clientSocketChannel, content);
        }

        responseQueue.clear();

        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector,SelectionKey.OP_READ,responseQueue);

    }

    /**
     *  读处理
     */
    private void handleReadable(SelectionKey key) throws ClosedChannelException {

        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();



        //读取数据
        ByteBuffer  readBuffer = CodeUtil.read(clientSocketChannel);
        // 处理连接已经断开的情况
        if(readBuffer ==null){
            System.out.println("断开 Channel");
            clientSocketChannel.register(selector, 0);
            return;
        }


        //打印数据
        if(readBuffer.position()>0){
           String content =  CodeUtil.newString(readBuffer);
            System.out.println("读取数据：" + content);

            // 添加到响应队列
            List<String> responseQueue = (List<String>) key.attachment();
            responseQueue.add("响应：" + content);

            // 注册 Client Socket Channel 到 Selector
            clientSocketChannel.register(selector,SelectionKey.OP_WRITE,responseQueue);
        }


    }

    /**
     * 连接处理
     */
    private void handelAcceptable(SelectionKey key) {

        try {
            // 接受 Client Socket Channel
            SocketChannel clientSocketChannel = ssc.accept();

            //设置非阻塞
            clientSocketChannel.configureBlocking(false);

            System.out.println("Client  accept success ");

            //客户端普通通道注册select 并对读感兴趣
            clientSocketChannel.register(selector,SelectionKey.OP_READ,new ArrayList<String>());

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Client  accept error !!!");
        }
    }

    public static void main(String[] args) {
        NioServer nioServer =  new NioServer(8888);
    }
}
