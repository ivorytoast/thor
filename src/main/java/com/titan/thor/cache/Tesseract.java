package com.titan.thor.cache;

import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;
import redis.clients.jedis.Jedis;

import java.util.*;

@Log
public class Tesseract implements InfinityStone {

    private String underlier;
    private String bookType;

    private final Jedis jedis;

    // These 4 must always be affected together
    private double amountAvailable;
    private TreeMap<Double, Long> numberOfSharesPerPrice;
    private Map<Double, Map<Long, Order>> ordersGroupedByPrice;
    private Map<Long, Order> cache;

    // This one may not
    private List<Order> completedOrders;

    public Tesseract(BookType bookType, String underlier) {
        this.underlier = underlier;

        this.amountAvailable = 0;
        this.ordersGroupedByPrice = new HashMap<>();
        this.completedOrders = new ArrayList<>();
        this.cache = new HashMap<>();

        if (bookType == BookType.BID) {
            this.bookType = "bids";
            this.numberOfSharesPerPrice = new TreeMap<>(Collections.reverseOrder());
        } else {
            this.bookType = "asks";
            this.numberOfSharesPerPrice = new TreeMap<>();
        }

        jedis = new Jedis("redis");
        jedis.set("available","0");
        System.out.println("Amount in REDIS (Constructor): " + jedis.get("available"));
    }

    // underlier_book_variable_{firstLevelKey}_{secondLevelKey}  {} = Optional

    // Amount Available
    // spx:bids:aa (Double)
    // spx:asks:aa (Double)
    public String generateAmountAvailableKey() {
        return getUnderlier() + ":" + getBookType() + ":" + "aa";
    }

    // Number of Shares Per Price
    // spx:bids:spp:100.0 (Long)
    // spx:asks:spp:100.0 (Long)
    public String generateSharesPerPriceKey(double price) {
        return getUnderlier() + ":" + getBookType() + ":spp:" + price;
    }

    // Orders Grouped By Price
    // spx:bids:ogp:100.0:42 (OrderView)
    // spx:asks:ogp:100.0:42 (OrderView)
    public String generateOrdersGroupedByPriceKey(Order order) {
        return getUnderlier() + ":" + getBookType() + ":ogp:" + order.getPrice() + ":" + order.getId();
    }

    // Cache
    // spx:bids:cache:42 (OrderView)
    // spx:asks:cache:42 (OrderView)
    public String generateCacheKey(Long orderID) {
        return getUnderlier() + ":" + getBookType() + ":cache:" + orderID;
    }

    // Completed Orders
    // spx:bids:co (List<OrderView>)
    public String generateCompletedOrdersKey() {
        return getUnderlier() + ":" + getBookType() + ":" + "co";
    }

    public Map<String, String> orderViewToMap(Order order) {
        Map<String, String> map = new HashMap<>();
        map.put("id", order.getId().toString());
        map.put("userID", order.getUserID());
        map.put("symbol", order.getSymbol());
        map.put("quantity", order.getQuantity().toString());
        map.put("price", order.getPrice().toString());
        map.put("side", order.getSide());
        map.put("quantityRemaining", order.getQuantityRemaining().toString());
        return map;
    }

    @Override
    public void add(Order order) {
        log.info("Starting to add order to REDIS: " + order.toString());
        long id = order.getId();
        double price = order.getPrice();
        long quantity = order.getQuantity();

        if (jedis.exists(generateCacheKey(id))) {
            log.info("Order already exists. Do not use add -- use update");
            return;
        }

        if (jedis.exists(generateOrdersGroupedByPriceKey(order))) {
            log.info("Order already exists. Do not use add -- use update");
            return;
        }

        jedis.hmset(generateCacheKey(id), orderViewToMap(order));
        jedis.incrBy(generateSharesPerPriceKey(price), quantity);
        jedis.hmset(generateOrdersGroupedByPriceKey(order), orderViewToMap(order));
        jedis.incrBy(generateAmountAvailableKey(), quantity);
        log.info("Finished adding order to REDIS: " + order.toString());
    }

