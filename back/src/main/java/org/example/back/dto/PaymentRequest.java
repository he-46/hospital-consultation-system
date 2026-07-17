package org.example.back.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {

    private Integer payMethod;

    private BigDecimal amount;
}
