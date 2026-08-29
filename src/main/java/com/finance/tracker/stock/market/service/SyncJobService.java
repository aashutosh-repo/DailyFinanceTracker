package com.finance.tracker.stock.market.service;

import com.finance.tracker.constants.SyncJobStatus;
import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.market.SyncJobRepository;
import com.finance.tracker.stock.market.dto.SyncJobPageResponse;
import com.finance.tracker.stock.market.dto.SyncJobResponse;
import com.finance.tracker.stock.market.entity.SyncJob;
import com.finance.tracker.stock.market.specification.SyncJobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncJobService {

    private final SyncJobRepository syncJobRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SyncJob createJob(
            Company company,
            LocalDate fromDate,
            LocalDate toDate,
            String provider
    ) {

        SyncJob job = SyncJob.builder()
                        .company(company)
                        .fromDate(fromDate)
                        .toDate(toDate)
                        .provider(provider)
                        .status(SyncJobStatus.RUNNING)
                        .startedAt(LocalDateTime.now())
                        .recordsProcessed(0)
                        .recordsInserted(0)
                        .recordsUpdated(0)
                        .build();

        return syncJobRepository.save(job);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSuccess(
            UUID jobId,
            int processed,
            int inserted,
            int updated) {
        SyncJob job = syncJobRepository.findById(jobId)
                        .orElseThrow();


        job.setStatus(SyncJobStatus.SUCCESS);
        job.setRecordsProcessed(processed);
        job.setRecordsInserted(inserted);
        job.setRecordsUpdated(updated);
        job.setCompletedAt(LocalDateTime.now());

        syncJobRepository.save(job);
    }

    @Transactional
    public void markRunning(UUID jobId) {
        SyncJob job = syncJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Sync job not found: " + jobId));

        job.setStatus(SyncJobStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());

        syncJobRepository.save(job);
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            UUID jobId,
            Exception exception) {

        SyncJob job = syncJobRepository.findById(jobId).orElseThrow();

        job.setStatus(SyncJobStatus.FAILED);
        job.setErrorMessage(exception.getMessage());
        job.setCompletedAt(LocalDateTime.now());

        syncJobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public SyncJobResponse getJob(UUID jobId) {

        SyncJob job = syncJobRepository.findById(jobId)
                        .orElseThrow(() -> new RuntimeException("Sync job not found: " + jobId));
        return mapToResponse(job);
    }

    @Transactional(readOnly = true)
    public SyncJobPageResponse getJobs(
            String symbol,
            SyncJobStatus status,
            int page,
            int size,
            String sort
    ) {
        Sort sortObject = createSort(sort);


        Pageable pageable = PageRequest.of(
                        page,
                        size,
                        sortObject
                );


        Specification<SyncJob> specification = Specification.where(
                                SyncJobSpecification.hasSymbol(symbol))
                        .and(SyncJobSpecification.hasStatus(status));


        Page<SyncJob> jobPage = syncJobRepository.findAll(specification, pageable);

        List<SyncJobResponse> responses = jobPage.getContent().stream()
                        .map(this::mapToResponse)
                        .toList();


        return new SyncJobPageResponse(
                responses,
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages()
        );
    }

    @Transactional
    public SyncJob createQueuedJob(
            Company company,
            LocalDate fromDate,
            LocalDate toDate,
            String provider
    ) {

        SyncJob job = new SyncJob();
        job.setId(UUID.randomUUID());
        job.setCompany(company);
        job.setFromDate(fromDate);
        job.setToDate(toDate);
        job.setProvider(provider);
        job.setStatus(SyncJobStatus.QUEUED);
        job.setStartedAt(null);

        return syncJobRepository.save(job);
    }

    private Sort createSort(String sort) {

        String field = "startedAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort != null && !sort.isBlank()) {

            String[] parts = sort.split(",");
            field = parts[0];
            if (!ALLOWED_SORT_FIELDS.contains(field)) {
                field = "startedAt";
            }

            if (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) {
                direction = Sort.Direction.ASC;
            }
        }

        return Sort.by(direction, field);
    }

    private SyncJobResponse mapToResponse(SyncJob job) {

        return new SyncJobResponse(
                job.getId(),
                job.getCompany().getSymbol(),
                job.getStatus().name(),
                job.getFromDate(),
                job.getToDate(),
                job.getProvider(),
                job.getTotalRecords(),
                job.getInsertedRecords(),
                job.getUpdatedRecords(),
                job.getErrorMessage(),
                job.getStartedAt(),
                job.getCompletedAt()
        );
    }

    private static final Set<String>
            ALLOWED_SORT_FIELDS =
            Set.of("startedAt",
                    "completedAt",
                    "status"
            );
}