    @Override
    public Order remove(long orderID) {
        if (!jedis.exists(generateCacheKey(orderID))) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

        Order order = find(orderID);

        double price = order.getPrice();
        long quantityToRemove = order.getQuantityRemaining();

        order.setQuantityRemaining(0L);

//        cache.remove(orderID);
        log.info("About to delete from cache: " + generateCacheKey(orderID));
        jedis.del(generateCacheKey(orderID));

//        ordersGroupedByPrice.get(price).remove(orderID);
        log.info("About to delete from OGP: " + generateOrdersGroupedByPriceKey(order));
        jedis.del(generateOrdersGroupedByPriceKey(order));

//        numberOfSharesPerPrice.put(price, numberOfSharesPerPrice.get(price) - quantityToRemove);
        log.info("Adjusting quantity in SPP: " + generateOrdersGroupedByPriceKey(order));
        jedis.incrBy(generateSharesPerPriceKey(price), quantityToRemove);

//        amountAvailable -= quantityToRemove;
        log.info("Adjusting amount available: " + generateAmountAvailableKey());
        jedis.incrBy(generateAmountAvailableKey(), quantityToRemove * -1L);

//        completedOrders.add(order);
        log.info("Adding a new order to completed orders: " + generateCompletedOrdersKey());
        jedis.rpush(generateCompletedOrdersKey(), String.valueOf(orderID));

        return order;
    }

    @Override
    public Order find(long orderID) {
        if (!jedis.exists(generateCacheKey(orderID))) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

//      double price = cache.get(orderID).getPrice();
        log.info("Trying to get the 'price' field of: " + generateCacheKey(orderID));
        String priceAsString = jedis.hget(generateCacheKey(orderID), "price");
        double price = Double.parseDouble(priceAsString);

        /*
        TODO: Have to figure this part out
         */
//        Order output = ordersGroupedByPrice.get(price).get(orderID);
//        jedis.hget(generateOrdersGroupedByPriceKey(order), orderViewToMap(order));

//        return output;
        return null;
    }

    /*
    TODO: Update's logic needs to change. You should not be able to update the symbol, original quantity. It should just accept orderID and quantity changed.
        These orders do not have lifecycle events attached to them, so they should not have the ability to change their key economics...
     */
    @Override
    public void update(long orderID, long quantityChanged) {
        Order cacheOrder = find(orderID);

//        double cacheOrderPrice = newOrder.getPrice();
//
//        numberOfSharesPerPrice.put(cacheOrderPrice, numberOfSharesPerPrice.get(cacheOrderPrice) - quantityChanged);
//        ordersGroupedByPrice.get(cacheOrderPrice).put(cacheOrder.getId(), newOrder);
//        cache.put(cacheOrder.getId(), newOrder);
//        amountAvailable -= quantityChanged;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public String getUnderlier() {
        return underlier;
    }

    public void setUnderlier(String underlier) {
        this.underlier = underlier;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    public void setAmountAvailable(double amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public TreeMap<Double, Long> getNumberOfSharesPerPrice() {
        return numberOfSharesPerPrice;
    }

    public void setNumberOfSharesPerPrice(TreeMap<Double, Long> numberOfSharesPerPrice) {
        this.numberOfSharesPerPrice = numberOfSharesPerPrice;
    }

    public Map<Double, Map<Long, Order>> getOrdersGroupedByPrice() {
        return ordersGroupedByPrice;
    }

    public void setOrdersGroupedByPrice(Map<Double, Map<Long, Order>> ordersGroupedByPrice) {
        this.ordersGroupedByPrice = ordersGroupedByPrice;
    }

    public Map<Long, Order> getCache() {
        return cache;
    }

    public void setCache(Map<Long, Order> cache) {
        this.cache = cache;
    }

    public List<Order> getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(List<Order> completedOrders) {
        this.completedOrders = completedOrders;
    }

}
