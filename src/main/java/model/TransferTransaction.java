package model;

import java.math.BigDecimal;

public class TransferTransaction {


    private long id;
    private long fromAccount;
    private long toAccount;
    private BigDecimal amount;
    private TransferStatus status;

    public TransferTransaction() {
    }

    public TransferTransaction(long id, long from, long to, BigDecimal amount) {
        this.id = id;
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
        this.status = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public long getToAccount() {
        return toAccount;
    }

    public void setToAccount(long toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public TransferTransaction status(TransferStatus status) {
        setStatus(status);
        return this;
    }
}
