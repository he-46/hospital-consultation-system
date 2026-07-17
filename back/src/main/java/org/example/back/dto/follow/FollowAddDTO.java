package org.example.back.dto.follow;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class FollowAddDTO {
    @NotNull(message = "关注类型不能为空")
    private Integer followType;
    @NotNull(message = "目标id不能为空")
    private Long followId;
}