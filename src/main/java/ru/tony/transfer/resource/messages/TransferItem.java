package ru.tony.transfer.resource.messages;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItem  {

    private Long transactionId;
    private Date transactionTime;
}
