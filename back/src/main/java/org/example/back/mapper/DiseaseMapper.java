package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.back.entity.Disease;

@Mapper
public interface DiseaseMapper extends BaseMapper<Disease> {
}
