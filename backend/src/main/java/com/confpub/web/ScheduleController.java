package com.confpub.web;

import com.confpub.domain.Page;
import com.confpub.domain.Schedule;
import com.confpub.repository.PageRepository;
import com.confpub.repository.ScheduleRepository;
import com.confpub.web.dto.CreateScheduleRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleRepository scheduleRepository;
    private final PageRepository pageRepository;

    public ScheduleController(ScheduleRepository scheduleRepository, PageRepository pageRepository) {
        this.scheduleRepository = scheduleRepository;
        this.pageRepository = pageRepository;
    }

    @PostMapping
    public ResponseEntity<?> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        Page page = pageRepository.findById(request.getPageId())
                .orElse(null);

        if (page == null) {
            return ResponseEntity.badRequest().body("Page not found for id " + request.getPageId());
        }

        Schedule schedule = new Schedule();
        schedule.setPage(page);
        schedule.setScheduledAt(request.getScheduledAt());
        // status and attemptCount already have defaults

        Schedule saved = scheduleRepository.save(schedule);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Schedule> listSchedules() {
        return scheduleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getSchedule(@PathVariable Long id) {
        return scheduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

