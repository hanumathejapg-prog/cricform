package com.cricform.scheduler;

import com.cricform.service.CacheService;
import com.cricform.service.DataIngestionService;
import com.cricform.service.FormScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyRefreshScheduler {

    private final DataIngestionService dataIngestionService;
    private final FormScoreService formScoreService;
    private final CacheService cacheService;

    @Scheduled(cron = "${cricform.scheduler.cron}")
    public void runDailyRefresh() {
        dataIngestionService.runFullIngestionCycle();
        formScoreService.recalculateAllScores();
        cacheService.invalidateAllFormScores();
        cacheService.repopulateAllFormScores();
        cacheService.updateLastRefreshTimestamp();
        log.info("Daily refresh cycle completed");
    }

    public void triggerManualRefresh() {
        runDailyRefresh();
    }
}
