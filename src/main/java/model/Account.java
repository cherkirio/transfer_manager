package model;

import java.math.BigDecimal;

/**
 * Created by kirio on 29.10.2018.
 */
public class Account {
    private long id;
    private BigDecimal balance;

    public Account() {
    }

    public Account(long id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
