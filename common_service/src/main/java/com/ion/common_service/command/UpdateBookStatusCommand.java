package com.ion.common_service.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookStatusCommand {

    private String bookId;

    private Boolean isReady;
    private String employeeId;
    private String borrowingId;

}
