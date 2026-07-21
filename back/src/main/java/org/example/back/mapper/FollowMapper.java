package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.example.back.entity.Follow;
import org.example.back.entity.FollowVO; // 同包导入
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    // 新增联查分页方法
    IPage<FollowVO> selectFollowWithDoctor(
            IPage<FollowVO> page,
            @Param("userId") Long userId,
            @Param("followType") Integer followType
    );
}