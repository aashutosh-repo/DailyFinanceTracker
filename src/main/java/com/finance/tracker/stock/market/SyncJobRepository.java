package com.finance.tracker.stock.market;


import com.finance.tracker.stock.market.entity.SyncJob;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID>, JpaSpecificationExecutor<SyncJob> {

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"company"})
    Optional<SyncJob> findById(@NotNull UUID id);

    @Override
    @EntityGraph(attributePaths = {"company"})
    Page<SyncJob> findAll(Specification<SyncJob> specification, @NotNull Pageable pageable);
}