package com.titan.thor.model.base;

import com.titan.thor.cache.Tesseract;
import com.titan.thor.database.Wanda;
import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;

@Log
public abstract class Book {

    public BookType bookType;

    public Tesseract tesseract;

    public Book(BookType bookType, Tesseract cache) {
        this.bookType = bookType;
        this.tesseract = cache;
    }

    public void addOrder(Order order) {
        log.info("Adding order");
        this.tesseract.add(order);
        log.info("Added order");
    }

    public Order removeOrder(long orderID) {
        return this.tesseract.remove(orderID);
    }

    public void updateOrder(Order order, long quantityToChange) {
        this.tesseract.update(order.getId(), quantityToChange);
    }

    public Order findOrder(long orderID) {
        return this.tesseract.find(orderID);
    }

    public BookType getBookType() {
        return bookType;
    }

}
