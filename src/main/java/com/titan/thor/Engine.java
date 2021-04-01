package com.titan.thor;

import com.titan.thor.model.Order;
import com.titan.thor.model.base.Book;
import com.titan.thor.model.children.MatchOutput;
import com.titan.thor.model.children.Symbol;
import lombok.extern.java.Log;

import java.util.*;

@Log
public class Engine {

    boolean debug = true;

    Map<String, Symbol> symbols;

    public Engine() {
        symbols = new HashMap<>();
    }

    public List<Order> acceptOrder(Order order) {
        List<Order> allAffectedOrders = new ArrayList<>();

        // Add Symbol to REDIS (Set of available symbols)
        Symbol symbol = new Symbol(order.getSymbol());

        long originalQuantity = order.getQuantityRemaining();
        if (order.getSide().equals("buy")) {
            // Add
            symbol.bids.addOrder(order);

            // Match
            MatchOutput matchOutput = matchOrder(order, symbol.asks);

            // Remove/Update Incoming Order
            Order matchedIncomingOrder = matchOutput.getModifiedOrder();

            if (matchOutput.isFullyMatch()) {
                if (debug) System.out.println("The buy order was fully matched!");
                symbol.bids.removeOrder(matchedIncomingOrder.getId());
            } else {
                if (debug) System.out.println("The buy order was partially matched!");
                symbol.bids.updateOrder(matchedIncomingOrder, originalQuantity - order.getQuantityRemaining());
            }

            allAffectedOrders.add(matchedIncomingOrder);

            // Remove/Update Existing Orders
            for (long orderID : matchOutput.getExistingOrdersToRemove()) {
                log.info("Removing the following orderID: " + orderID);
                Order removedOrder = symbol.asks.tesseract.remove(orderID);
                allAffectedOrders.add(removedOrder);
            }
            for (long orderID : matchOutput.getExistingOrdersToUpdate()) {
                log.info("Getting the following orderID to update (in asks): " + orderID);
                Order updatedOrder = symbol.asks.tesseract.find(orderID);
                allAffectedOrders.add(updatedOrder);
            }
        } else {
            // Add
            symbol.asks.addOrder(order);

            // Match
            MatchOutput matchOutput = matchOrder(order, symbol.bids);

            // Remove/Update Incoming Order
            Order matchedIncomingOrder = matchOutput.getModifiedOrder();

            if (matchOutput.isFullyMatch()) {
                if (debug) System.out.println("The buy order was fully matched!");
                symbol.asks.removeOrder(matchOutput.getModifiedOrder().getId());
            } else {
                if (debug) System.out.println("The buy order was partially matched!");
                symbol.asks.updateOrder(matchOutput.getModifiedOrder(), originalQuantity - order.getQuantityRemaining());
            }

            allAffectedOrders.add(matchOutput.getModifiedOrder());

            // Remove/Update Existing Orders
            for (long orderID : matchOutput.getExistingOrdersToRemove()) {
                log.info("Removing the following orderID: " + orderID);
                Order removedOrder = symbol.bids.tesseract.remove(orderID);
                allAffectedOrders.add(removedOrder);
            }
            for (long orderID : matchOutput.getExistingOrdersToUpdate()) {
                log.info("Getting the following orderID to update (in bids): " + orderID);
                Order updatedOrder = symbol.bids.tesseract.find(orderID);
                allAffectedOrders.add(updatedOrder);
            }
        }
        return allAffectedOrders;
    }

    private MatchOutput matchOrder(Order incomingOrder, Book book) {
        Set<Double> bestPricesToFillWith = findTheBestPricesToFillWith(incomingOrder, book);
        Set<Long> existingOrdersToRemove = new HashSet<>();
        Set<Long> existingOrdersToUpdate = new HashSet<>();
        for (double price : bestPricesToFillWith) {
            for (Order existingOrder : book.tesseract.getOrdersGroupedByPrice().get(price).values()) {
                if (incomingOrder.getQuantity() == 0) {
                    break;
                }
                if (incomingOrder.getQuantity().equals(existingOrder.getQuantityRemaining())) {
                    existingOrdersToRemove.add(existingOrder.getId());

                    return new MatchOutput(true, incomingOrder, existingOrdersToRemove, existingOrdersToUpdate);
                } else if (incomingOrder.getQuantityRemaining() < existingOrder.getQuantityRemaining()) {
                    long quantityChanging = incomingOrder.getQuantityRemaining();

                    existingOrder.setQuantityRemaining(existingOrder.getQuantityRemaining() - quantityChanging);
                    book.tesseract.update(existingOrder.getId(), quantityChanging);
                    existingOrdersToUpdate.add(existingOrder.getId());

                    return new MatchOutput(true, incomingOrder, existingOrdersToRemove, existingOrdersToUpdate);
                } else {
                    long quantityChanging = existingOrder.getQuantityRemaining();

                    existingOrdersToRemove.add(existingOrder.getId());
                    incomingOrder.setQuantityRemaining(incomingOrder.getQuantityRemaining() - quantityChanging);
                }
            }
        }
        return new MatchOutput(false, incomingOrder, existingOrdersToRemove, existingOrdersToUpdate);
    }

    private Set<Double> findTheBestPricesToFillWith(Order order, Book book) {
        long start = System.nanoTime();
        if (book.tesseract.getAmountAvailable() < order.getQuantity()) {
            return new HashSet<>();
        } else {
            long toBeFilled = order.getQuantity();
            Set<Double> prices = new HashSet<>();
            for (Map.Entry<Double, Long> entry : book.tesseract.getNumberOfSharesPerPrice().entrySet()) {
                if (toBeFilled == 0) return prices;
                if (entry.getValue() > 0) {
                    if (toBeFilled <= entry.getValue()) {
                        toBeFilled = 0;
                    } else {
                        toBeFilled -= entry.getValue();
                    }
                    prices.add(entry.getKey());
                }
            }
            long end = System.nanoTime();
            double seconds = (double) (end - start) / 1_000_000_000.0;
            if (order.getId() > 999_999 && order.getId() % 100_000 == 0) {
                System.out.println("Finding best prices to fill with time: " + seconds);
            }
            return prices;
        }
    }

    public void updateOrder(Order order) {
        /*
        TODO: Doing the whole thing...
         */
    }

    public Order convertFixToOrder(String fix) {
        /*
        TODO: All of it
         */
        return null;
    }

    public String convertOrderToFix(Order order) {
        /*
        TODO: All of it
         */
        return "";
    }

}
