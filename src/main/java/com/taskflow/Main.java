package com.taskflow;

import com.taskflow.util.Constants;

/**
 * TaskFlow application entry point.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        System.out.println(Constants.BANNER);
        System.out.println("Version: " + Constants.VERSION);
    }
}
