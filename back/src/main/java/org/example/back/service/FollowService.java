package org.example.back.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.dto.follow.FollowAddDTO;
import org.example.back.entity.Follow;
import org.example.back.entity.FollowVO;

public interface FollowService {
    // 添加关注
    void addFollow(FollowAddDTO dto, Long userId);
    // 取消关注
    void deleteFollow(Long id);
    // 我的关注分页
    IPage<FollowVO> getMyFollowPage(Long page, Long size, Integer type, Long userId);
    // 判断是否已关注
    Boolean checkFollow(Integer type, Long targetId);

    // 获取关注记录ID（用于取消关注）
    Long getFollowRecordId(Integer followType, Long followId);
}