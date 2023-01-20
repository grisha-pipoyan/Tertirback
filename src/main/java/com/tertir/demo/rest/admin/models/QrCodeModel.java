package com.tertir.demo.rest.admin.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class QrCodeModel {

    private UUID id;
    private Integer counter;

}
