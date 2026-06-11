package com.fooddelivery.common.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class FieldErrorDetails {
    private String field;
    private String message;

}
