package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.back.entity.FamilyMember;

@Mapper
public interface FamilyMemberMapper extends BaseMapper<FamilyMember> {
}
