package com.titan.thor.cache;

import com.titan.thor.model.Order;

public interface InfinityStone {

    /**
     * Adds element into cache
     * @param order -- Order to be added
     */
    void add(Order order);

    /**
     * Removes element from cache
     *
     * Throws exception if not found
     * @param orderID -- OrderID to be removed
     * @return
     */
    Order remove(long orderID);

    /**
     * Updates element from cache with the details of input order
     *
     * Input Order's ID MUST match an existing order in the cache
     *      - If condition not met, exception is thrown
     * @param orderID -- OrderID of the order which needs to be updated
     */
    void update(long orderID, long quantityToChange);

    /**
     * Retrieves order from cache (does not add/remove/update)
     *
     * Throws exception if orderID not found
     * @param orderID -- OrderID to be found in cache
     * @return
     */
    Order find(long orderID);

    /**
     * Cancels order from database. Unlike remove, which sets quantity to 0,
     * it simply removes all the values from spp, ogp, cache and moves it to
     * completed orders
     *
     * Throws exception if orderID not found
     * @param orderID -- OrderID to be found in cache
     * @return
     */
    Order cancel(long orderID);

}
