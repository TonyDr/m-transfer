package ru.tony.transfer.resource.messages;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank
    private String from;

    @NotBlank
    private String to;

    @NotNull
    @DecimalMin("0.009")
    private BigDecimal amount;
}
