package com.titan.thor.database.converter;

import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;

import java.util.List;

public class Converter {

    public static void dataToViewModelConverterForOrderList(List<Order> viewModel, List<OrderDAO> dataModel) {
        for (int i = 0; i < dataModel.size(); i++) {
            Order orderView = new Order();
            createNewOrderFromDAO(orderView,dataModel.get(i));
            viewModel.add(orderView);
        }
    }

    public static void createNewDatabaseOrderFromMawRequest(MawOrderRequest order, OrderDAO databaseOrder) {
        databaseOrder.setUserID(order.getUserID());
        databaseOrder.setId(null);
        databaseOrder.setSymbol(order.getSymbol());
        databaseOrder.setQuantity(order.getQuantity());
        databaseOrder.setPrice(order.getPrice());
        databaseOrder.setSide(order.getSide());
        databaseOrder.setQuantityRemaining(order.getQuantity());
        databaseOrder.setCancelled(false);
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
