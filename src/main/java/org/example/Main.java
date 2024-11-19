package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (ExampleRunnable runnable = new ExampleRunnable()) {
            runnable.run();
        } catch (IOException e) {
            logger.error("Cannot run", e);
        }
    }

}