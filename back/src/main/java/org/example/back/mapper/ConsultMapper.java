package org.example.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.example.back.entity.Consult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsultMapper extends BaseMapper<Consult> {
    // 联查医生和医院信息的分页查询
    IPage<Consult> selectConsultPage(IPage<Consult> page,
                                     @Param("userId") Long userId,
                                     @Param("status") Integer status);

    // 联查医生和医院信息的单条查询
    Consult selectConsultById(@Param("id") Long id);
}
