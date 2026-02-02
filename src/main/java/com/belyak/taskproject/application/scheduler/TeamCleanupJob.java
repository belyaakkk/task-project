package com.belyak.taskproject.application.scheduler;

import com.belyak.taskproject.domain.port.service.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamCleanupJob {

    private final TeamService teamService;

    @Transactional
    @Scheduled(cron = "${app.scheduling.team-cleanup-cron:0 0 3 * * *}")
    public void permanentlyDeleteOldTeams() {
        log.info("Starting scheduled team cleanup...");
        teamService.cleanupDeletedTeams();
        log.info("Scheduled team cleanup finished.");
    }
}
