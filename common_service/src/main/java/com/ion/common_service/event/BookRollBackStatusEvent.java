package com.ion.common_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRollBackStatusEvent {

    private String bookId;
    private Boolean isReady;
    private String employeeId;
    private String borrowingId;

}
