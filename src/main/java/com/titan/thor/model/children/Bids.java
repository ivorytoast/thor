package com.titan.thor.model.children;

import com.titan.thor.cache.Tesseract;
import com.titan.thor.model.base.Book;
import com.titan.thor.model.enums.BookType;

public class Bids extends Book {
    public Bids(String underlier) {
        super(BookType.BID, new Tesseract(BookType.BID, underlier));
    }
}
