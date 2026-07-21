package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.entity.FamilyMember;
import org.example.back.mapper.FamilyMemberMapper;
import org.example.back.service.FamilyMemberService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements FamilyMemberService {

    @Override
    public List<FamilyMember> getList(Long userId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getUserId, userId);
        wrapper.orderByDesc(FamilyMember::getIsDefault);
        wrapper.orderByDesc(FamilyMember::getCreateTime);
        return list(wrapper);
    }

    @Override
    public FamilyMember add(FamilyMember member, Long userId) {
        member.setUserId(userId);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        if (member.getIsDefault() == null) {
            member.setIsDefault(0);
        }
        save(member);
        return member;
    }

    @Override
    public FamilyMember update(FamilyMember member, Long userId) {
        FamilyMember existing = getById(member.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该成员");
        }
        member.setUserId(userId);
        member.setUpdateTime(LocalDateTime.now());
        updateById(member);
        return member;
    }

    @Override
    public void delete(Long id, Long userId) {
        FamilyMember existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该成员");
        }
        removeById(id);
    }
}
