package com.hw.springboot.clazz;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * observable pattern
 */
@Slf4j
public abstract class AbstractService<T> {

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public AbstractService(String name) {
        this.name = name;
    }

    private StatusEnum status = StatusEnum.READY;

    public String getName() {
        return name;
    }

    private String name;

    public Integer priority = 0;

    public List<AbstractService> getObservers() {
        return observers;
    }

    private List<AbstractService> observers = new ArrayList<>();

    public Map<AbstractService, StatusEnum> getBlockedBy() {
        return blockedBy;
    }

    private void increasePriority(AbstractService service) {
        priority = +service.priority;
        priority++;
    }

    private Map<AbstractService, StatusEnum> blockedBy = new HashMap<>();

    public void blockedBy(AbstractService service) {
        service.increasePriority(this);
        updatePriorityChange(service);
        status = StatusEnum.BLOCKED;
        service.addObserver(this);
        blockedBy.put(service, service.getStatus());
    }

    /**
     * called after task execution success
     */
    public void onComplete() {
        log.debug("updating onComplete for - {} ", name);
        status = StatusEnum.COMPLETED;
        observers.stream().forEach(observers -> {
            observers.update(this);
        });
    }

    public void update(AbstractService service) {
        blockedBy.put(service, StatusEnum.COMPLETED);
        if (!blockedBy.containsValue(StatusEnum.READY) && !blockedBy.containsValue(StatusEnum.BLOCKED) && !blockedBy.containsValue(StatusEnum.RUNNING))
            status = StatusEnum.READY;
    }

    public void updatePriorityChange(AbstractService svc1) {
        Map<AbstractService, StatusEnum> blockedBy = svc1.getBlockedBy();
        blockedBy.keySet().stream().forEach(var0 -> var0.priority++);
    }

    public void addObserver(AbstractService service) {
        observers.add(service);
    }

    public abstract CompletableFuture<T> task();

}
