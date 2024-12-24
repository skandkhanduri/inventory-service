package com.secor.inventoryservice.config;

import java.util.Random;

public class RequestIdGenerator {
    public static String generateRequestId() {
        Random random = new Random();
        return "REQ-" + random.nextInt(1000000);
    }
}