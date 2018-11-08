package ru.tony.transfer.resource.messages;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferHistoryItem {

    private Long transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private String toNumber;
    private String fromNumber;
    private Date transactionTime;
}
