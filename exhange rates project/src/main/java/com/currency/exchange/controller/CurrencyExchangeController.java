package com.currency.exchange.controller;

import com.currency.exchange.service.CurrencyExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CurrencyExchangeController {

    @Autowired
    private CurrencyExchangeService service;

    @GetMapping("/currencies")
    public Map getCurrencies() {
        return service.getCurrencies();
    }


    @GetMapping("/rates")
    public Map<String, Double> getAllRates() {
        return service.getAllRates();
    }

    @GetMapping("/rates/{currency}")
    public ResponseEntity<?> getRate(@PathVariable String currency) {
        Double rate = service.getRateByCurrency(currency);
        return (rate != null) ? ResponseEntity.ok(rate) : ResponseEntity.notFound().build();
    }
}
