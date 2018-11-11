package ru.tony.transfer.model;


import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;
    private String name;
    private String number;
    private BigDecimal balance;
    private Date createTime;
}
