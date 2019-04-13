package com.gaven.exercise.bio.v1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO服务端源码
 */
@Slf4j
public class BIOServer {

    //默认端口
    private static  int DEFAULT_PORT =  7777;

    //单例的ServerSocket
    private static ServerSocket serverSocket;

    //根据传入参数设置监听端口，如果没有参数调用以下方法并使用默认值
    public static void start() throws  IOException{
        //调用默认值
        start(DEFAULT_PORT);
    }

    //这个方法不会被大量并发访问，不太需要考虑效率，直接进行方法同步就行了
    private synchronized static void start(int port) throws IOException {
        if(serverSocket != null) return;

        try{
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动，端口号:" + port);

            //通过无线循环监听客户端连接
            //如果没有客户端接入，将阻塞在accept操作上。
            while (true) {
                Socket socket = serverSocket.accept();
                Thread.sleep(10000);
                new Thread(new ServerHandler(socket)).start();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //一些必要的清理工作
            if (serverSocket != null) {
                System.out.println("服务端已关闭。");
                serverSocket.close();
                serverSocket = null;
            }
        }

    }
}
