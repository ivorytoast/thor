package com.titan.thor.model.children;

import java.util.Set;

public class MatchOutput {
    public boolean fullyMatch;
    public Set<Long> existingOrdersToRemove;
    public Set<Long> existingOrdersToUpdate;

    public MatchOutput(boolean fullyMatch, Set<Long> existingOrdersToRemove, Set<Long> existingOrdersToUpdate) {
        this.fullyMatch = fullyMatch;
        this.existingOrdersToRemove = existingOrdersToRemove;
        this.existingOrdersToUpdate = existingOrdersToUpdate;
    }

    public boolean isFullyMatch() {
        return fullyMatch;
    }

    public void setFullyMatch(boolean fullyMatch) {
        this.fullyMatch = fullyMatch;
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