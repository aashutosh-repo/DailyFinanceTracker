package com.finance.tracker.stock.market.util;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.service.SyncJobService;
import com.finance.tracker.stock.market.dto.MarketData;
import com.finance.tracker.stock.market.entity.MarketPrice;
import com.finance.tracker.stock.market.provider.MarketDataProvider;
import com.finance.tracker.stock.market.validator.MarketDataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MarketSyncWorker {

    private final MarketDataProvider marketDataProvider;
    private final MarketPriceRepository marketPriceRepository;
    private final MarketDataValidator marketDataValidator;
    private final SyncJobService syncJobService;
    private final CompanyRepository companyRepository;


    @Async("marketDataExecutor")
    @Transactional
    public void processSync(UUID jobId,  UUID companyId, LocalDate fromDate, LocalDate toDate
    ) {
        try {
            Company company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new RuntimeException("Company not found"));

            List<MarketData> marketDataList = marketDataProvider.getHistoricalPrices(
                                    company.getSymbol(),
                                    fromDate,
                                    toDate);
            //Existing Price
            List<MarketPrice> existingPrices = marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                    company.getId(),
                    fromDate, toDate);

            //Lookup Map
            Map<LocalDate, MarketPrice> existingPriceMap = existingPrices
                    .stream()
                    .collect(Collectors.toMap(
                                    MarketPrice::getPriceDate,
                                    Function.identity()
                            )
                    );

            List<MarketPrice> pricesToInsert = new ArrayList<>();
            List<MarketPrice> pricesToUpdate = new ArrayList<>();
            int inserted = 0;
            int updated = 0;

            for (MarketData data : marketDataList) {
                //validate
                marketDataValidator.validate(data);

                MarketPrice existingPrice = existingPriceMap.get(data.priceDate());

                if (existingPrice != null) {

                    existingPrice.setOpenPrice(data.open());
                    existingPrice.setHighPrice(data.high());
                    existingPrice.setLowPrice(data.low());
                    existingPrice.setClosePrice(data.close());
                    existingPrice.setVolume(data.volume());
                    existingPrice.setSource(data.source());

                    pricesToUpdate.add(existingPrice);
                    updated++;
                } else {
                    MarketPrice newPrice = MarketPrice.builder()
                                    .company(company)
                                    .priceDate(data.priceDate())
                                    .openPrice(data.open())
                                    .highPrice(data.high())
                                    .lowPrice(data.low())
                                    .closePrice(data.close())
                                    .volume(data.volume())
                                    .source(data.source())
                                    .build();

                    pricesToInsert.add(newPrice);
                    inserted++;
                }
            }

            if (!pricesToInsert.isEmpty()) {
                marketPriceRepository.saveAll(pricesToInsert);
            }

            if (!pricesToUpdate.isEmpty()) {
                marketPriceRepository.saveAll(pricesToUpdate);
            }

            syncJobService.markSuccess(
                            jobId,
                            marketDataList.size(),
                            inserted,
                            updated
                    );


        } catch (Exception exception) {
            syncJobService.markFailed(jobId, exception);
        }
    }
}