package com.finance.tracker.stock.market.service;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.dto.MarketData;
import com.finance.tracker.stock.market.dto.SyncJobResponse;
import com.finance.tracker.stock.market.entity.MarketPrice;
import com.finance.tracker.stock.market.provider.MarketDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketPriceSyncService {

    private final CompanyRepository companyRepository;
    private final MarketDataProvider marketDataProvider;
    private final SyncJobService syncJobService;
    private final MarketPriceRepository marketPriceRepository;

    @Transactional
    public void executeSync(UUID jobId) {

        // =====================================
        // 1. Get Sync Job
        // =====================================

        SyncJobResponse job = syncJobService.getJob(jobId);

        // =====================================
        // 2. Get Company
        // =====================================

        Company company = companyRepository.findBySymbolIgnoreCase(job.symbol())
                        .orElseThrow(() -> new RuntimeException("Company not found: " + job.symbol()));

        // =====================================
        // 3. Fetch Market Data
        // =====================================

        List<MarketData> prices = marketDataProvider.getHistoricalPrices(job.symbol(), job.fromDate(), job.toDate());

        // =====================================
        // 4. Fetch Existing Prices - ONE QUERY
        // =====================================

        List<MarketPrice> existingPrices = marketPriceRepository.findByCompanyIdAndPriceDateBetween(
                                company.getId(),
                                job.fromDate(),
                                job.toDate()
                        );

        // =====================================
        // 5. Convert Existing Prices to Map
        // =====================================

        Map<LocalDate, MarketPrice> existingPriceMap = existingPrices.stream()
                        .collect(Collectors.toMap(
                                        MarketPrice::getPriceDate,
                                        marketPrice -> marketPrice
                                )
                        );


        // =====================================
        // 6. Prepare Batch Save
        // =====================================

        List<MarketPrice> pricesToSave = new ArrayList<>();
        int insertedRecords = 0;
        int updatedRecords = 0;

        // =====================================
        // 7. Process in Memory
        // =====================================

        for (MarketData priceData : prices) {

            MarketPrice existingPrice = existingPriceMap.get(priceData.priceDate());

            if (existingPrice != null) {

                updateMarketPrice(existingPrice, priceData);
                pricesToSave.add(existingPrice);
                updatedRecords++;
            } else {
                MarketPrice newPrice = createMarketPrice(company, priceData);
                pricesToSave.add(newPrice);
                insertedRecords++;
            }
        }


        // =====================================
        // 8. Batch Save
        // =====================================

        marketPriceRepository.saveAll(pricesToSave);
        // =====================================
        // 9. Mark SUCCESS
        // =====================================

        syncJobService.markSuccess(
                jobId,
                prices.size(),
                insertedRecords,
                updatedRecords
        );
    }

    private MarketPrice createMarketPrice(Company company, MarketData data) {

        MarketPrice marketPrice =
                new MarketPrice();

        marketPrice.setCompany(company);
        marketPrice.setPriceDate(data.priceDate());
        marketPrice.setOpenPrice(data.open());
        marketPrice.setHighPrice(data.high());
        marketPrice.setLowPrice(data.low());
        marketPrice.setClosePrice(data.close());
        marketPrice.setVolume(data.volume());

        return marketPrice;
    }


    private void updateMarketPrice(
            MarketPrice marketPrice,
            MarketData data
    ) {

        marketPrice.setOpenPrice(data.open());
        marketPrice.setHighPrice(data.high());
        marketPrice.setLowPrice(data.low());
        marketPrice.setClosePrice(data.close());
        marketPrice.setVolume(data.volume());
    }
}