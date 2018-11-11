### This is app for money transfer between accounts 

#### RestAPI of the app

| URL                    |Method| Description   | 
| ---------------------- |:-----|:--------------| 
| /accounts              |POST  | Create account and return information about created record | 
| /accounts/{id}         |GET   | Return information about account with given id     | 
| /accounts              |GET   | Return list of all existed accounts      | 
| /accounts/transfer     |POST  | Transfer money between accounts      |
| /accounts/{id}/history |GET   | Return information about transaction for given account id      |

#### Descriptions of the services request and response

##### /accounts (POST)
Creates account with given name and amount of money
###### Request

```
{ 
	"name": ${account_name},
	"balance": ${balance_of_account}
}
```

###### Response

```
{
    "status": "OK",
    "account": {
        "id": ${id_of_account},
        "number": ${generated_number},
        "createTime": ${creation_date},
        "name": ${account_name},
        "balance": ${balance_of_account}
    }
}
```

##### /accounts/transfer
Transfer money between accounts
###### Request

```
{
	"from": ${number_of_account_from},
	"to": ${number_of_account_to},
	"amount": ${amount_of_money_to_transfer}
}
```

###### Response

```
{
    "status": "OK",
    "info": {
        "transactionId": ${id_of_transaction},
        "transactionTime": ${transaction_time}
    }
}
```

##### /accounts/{id}/history
Return list of transaction for given account id
###### Request
id - is identifier of account

###### Response

```
{
    "status": "OK",
    "items": [
        {
            "transactionId": ${id_of_transaction},
            "type": ${type_of_operation}, // WITHDRAWAL/DEPOSIT
            "amount": ${amount_of_money},
            "toNumber": ${recipient_account_number}, //optional
            "fromNumber": ${sender_account_number}, //optional
            "transactionTime": ${transaction time}
        }
    ]
}
```


##### /accounts (GET)
Return list of all accounts

###### Response

```
{
    "status": "OK",
    "accounts": [
        {
            "id": ${account_id},
            "number": ${account_number},
            "createTime": ${creation_date},
            "name": ${account_name},
            "balance": ${current_account_balance}
        }
    ]
}
```

##### /accounts/{id} (GET)

Return information about account with given id

###### Response

```
{
    "status": "OK",
    "account": [
        {
            "id": ${account_id},
            "number": ${account_number},
            "createTime": ${creation_date},
            "name": ${account_name},
            "balance": ${current_account_balance}
        }
    ]
}
```
