package com.titan.thor.model.children;

import com.titan.thor.model.base.Book;

public class Symbol {

    public Book bids;
    public Book asks;
    public String underlier;

    public Symbol(String underlier) {
        this.bids = new Bids(underlier);
        this.asks = new Asks(underlier);
        this.underlier = underlier;
    }

}
