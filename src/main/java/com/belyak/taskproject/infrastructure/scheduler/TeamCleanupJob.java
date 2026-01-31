package com.belyak.taskproject.infrastructure.scheduler;

import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class TeamCleanupJob {

    private final TeamRepository teamRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void permanentlyDeleteOldTeams() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        teamRepository.deleteByStatusAndDeletedAtBefore(TeamStatus.DELETED, thirtyDaysAgo);
    }
}
