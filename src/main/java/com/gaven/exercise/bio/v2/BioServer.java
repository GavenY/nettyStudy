package com.gaven.exercise.bio.v2;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BioServer {

    private static  int DEFUALT_PORT = 8888;

    private static ServerSocket serverSocket;

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public static void start() throws  IOException{
        start(DEFUALT_PORT);
    }

    private synchronized static void start(int port) throws IOException {

        if(serverSocket != null) return;

        try{
            serverSocket = new ServerSocket(port);

            while(true){
                Socket socket = serverSocket.accept();
                executor.execute(new MessageServerHandler(socket));
            }

        }finally {

        }





    }


}
