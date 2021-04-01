package com.titan.thor.model.children;

import com.titan.thor.cache.Tesseract;
import com.titan.thor.model.base.Book;
import com.titan.thor.model.enums.BookType;

public class Asks extends Book {

    public Asks(String underlier) {
        super(BookType.ASK, new Tesseract(BookType.ASK, underlier));
    }

}
