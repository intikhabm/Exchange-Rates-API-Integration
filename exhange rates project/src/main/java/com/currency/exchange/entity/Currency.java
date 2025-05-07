package com.currency.exchange.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "currency")
public class Currency {
    @Id
    private String code;
    private String name;

    public Currency() {
    }

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getters and setters
}