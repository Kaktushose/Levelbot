package de.kaktushose.levelbot.shop.data.items;

import de.kaktushose.levelbot.shop.data.transactions.Transaction;

import javax.persistence.*;

/**
 * This class covers the edge case where a user has normal premium but receives unlimited as well. Unlimited will become
 * the active item and the normal premium will be moved to this table until unlimited gets removed again.
 */
@Entity
@Table(name = "frozen_items")
public class FrozenItem {

    @Id
    private long userId;
    private long startTime;
    private long buyTime;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId")
    private Item item;

    public FrozenItem() {
    }

    public FrozenItem(long userId, long startTime, long buyTime, Item item) {
        this.userId = userId;
        this.startTime = startTime;
        this.buyTime = buyTime;
        this.item = item;
    }

    public static FrozenItem fromTransaction(long userId, Transaction transaction) {
        return new FrozenItem(userId, System.currentTimeMillis(), transaction.getBuyTime(), transaction.getItem());
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(long buyTime) {
        this.buyTime = buyTime;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
