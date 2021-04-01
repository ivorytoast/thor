package com.titan.thor.database.converter;

import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;

public class Converter {

    public static void viewToDataModelConverter(Order order, OrderDAO databaseOrder) {
        databaseOrder.setUserID(order.getUserID());
        databaseOrder.setId(order.getId());
        databaseOrder.setSymbol(order.getSymbol());
        databaseOrder.setQuantity(order.getQuantity());
        databaseOrder.setPrice(order.getPrice());
        databaseOrder.setSide(order.getSide());
        databaseOrder.setQuantityRemaining(order.getQuantityRemaining());
    }


}
