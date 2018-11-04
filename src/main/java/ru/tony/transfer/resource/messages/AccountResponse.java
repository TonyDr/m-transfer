package ru.tony.transfer.resource.messages;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse extends BaseResponse{

    private AccountItem account;

}
