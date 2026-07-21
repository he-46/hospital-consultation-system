package org.example.back.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowVO {
    // 关注表字段
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long userId;
    private Integer followType;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long followId;
    private LocalDateTime createTime;

    // 医院字段（followType=1时填充）
    private String hospitalName;
    private String hospitalLevel;
    private String hospitalImage;
    private String hospitalIntro;

    // 医生字段（followType=2时填充）
    private String doctorName;
    private String doctorTitle;
    private String doctorDepartment;
    private String doctorAvatar;
    private String doctorIntro;

    // 疾病字段（followType=3时填充）
    private String diseaseName;
    private String diseaseAlias;
    private String diseaseDesc;

    // ---- 便捷获取方法 ----

    /** 关注对象名称 */
    public String getTargetName() {
        if (followType == null) return "";
        switch (followType) {
            case 1: return hospitalName != null ? hospitalName : "";
            case 2: return doctorName != null ? doctorName : "";
            case 3: return diseaseName != null ? diseaseName : "";
            default: return "";
        }
    }

    /** 关注对象图片 */
    public String getTargetImage() {
        if (followType == null) return "";
        switch (followType) {
            case 1: return hospitalImage != null ? hospitalImage : "";
            case 2: return doctorAvatar != null ? doctorAvatar : "";
            default: return "";
        }
    }

    /** 关注对象简介 */
    public String getTargetIntro() {
        if (followType == null) return "";
        switch (followType) {
            case 1: return hospitalIntro != null ? hospitalIntro : "";
            case 2: return doctorIntro != null ? doctorIntro : "";
            case 3: return diseaseDesc != null ? diseaseDesc : "";
            default: return "";
        }
    }
}
