package com.hw.springboot.clazz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AsyncServiceExecutor<T> {

    private Set<AbstractService> servicePool = new HashSet<>();
    private List<AbstractService> queuedServices = new ArrayList<>();
    private Map<AbstractService, CompletableFuture> runningMap = new HashMap<>();
    private AbstractService monitorService = new AbstractService("monitor") {
        @Override
        public CompletableFuture task() {
            return null;
        }

        @Override
        public void update(AbstractService service) {
            /**
             * run every time if a service got completed
             */
            log.debug("service complete- [{}]", service.getName());
            List<AbstractService> readyServices = getReadyServices();
            queuedServices.addAll(readyServices);
            queuedServices.sort((t2, t1) -> t1.priority - t2.priority);
            /**
             * attach monitor service, make sure monitor service get last called
             */
            readyServices.stream().forEach(svc -> svc.addObserver(monitorService));
            servicePool.remove(service);
        }
    };

    public T executeReadyServiceImproved() throws InterruptedException {

        log.debug("total number of services - {}", servicePool.size());
        List<AbstractService> readyServices = getReadyServices();

        /**
         * attach monitor service, make sure monitor service get last called
         */
        readyServices.stream().forEach(service -> service.addObserver(monitorService));
        queuedServices.addAll(readyServices);
        queuedServices.sort((service, t1) -> t1.priority - service.priority);
        while (servicePool.size() != 0) {
            /**
             * add thread sleep to improve quick service always take all capacity of pool
             */
            Thread.sleep(10);
            List<AbstractService> preQueueCheck = runningMap.keySet().stream().filter(e -> runningMap.get(e).isDone()).collect(Collectors.toList());
            preQueueCheck.forEach(cSvc -> {
                cSvc.onComplete();
                runningMap.remove(cSvc);
            });
            /**
             * if available threads is less then readyService, submit one by one
             * services do not submitted are put back to pool
             */
            int availableThread = ForkJoinPool.getCommonPoolParallelism() - ForkJoinPool.commonPool().getRunningThreadCount();
            queuedServices.stream().skip(0L).limit(availableThread).forEach(
                    rSvc -> {
                        CompletableFuture task = rSvc.task();
                        rSvc.setStatus(StatusEnum.RUNNING);
                        runningMap.put(rSvc, task);
                    }

            );
            queuedServices.clear();
        }
        return null;
    }

    public List<AbstractService> getReadyServices() {
        List<AbstractService> readyService = servicePool.stream().filter(svc -> StatusEnum.READY.equals(svc.getStatus())).collect(Collectors.toList());
        /**
         * check if service is already in queue
         */
        List<AbstractService> filteredService = readyService.stream().filter(e -> !queuedServices.contains(e)).collect(Collectors.toList());
        if (!filteredService.isEmpty())
            log.debug("found ready services - {}", filteredService.stream().map(AbstractService::getName).collect(Collectors.toList()));
        return filteredService;
    }

    public void addAll(AbstractService... services) {
        servicePool.addAll(Arrays.stream(services).collect(Collectors.toSet()));
    }

}
