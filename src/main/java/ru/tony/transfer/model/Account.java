package ru.tony.transfer.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Account {

    private Long id;
    private String name;
    private String number;
    private BigDecimal balance;
    private Date createDate;
}
