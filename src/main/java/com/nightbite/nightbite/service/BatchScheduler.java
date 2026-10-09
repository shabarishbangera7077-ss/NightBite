package com.nightbite.nightbite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchService batchService;
    private final FeeService feeService;

    @Scheduled(fixedRate = 60000)
    public void lockOpenBatches() {
        batchService.lockExpiredBatches(feeService);
    }
}
