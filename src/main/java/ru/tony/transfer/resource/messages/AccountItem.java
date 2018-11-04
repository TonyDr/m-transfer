package ru.tony.transfer.resource.messages;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountItem {

    private Long id;
    private String number;
    private Date createDate;
    private String name;
    private BigDecimal balance;
}
