package com.finance.tracker.stock.market.specification;


import com.finance.tracker.constants.SyncJobStatus;
import com.finance.tracker.stock.market.entity.SyncJob;
import org.springframework.data.jpa.domain.Specification;

public class SyncJobSpecification {

    private SyncJobSpecification() {
    }

    public static Specification<SyncJob> hasSymbol(String symbol) {

        return (root, query, criteriaBuilder) -> {
            if (symbol == null || symbol.isBlank()) {
                return null;
            }

            return criteriaBuilder.equal(criteriaBuilder.upper(root.join("company").get("symbol")), symbol.toUpperCase()
            );
        };
    }


    public static Specification<SyncJob> hasStatus(SyncJobStatus status) {

        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}