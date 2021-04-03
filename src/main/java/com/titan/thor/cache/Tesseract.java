package com.titan.thor.cache;

import com.titan.thor.model.Order;
import com.titan.thor.model.enums.BookType;
import lombok.extern.java.Log;
import redis.clients.jedis.Jedis;

import java.util.*;

@Log
public class Tesseract implements InfinityStone {

    // underlier_book_variable_{firstLevelKey}_{secondLevelKey}  {} = Optional

    private String underlier;
    private String bookType;

    private final Jedis jedis;

    // These 4 must always be affected together
//    private double amountAvailable;
//    private TreeMap<Double, Long> numberOfSharesPerPrice;
//    private Map<Double, Map<Long, Order>> ordersGroupedByPrice;
//    private Map<Long, Order> cache;

    // This one may not
//    private List<Order> completedOrders;

    /**
     * TODO: Have to make number of shares per price a sorted set in REDIS so I can actually get the best prices
     */
    public Tesseract(BookType bookType, String underlier) {
        this.underlier = underlier;

        if (bookType == BookType.BID) {
            this.bookType = "bids";
        } else {
            this.bookType = "asks";
        }

        jedis = new Jedis("redis");
    }

    // spx:bids:aa (Double)
    public String generateAmountAvailableKey() {
        return getUnderlier() + ":" + getBookType() + ":" + "aa";
    }

    // spx:bids:spp:100.0 (Long)
    public String generateSharesPerPriceKey(double price) {
        return getUnderlier() + ":" + getBookType() + ":spp:" + price;
    }

    // spx:bids:ogp:100.0:42 (OrderView)
    public String generateOrdersGroupedByPriceKey(Order order) {
        return getUnderlier() + ":" + getBookType() + ":ogp:" + order.getPrice() + ":" + order.getId();
    }

    // spx:bids:cache:42 (OrderView)
    public String generateCacheKey(Long orderID) {
        return getUnderlier() + ":" + getBookType() + ":cache:" + orderID;
    }

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

        log.info("Adding order to cache");
        jedis.hmset(generateCacheKey(id), orderViewToMap(order));

        log.info("Adding additional shares to spp");
        jedis.incrBy(generateSharesPerPriceKey(price), quantity);

        log.info("Adding order to ogp");
        jedis.hmset(generateOrdersGroupedByPriceKey(order), orderViewToMap(order));

        log.info("Adding additional amount to amount available");
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

        log.info("Starting to remove order from REDIS: " + order.toString());

        double price = order.getPrice();
        long quantityToRemove = order.getQuantityRemaining();

        order.setQuantityRemaining(0L);

        log.info("About to delete from cache: " + generateCacheKey(orderID));
        jedis.del(generateCacheKey(orderID));

        log.info("About to delete from OGP: " + generateOrdersGroupedByPriceKey(order));
        jedis.del(generateOrdersGroupedByPriceKey(order));

        log.info("Adjusting quantity in SPP: " + generateOrdersGroupedByPriceKey(order));
        jedis.decrBy(generateSharesPerPriceKey(price), quantityToRemove);

        if (jedis.get(generateSharesPerPriceKey(price)).equals("0")) {
            jedis.del(generateSharesPerPriceKey(price));
        }

        log.info("Adjusting amount available: " + generateAmountAvailableKey());
        jedis.decrBy(generateAmountAvailableKey(), quantityToRemove);

        log.info("Adding a new order to completed orders: " + generateCompletedOrdersKey());
        jedis.rpush(generateCompletedOrdersKey(), String.valueOf(orderID));

        log.info("Finished removing order from REDIS: " + order.toString());

        return order;
    }

    @Override
    public Order cancel(long orderID) {
        if (!jedis.exists(generateCacheKey(orderID))) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

        Order order = find(orderID);

        log.info("Starting to remove order from REDIS: " + order.toString());

        double price = order.getPrice();
        long quantityToRemove = order.getQuantityRemaining();

        log.info("About to delete from cache: " + generateCacheKey(orderID));
        jedis.del(generateCacheKey(orderID));

        log.info("About to delete from OGP: " + generateOrdersGroupedByPriceKey(order));
        jedis.del(generateOrdersGroupedByPriceKey(order));

        log.info("Adjusting quantity in SPP: " + generateOrdersGroupedByPriceKey(order));
        jedis.decrBy(generateSharesPerPriceKey(price), quantityToRemove);

        if (jedis.get(generateSharesPerPriceKey(price)).equals("0")) {
            jedis.del(generateSharesPerPriceKey(price));
        }

        log.info("Adjusting amount available: " + generateAmountAvailableKey());
        jedis.decrBy(generateAmountAvailableKey(), quantityToRemove);

        log.info("Adding a new order to completed orders: " + generateCompletedOrdersKey());
        jedis.rpush(generateCompletedOrdersKey(), String.valueOf(orderID));

        log.info("Finished removing order from REDIS: " + order.toString());

        return order;
    }

    @Override
    public Order find(long orderID) {
        if (!jedis.exists(generateCacheKey(orderID))) {
            log.info("Can't remove something that does not exist!");
            return null;
        }

        log.info("Starting to find the order in REDIS: " + orderID);

        log.info("Trying to get the 'price' field of: " + generateCacheKey(orderID));
        Map<String, String> values = jedis.hgetAll(generateCacheKey(orderID));

        if (values.isEmpty()) {
            log.info("There were no values returned from redis during the find method");
            return null;
        }

        log.info("Finished finding the order in REDIS: " + orderID);

        return convertHashToOrder(values);
    }

    @Override
    public void update(long orderID, long quantityChanged) {
        Order cacheOrder = find(orderID);

        double price = cacheOrder.getPrice();

        log.info("Adjusting quantity in SPP for an update: " + generateOrdersGroupedByPriceKey(cacheOrder));
        jedis.decrBy(generateSharesPerPriceKey(price), quantityChanged);

        if (jedis.get(generateSharesPerPriceKey(price)).equals("0")) {
            jedis.del(generateSharesPerPriceKey(price));
        }

        log.info("Updating order in ogp for key: " + generateOrdersGroupedByPriceKey(cacheOrder));
        jedis.hmset(generateOrdersGroupedByPriceKey(cacheOrder), orderViewToMap(cacheOrder));

        log.info("Updating order in cache for key: " + generateCacheKey(orderID));
        jedis.hmset(generateCacheKey(orderID), orderViewToMap(cacheOrder));

        log.info("Adjusting amount available: " + generateAmountAvailableKey());
        jedis.decrBy(generateAmountAvailableKey(), quantityChanged);
    }

    private Order convertHashToOrder(Map<String, String> values) {
        long id = java.lang.Long.parseLong(values.get("id"));
        long quantity = java.lang.Long.parseLong(values.get("quantity"));
        double price = java.lang.Double.parseDouble(values.get("price"));
        long quantityRemaining = java.lang.Long.parseLong(values.get("quantityRemaining"));

        return new Order(id, values.get("userID"), values.get("symbol"), quantity, price, values.get("side"), quantityRemaining);
    }

    public long getAmountAvailable() {
        String aaString = jedis.get(generateAmountAvailableKey());
        if (aaString.isEmpty()) {
            return 0;
        }
        return Long.parseLong(jedis.get(generateAmountAvailableKey()));
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

}
