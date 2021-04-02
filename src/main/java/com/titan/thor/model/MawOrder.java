package com.titan.thor.model;

public class MawOrder {

    private String fixMessage;

    public MawOrder() {}

    public MawOrder(String fixMessage) {
        this.fixMessage = fixMessage;
    }

    public String getFixMessage() {
        return fixMessage;
    }

    public void setFixMessage(String fixMessage) {
        this.fixMessage = fixMessage;
    }

}
