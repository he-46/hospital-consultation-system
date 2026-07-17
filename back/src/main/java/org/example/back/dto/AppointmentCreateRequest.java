package org.example.back.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AppointmentCreateRequest {

    private Long doctorId;

    private Long hospitalId;

    private Long scheduleId;

    private String patientName;

    private String patientPhone;

    private String patientIdCard;

    private Integer patientGender;

    private Integer patientAge;

    private String appointmentDate;

    private String appointmentTime;

    private String diseaseDesc;

    private BigDecimal amount;
}
