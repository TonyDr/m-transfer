package ru.tony.transfer.resource.messages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AccountListResponse extends BaseResponse {

    private List<AccountItem> accounts;

    public AccountListResponse(List<AccountItem> list) {
        this.accounts = list;
    }
}
