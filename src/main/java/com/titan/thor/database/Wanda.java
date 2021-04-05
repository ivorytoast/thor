package com.titan.thor.database;

import com.titan.thor.database.converter.Converter;
import com.titan.thor.database.repository.Pietro;
import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log
@Service
@Transactional
public class Wanda {

    @Autowired
    private Pietro orderRepository;

    public Wanda() {}

    public List<Order> getAllOrdersFromDatabase() {
        List<Order> orderViewList = new ArrayList<>();
        try {
            List<OrderDAO> data = orderRepository.findAll();
            Converter.dataToViewModelConverterForOrderList(orderViewList, data);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }
        return orderViewList;
    }

    public long addOrderToDatabase(MawOrderRequest orderRequest) {
        OrderDAO orderDAO = new OrderDAO();
        try {
            log.info("Added order into the database!");
            Converter.viewToDataModelConverter(orderRequest, orderDAO);
            OrderDAO createdOrder = orderRepository.save(orderDAO);
            return createdOrder.getId();
        } catch (Exception e) {
            log.info("Something went wrong adding the order...");
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

    public void cancelOrder(long orderID) {
        OrderDAO order = getOneOrderFromDatabase(orderID);
        order.setCancelled(true);
        updateOrderInDatabase(order);
    }

    public void updateOrderInDatabase(OrderDAO orderDAO) {
        orderRepository.save(orderDAO);
    }

    public String updateOrderAfterMatch(Order order) {
        OrderDAO orderDAO = orderRepository.getOne(order.getId());
        orderDAO.setQuantityRemaining(order.getQuantityRemaining());
        orderDAO.setCancelled(order.getCancelled());
        log.info("Updated OrderDAO: " + orderDAO.toString());

        orderRepository.save(orderDAO);
        return "Updated order!";
    }

}
