package com.tertir.demo.rest.admin.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationUserModel {

    private String id;
    private String username;
    private String IDBankNumber;
    private String otherBankNumber;
    private Integer randomCode;
    private Integer number5;
    private Integer number4;
    private Integer number3;
    private Integer number2;
    private Integer number1;
    private Double sum;
}
