package ru.tony.transfer.resource.messages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {

    ResponseStatus status;

    public BaseResponse(){
        this(ResponseStatus.OK);
    }

    public BaseResponse(ResponseStatus status) {
        this.status = status;
    }
}
