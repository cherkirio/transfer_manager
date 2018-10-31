package service;

import java.math.BigDecimal;

/**
 * Created by kirio on 31.10.2018.
 */
public interface ITransferService {

    void transfer(long from_id, long to_id, BigDecimal amount);
}
