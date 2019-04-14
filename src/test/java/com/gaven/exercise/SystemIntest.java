package com.gaven.exercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemIntest {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new
                InputStreamReader(System.in));
        String msg;
        while ((msg=in.readLine())!=null)
            System.out.println("Control receive msg:" + msg);
    }
}
