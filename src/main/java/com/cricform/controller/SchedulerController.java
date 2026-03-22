package com.cricform.controller;

import com.cricform.scheduler.DailyRefreshScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SchedulerController {

    private final DailyRefreshScheduler dailyRefreshScheduler;

    @PostMapping("/refresh")
    public ResponseEntity<String> triggerRefresh() {
        dailyRefreshScheduler.triggerManualRefresh();
        return ResponseEntity.ok("Manual refresh completed");
    }
}
