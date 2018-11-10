package ru.tony.transfer.resource.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class TransferHistoryItem {

    private Long transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private String toNumber;
    private String fromNumber;
    private Date transactionTime;
}
