package com.senkbeil.test.java;

/**
 * Provides an example of a main entrypoint using Java's main method.
 *
 * @note Should have a class name of com.senkbeil.test.java.Main
 * @note Should have breakpoint lines on 13, 14, 15, and 19
 */
public class Main {
    public static void main(String[] args)
            throws java.lang.InterruptedException
    {
        System.out.println("Hello, world!");
        for (String arg : args) {
            System.out.println("Argument: " + arg);
        }

        // Needed for tests to examine JVM without needing to set breakpoints
        while (true) { Thread.sleep(1); }
    }
}
