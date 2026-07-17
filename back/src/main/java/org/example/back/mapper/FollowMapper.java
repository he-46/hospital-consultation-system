package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.back.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}