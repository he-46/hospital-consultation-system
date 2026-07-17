package org.example.back.dto.consult;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CreateConsultDTO {
    @NotNull(message = "医生id不能为空")
    private Long doctorId;
    @NotBlank(message = "咨询人姓名不能为空")
    private String patientName;
    private String patientPhone;
    @NotBlank(message = "病情描述不能为空")
    private String diseaseDesc;
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;
}