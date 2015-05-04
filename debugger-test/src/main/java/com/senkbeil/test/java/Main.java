package com.senkbeil.test.java;

public class Main {
    public static void main(String[] args)
            throws java.lang.InterruptedException
    {
        System.out.println("Hello, world!");
        for (String arg : args) {
            System.out.println("Argument: " + arg);
        }
        while (true) { Thread.sleep(1); }
    }
}
