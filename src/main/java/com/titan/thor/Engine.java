package com.titan.thor;

import com.titan.thor.database.Wanda;
import com.titan.thor.model.Order;
import com.titan.thor.model.base.Book;
import com.titan.thor.model.children.MatchOutput;
import com.titan.thor.model.children.Symbol;
import lombok.extern.java.Log;

import java.util.*;

@Log
public class Engine {

    Wanda wanda;
    Map<String, Symbol> symbols;

    public Engine(Wanda wanda) {
        this.wanda = wanda;
        symbols = new HashMap<>();
    }

    // TODO: Next
    public void cancelOrder(Order order) {
        Symbol symbol = new Symbol(order.getSymbol());

        if (order.getSide().equals("buy")) {
            symbol.bids.removeMatchedOrder(order.getId());
        } else {
            symbol.asks.removeMatchedOrder(order.getId());
        }

        log.info("Cancelled " + order.getId() + " in REDIS");
    }

    public List<Order> execute(Order orderToExecute, Book bookToAdd, Book bookToMatch) {
        List<Order> ordersToUpdateInDatabase = new ArrayList<>();

        bookToAdd.addOrder(orderToExecute);

        MatchOutput matchOutput = matchOrder(orderToExecute, bookToMatch);

        if (matchOutput.isFullyMatch()) {
            System.out.println("Order was fully matched");
            bookToAdd.removeMatchedOrder(orderToExecute.getId());
            orderToExecute.setQuantityRemaining(0L);
            ordersToUpdateInDatabase.add(orderToExecute);
        } else {
            log.info("Order was not matched due to insufficient quantity in cache");
        }

        for (long orderID : matchOutput.getExistingOrdersToRemove()) {
            log.info("OrderID to be logically removed in database: " + orderID);
            Order removedOrder = bookToMatch.tesseract.removeMatchedOrder(orderID);
            ordersToUpdateInDatabase.add(removedOrder);
        }
        for (long orderID : matchOutput.getExistingOrdersToUpdate()) {
            log.info("OrderID to be updated in database: " + orderID);
            Order updatedOrder = bookToMatch.tesseract.find(orderID);
            ordersToUpdateInDatabase.add(updatedOrder);
        }

        return ordersToUpdateInDatabase;
    }

    public List<Order> acceptOrder(Order incomingOrder) {
        Symbol symbol = new Symbol(incomingOrder.getSymbol());

        if (incomingOrder.getSide().equals("buy")) {
            return execute(incomingOrder, symbol.bids, symbol.asks);
        } else {
            return execute(incomingOrder, symbol.asks, symbol.bids);
        }
    }

    private MatchOutput matchOrder(Order incomingOrder, Book book) {
        Set<Long> cacheOrdersToRemove = new HashSet<>();
        Set<Long> cacheOrdersToUpdate = new HashSet<>();

        LinkedList<Double> bestPricesToFillWith = findTheBestPricesToFillWith(incomingOrder, book);
        if (bestPricesToFillWith.size() == 0) {
            log.severe("There was not enough initial quantity to fully match the request...");
            return new MatchOutput(false, cacheOrdersToRemove, cacheOrdersToUpdate);
        }

        long quantityTracker = incomingOrder.getQuantityRemaining();
        for (double price : bestPricesToFillWith) {
            log.info("Number of orders for price: " + price + " -> " + book.tesseract.getOGPOrdersForPrice(price));
            for (Order cacheOrder : book.tesseract.getOGPOrdersForPrice(price)) {
                log.info("Incoming order: " + incomingOrder.toString());
                log.info("Cache order: " + cacheOrder.toString());
                if (incomingOrder.getQuantity().equals(cacheOrder.getQuantityRemaining())) {
                    cacheOrdersToRemove.add(cacheOrder.getId());

                    return new MatchOutput(true, cacheOrdersToRemove, cacheOrdersToUpdate);
                } else if (incomingOrder.getQuantityRemaining() < cacheOrder.getQuantityRemaining()) {
                    long quantityChanging = incomingOrder.getQuantityRemaining();

                    book.tesseract.update(cacheOrder.getId(), quantityChanging);

                    cacheOrdersToUpdate.add(cacheOrder.getId());

                    return new MatchOutput(true, cacheOrdersToRemove, cacheOrdersToUpdate);
                } else {
                    long quantityChanging = cacheOrder.getQuantityRemaining();

                    cacheOrdersToRemove.add(cacheOrder.getId());
                    quantityTracker -= quantityChanging;

                    if (quantityTracker == 0) {
                        log.info("The quantity got to 0. Order was matched");
                        return new MatchOutput(true, cacheOrdersToRemove, cacheOrdersToUpdate);
                    }
                }
            }
        }
        log.severe("Something went wrong, the order did not fully match...");
        return new MatchOutput(false, cacheOrdersToRemove, cacheOrdersToUpdate);
    }

    private LinkedList<Double> findTheBestPricesToFillWith(Order order, Book book) {
        return (book.tesseract.getAmountAvailable() < order.getQuantity()) ? new LinkedList<>() : book.tesseract.getPrices();
    }

}
