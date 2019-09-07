package com.hw.springboot.mock;

import com.hw.springboot.clazz.AbstractService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class RandomService extends AbstractService<String> {

    public RandomService(String name, long delay) {
        super(name);
        this.delay = delay;
    }

    private long delay;

    @Override
    public CompletableFuture<String> task() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("{} - calling doTasking", getName());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        });
    }
}
