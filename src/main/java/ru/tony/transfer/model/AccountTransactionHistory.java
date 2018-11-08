package ru.tony.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class AccountTransactionHistory {

    private Long id;
    private Long from;
    private String fromNumber;
    private Long to;
    private String toNumber;
    private Date transactionTime;
    private BigDecimal amount;
}
