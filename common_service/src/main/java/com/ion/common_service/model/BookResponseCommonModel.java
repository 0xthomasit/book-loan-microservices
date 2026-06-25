package com.ion.common_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("isReady")
    private Boolean isReady;

}