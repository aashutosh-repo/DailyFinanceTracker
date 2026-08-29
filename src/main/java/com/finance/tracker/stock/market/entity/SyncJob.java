package com.finance.tracker.stock.market.entity;

import com.finance.tracker.constants.SyncJobStatus;
import com.finance.tracker.stock.company.Company;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "market_sync_jobs",
        indexes = {
                @Index(
                        name = "idx_sync_job_company",
                        columnList = "company_id"
                ),
                @Index(
                        name = "idx_sync_job_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncJobStatus status;

    @Column(nullable = false, length = 50)
    private String provider;

    private Integer recordsProcessed;

    private Integer recordsInserted;

    private Integer recordsUpdated;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Integer totalRecords;
    private Integer InsertedRecords;
    private Integer updatedRecords;
}