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

    public static void createNewOrderFromDAO(Order order, OrderDAO databaseOrder) {
        order.setUserID(databaseOrder.getUserID());
        order.setId(databaseOrder.getId());
        order.setSymbol(databaseOrder.getSymbol());
        order.setQuantity(databaseOrder.getQuantity());
        order.setPrice(databaseOrder.getPrice());
        order.setSide(databaseOrder.getSide());
        order.setQuantityRemaining(databaseOrder.getQuantityRemaining());
        order.setCancelled(databaseOrder.isCancelled());
    }

}
