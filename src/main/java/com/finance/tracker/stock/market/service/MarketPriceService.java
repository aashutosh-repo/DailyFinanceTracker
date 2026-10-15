package com.finance.tracker.stock.market.service;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.dto.*;
import com.finance.tracker.stock.market.entity.MarketPrice;
import com.finance.tracker.stock.market.entity.SyncJob;
import com.finance.tracker.stock.market.provider.MarketDataProvider;
import com.finance.tracker.stock.market.provider.MarketDataClassification;
import com.finance.tracker.stock.market.util.MarketPriceSyncWorker;
import com.finance.tracker.stock.market.util.MarketSyncWorker;
import com.finance.tracker.stock.market.validator.MarketDataValidator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketPriceService {

    private final CompanyRepository companyRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final MarketDataProvider marketDataProvider;
    private final MarketDataValidator marketDataValidator;
    private final SyncJobService syncJobService;
    private final MarketSyncWorker marketSyncWorker;
    private final MarketPriceSyncWorker marketPriceSyncWorker;


    @Transactional
    public MarketSyncResponse syncHistoricalPrices(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate) {

                validateDateRange(fromDate, toDate);

        Company company = companyRepository.findBySymbol(symbol.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));

        /*
         * Create sync job
         */

        SyncJob job = syncJobService.createJob(
                        company,
                        fromDate,
                        toDate,
                        marketDataProvider.getProviderName());


        try {

            /*
             * Fetch external data
             */

            List<MarketData> marketDataList = marketDataProvider.getHistoricalPrices(
                                    company.getSymbol(),
                                    fromDate,
                                    toDate
                            );

            marketDataValidator.validateBatch(marketDataList, fromDate, toDate);


            int inserted = 0;
            int updated = 0;

            /*
             * Process records
             */

            for (MarketData data : marketDataList) {

                /*
                 * Validate
                 */
                marketDataValidator.validate(data);


                /*
                 * Check existing record
                 */

                Optional<MarketPrice> existingPrice = marketPriceRepository.findByCompanyIdAndPriceDate(
                                        company.getId(),
                                        data.priceDate()
                                );


                if (existingPrice.isPresent()) {

                    MarketPrice price = existingPrice.get();
                    price.setOpenPrice(data.open());
                    price.setHighPrice(data.high());
                    price.setLowPrice(data.low());
                    price.setClosePrice(data.close());
                    price.setVolume(data.volume());
                    price.setSource(data.source());

                    marketPriceRepository
                            .save(price);

                    updated++;

                } else {

                    MarketPrice price = MarketPrice.builder()
                                    .company(company)
                                    .priceDate(data.priceDate())
                                    .openPrice(data.open())
                                    .highPrice(data.high())
                                    .lowPrice(data.low())
                                    .closePrice(data.close())
                                    .volume(data.volume())
                                    .source(data.source())
                                    .build();

                    marketPriceRepository.save(price);
                    inserted++;
                }
            }

            /*
             * Mark job successful
             */

            syncJobService.markSuccess(
                    job.getId(),
                    marketDataList.size(),
                    inserted,
                    updated
            );


            return MarketSyncResponse.builder()
                    .symbol(company.getSymbol())
                    .totalRecords(marketDataList.size())
                    .insertedRecords(inserted)
                    .updatedRecords(updated)
                    .provider(
                            marketDataProvider.getProviderName()
                    )
                    .dataClassification(marketDataProvider.getDataClassification())
                    .dataQualityWarnings(syncWarnings(marketDataList))

                    .build();


        } catch (Exception exception) {
            syncJobService.markFailed(job.getId(), exception);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse>
    getHistoricalPrices(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate) {
        validateDateRange(fromDate, toDate);
        Company company = companyRepository.findBySymbol(symbol.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));


        return marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                        company.getId(),
                        fromDate,
                        toDate
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    private MarketPriceResponse mapToResponse(MarketPrice price) {
        return MarketPriceResponse.builder()
                .priceDate(price.getPriceDate())
                .open(price.getOpenPrice())
                .high(price.getHighPrice())
                .low(price.getLowPrice())
                .close(price.getClosePrice())
                .volume(price.getVolume())
                .source(price.getSource())
                .dataClassification(MarketDataClassification.fromProvider(price.getSource()).name())
                .dataQualityWarnings(List.of("No exchange trading calendar is configured; missing dates cannot be classified as missing sessions."))
                .build();
    }

        private List<String> syncWarnings(List<MarketData> records) {
                List<String> warnings = new java.util.ArrayList<>();
                records.stream()
                                .filter(record -> record.dataQualityWarnings() != null)
                                .flatMap(record -> record.dataQualityWarnings().stream())
                                .filter(warning -> warning != null && !warning.isBlank())
                                .forEach(warnings::add);
                warnings.add("No exchange trading calendar is configured; missing dates cannot be classified as missing sessions.");
                return warnings.stream().distinct().toList();
        }

    public MarketSyncJobResponse startHistoricalSync(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate) {

        validateDateRange(fromDate, toDate);

        Company company = companyRepository.findBySymbol(symbol.toUpperCase())
                        .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));

        /*
         * Create job
         */

        SyncJob job = syncJobService.createJob(company, fromDate, toDate, marketDataProvider.getProviderName());

        /*
         * Start async processing
         */

        marketSyncWorker.processSync(
                        job.getId(),
                        company.getId(),
                        fromDate,
                        toDate
                );

        /*
         * Return immediately
         */

        return MarketSyncJobResponse.builder()
                .jobId(job.getId())
                .status("RUNNING")
                .message("Market data sync started")
                .provider(marketDataProvider.getProviderName())
                .dataClassification(marketDataProvider.getDataClassification())
                .dataQualityWarnings(List.of("No exchange trading calendar is configured; missing dates cannot be classified as missing sessions."))
                .build();
    }

    @Transactional
    public AsyncMarketSyncResponse startAsyncSync(
            String symbol,
            LocalDate from,
            LocalDate to
    ) {

        validateDateRange(from, to);

        Company company = companyRepository.findBySymbolIgnoreCase(symbol)
                        .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));

        SyncJob job = syncJobService.createQueuedJob(company, from, to, marketDataProvider.getProviderName());

        // IMPORTANT:
        // Call async worker through Spring bean
        marketPriceSyncWorker.process(job.getId());

        return new AsyncMarketSyncResponse(job.getId(), job.getStatus().name());
    }

        private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
                if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
                }
        }
}
