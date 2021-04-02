package com.titan.thor.database;

import com.titan.thor.database.converter.Converter;
import com.titan.thor.database.repository.Pietro;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
public class Wanda {

    @Autowired
    private Pietro orderRepository;

    public Wanda() {}

    public long addOrderToDatabase(Order order) {
        OrderDAO orderDAO = new OrderDAO();
        try {
            Converter.viewToDataModelConverter(order, orderDAO);
            OrderDAO createdOrder = orderRepository.save(orderDAO);
            return createdOrder.getId();
        } catch (Exception e) {
            return -1;
        }
    }

    public OrderDAO getOneOrderFromDatabase(long orderID) {
        try {
            return orderRepository.getOne(orderID);
        }
        catch (Exception e) {
            log.info("Something went wrong!");
            log.severe(e.toString());
            return null;
        }
    }

    public void updateOrderInDatabase(OrderDAO orderDAO) {
        orderRepository.save(orderDAO);
    }

}
