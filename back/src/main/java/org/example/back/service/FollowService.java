package org.example.back.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.dto.follow.FollowAddDTO;
import org.example.back.entity.Follow;

public interface FollowService {
    // 添加关注
    void addFollow(FollowAddDTO dto);
    // 取消关注
    void deleteFollow(Long id);
    // 我的关注分页
    IPage<Follow> getMyFollowPage(Long page, Long size, Integer type);
    // 判断是否已关注
    Boolean checkFollow(Integer type, Long targetId);
}