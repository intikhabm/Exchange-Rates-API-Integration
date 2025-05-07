package com.currency.exchange.service;

import com.currency.exchange.entity.Currency;
import com.currency.exchange.entity.ExchangeRate;
import com.currency.exchange.repository.CurrencyRepository;
import com.currency.exchange.repository.ExchangeRateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CurrencyExchangeService {

    private final Logger logger = LogManager.getLogger(CurrencyExchangeService.class);

    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;


    private final Map<String, String> currencyMap = new ConcurrentHashMap<>();
    private final Map<String, Double> inMemoryRates = new ConcurrentHashMap<>();


    @Autowired
    public CurrencyExchangeService(RestTemplate restTemplate, CurrencyRepository currencyRepository, ExchangeRateRepository exchangeRateRepository) {
        this.restTemplate = restTemplate;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("Currency List When Starting The Project {}", currencyMap);
//        fetchAndStoreRates();
    }


    public Map getCurrencies() {
        logger.info("Cron Start");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String currenciesAPIUrl = "https://openexchangerates.org/api/currencies.json?prettyprint=false&show_alternative=false&show_inactive=false";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    currenciesAPIUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, String> currencies = response.getBody();
                currencyMap.clear();
                currencyMap.putAll(currencies);

                // Save to database
                currencyRepository.saveAll(currencies.entrySet().stream()
                        .map(entry -> new Currency(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()));
                logger.info("Updates Currency Rates {}", currencyMap);
                return currencyMap;
            } else {
                logger.info("Failed to retrieve currencies: {}", response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Currency API Failed Due to Error: ", e);
        }
        return Collections.emptyMap();
    }

    public Map<String, String> getCurrencyRates() {
        return currencyMap;
    }



//    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Scheduled(cron = "0 */2 * * * *")
    public void fetchAndStoreRates() {
        logger.info("Getting Latest Currency Rates");
        String exchangeRatesAPIUrl = "https://openexchangerates.org/api/latest.json?app_id=2e02d5ce0b894f84b0ef45a6caeb3e1c";
       try {
           ResponseEntity<Map> response = restTemplate.getForEntity(exchangeRatesAPIUrl, Map.class);
           if (response.getStatusCode().is2xxSuccessful()) {
               Map<String, Object> body = response.getBody();
               if (body != null && body.containsKey("rates")) {
                   Map<String, Double> rates = (Map<String, Double>) body.get("rates");
                   LocalDateTime now = LocalDateTime.now();

                   List<ExchangeRate> exchangeRates=new ArrayList<>();
                   for (Map.Entry<String, Double> entry : rates.entrySet()) {
                       logger.info("entry {}", entry);
                       Object rateValue = entry.getValue();
                       // Save to DB
                       ExchangeRate rate = new ExchangeRate();
                       rate.setCurrency(entry.getKey());
                       if (rateValue instanceof Integer) {
                           rate.setRate(((Integer) rateValue).doubleValue());
                       }
                       else {
                           rate.setRate((Double) rateValue);
                       }
                       rate.setTimestamp(now);
                       exchangeRates.add(rate);

                       // Save to in-memory map
                       inMemoryRates.put(entry.getKey(), entry.getValue());
                   }

                   exchangeRateRepository.saveAll(exchangeRates);
               }
           }
       } catch (Exception e) {
           throw new RuntimeException("Exchange Rate API Error: ", e);
       }

    }

    public Map<String, Double> getAllRates() {
        return inMemoryRates;
    }

    public Double getRateByCurrency(String currency) {
        return inMemoryRates.get(currency.toUpperCase());
    }

}
