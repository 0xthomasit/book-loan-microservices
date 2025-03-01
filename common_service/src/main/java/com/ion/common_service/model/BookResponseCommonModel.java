package com.ion.common_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseCommonModel {

    private String id;
    private String name;
    private String author;
    private Boolean isReady;

}