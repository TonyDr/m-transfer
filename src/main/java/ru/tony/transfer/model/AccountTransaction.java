package ru.tony.transfer.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class AccountTransaction {

    private Long id;
    private Long from;
    private Long to;
    private Date transactionTime;
    private BigDecimal amount;
}
