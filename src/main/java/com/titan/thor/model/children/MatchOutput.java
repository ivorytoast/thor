package com.titan.thor.model.children;

import com.titan.thor.model.Order;

import java.util.Set;

public class MatchOutput {
    public boolean fullyMatch;
    public Order modifiedOrder;
    public Set<Long> existingOrdersToRemove;
    public Set<Long> existingOrdersToUpdate;

    public MatchOutput(boolean fullyMatch, Order modifiedOrder, Set<Long> existingOrdersToRemove, Set<Long> existingOrdersToUpdate) {
        this.fullyMatch = fullyMatch;
        this.modifiedOrder = modifiedOrder;
        this.existingOrdersToRemove = existingOrdersToRemove;
        this.existingOrdersToUpdate = existingOrdersToUpdate;
    }

    public boolean isFullyMatch() {
        return fullyMatch;
    }

    public void setFullyMatch(boolean fullyMatch) {
        this.fullyMatch = fullyMatch;
    }

    public Order getModifiedOrder() {
        return modifiedOrder;
    }

    public void setModifiedOrder(Order modifiedOrder) {
        this.modifiedOrder = modifiedOrder;
    }

    public Set<Long> getExistingOrdersToRemove() {
        return existingOrdersToRemove;
    }

    public void setExistingOrdersToRemove(Set<Long> existingOrdersToRemove) {
        this.existingOrdersToRemove = existingOrdersToRemove;
    }

    public Set<Long> getExistingOrdersToUpdate() {
        return existingOrdersToUpdate;
    }

    public void setExistingOrdersToUpdate(Set<Long> existingOrdersToUpdate) {
        this.existingOrdersToUpdate = existingOrdersToUpdate;
    }

}