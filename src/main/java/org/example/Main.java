package org.example;

import org.example.controller.MasterController;

public class Main {
    public static void main(String[] args) throws Exception {
        MasterController.getInstance().run();
    }
}