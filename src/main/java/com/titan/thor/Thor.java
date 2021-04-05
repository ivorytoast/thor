package com.titan.thor;

import com.titan.thor.converter.queue.FIXConverter;
import com.titan.thor.database.Wanda;
import com.titan.thor.database.converter.Converter;
import com.titan.thor.model.MawCancel;
import com.titan.thor.model.MawOrderRequest;
import com.titan.thor.model.Order;
import com.titan.thor.model.dao.OrderDAO;
import lombok.extern.java.Log;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.List;

@Log
public class Thor implements Runnable {

    private final Wanda wanda;
    private final Engine engine;

    public Thor(Wanda wanda) {
        this.engine = new Engine(wanda);
        this.wanda = wanda;
    }

    public List<Order> getAllOrdersFromDatabase() {
        return this.wanda.getAllOrdersFromDatabase();
    }

    public String mawNewOrder(MawOrderRequest request) {
        log.info("Received new order request from Maw: " + request.toString());
        long createdID = this.wanda.addOrderToDatabase(request);
        Order order = new Order(createdID, request.getUserID(), request.getSymbol(), request.getQuantity(), request.getPrice(), request.getSide());

        log.info("Order being sent down to the engine: " + order.toString());
        List<Order> ordersToUpdateInDatabase = engine.acceptOrder(order);

        for (Order orderToUpdate : ordersToUpdateInDatabase) {
            log.info("Updating order in database: " + orderToUpdate.getId());
            this.wanda.updateOrderAfterMatch(orderToUpdate);
        }

        return FIXConverter.convertOrderToFix(order);
    }

    // TODO: Next on the list
    public void mawCancel(MawCancel mawCancel) {
        long orderID = mawCancel.getOrderID();
        log.info("Received new cancel request from Maw for orderID: " + orderID);
        OrderDAO orderDAO = wanda.getOneOrderFromDatabase(orderID);
        Order order = new Order();
        Converter.createNewOrderFromDAO(order, orderDAO);

        wanda.cancelOrder(orderID);
        engine.cancelOrder(order);

        log.info("OrderID " + orderID + "cancelled");
    }

    @Override
    public void run() {
        log.info("Started Thor...");
        try (ZContext context = new ZContext()) {
            ZMQ.Socket responder = context.createSocket(SocketType.REP);

            boolean didConnect = responder.connect("tcp://bifrost:5560");
            log.info("(Thor) Connected to bifrost: " + didConnect);

            while (!Thread.currentThread().isInterrupted()) {
                String fixMessageFromLoki = responder.recvStr(0);
                System.out.printf("Received request: [%s]\n", fixMessageFromLoki);

                MawOrderRequest orderRequest = FIXConverter.convertFixToOrderRequest(fixMessageFromLoki);

                String returnMessage = mawNewOrder(orderRequest);

                responder.send(returnMessage);
            }
        }
    }

}
