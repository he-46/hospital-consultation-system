package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.BusinessException;
import org.example.back.common.UserContext;
import org.example.back.dto.follow.FollowAddDTO;
import org.example.back.entity.Follow;
import org.example.back.entity.Disease;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.mapper.FollowMapper;
import org.example.back.mapper.DiseaseMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.service.FollowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private HospitalMapper hospitalMapper;
    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private DiseaseMapper diseaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFollow(FollowAddDTO dto) {
        Long userId = UserContext.getUserId();
        Integer type = dto.getFollowType();
        Long targetId = dto.getFollowId();

        // 判断是否已关注
        Long count = baseMapper.selectCount(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, type)
                .eq(Follow::getFollowId, targetId));
        if(count > 0){
            throw new BusinessException(40000,"已关注，不可重复操作");
        }

        // 校验目标存在
        switch (type){
            case 1:
                if(hospitalMapper.selectById(targetId) == null) throw new BusinessException(40000,"医院不存在");
                break;
            case 2:
                if(doctorMapper.selectById(targetId) == null) throw new BusinessException(40000,"医生不存在");
                break;
            case 3:
                if(diseaseMapper.selectById(targetId) == null) throw new BusinessException(40000,"疾病不存在");
                break;
            default:
                throw new BusinessException(40000,"关注类型非法");
        }

        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowType(type);
        follow.setFollowId(targetId);
        baseMapper.insert(follow);

        // 原子更新关注数 +1
        switch (type){
            case 1:
                hospitalMapper.update(null, Wrappers.<Hospital>lambdaUpdate()
                        .eq(Hospital::getId, targetId)
                        .setSql("follow_count = follow_count + 1"));
                break;
            case 2:
                doctorMapper.update(null, Wrappers.<Doctor>lambdaUpdate()
                        .eq(Doctor::getId, targetId)
                        .setSql("follow_count = follow_count + 1"));
                break;
            case 3:
                diseaseMapper.update(null, Wrappers.<Disease>lambdaUpdate()
                        .eq(Disease::getId, targetId)
                        .setSql("follow_count = follow_count + 1"));
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFollow(Long id) {
        Long userId = UserContext.getUserId();
        Follow follow = baseMapper.selectById(id);
        if(follow == null || !follow.getUserId().equals(userId)){
            throw new BusinessException(40000,"记录不存在");
        }
        Integer type = follow.getFollowType();
        Long targetId = follow.getFollowId();
        baseMapper.deleteById(id);

        // 原子更新关注数 -1
        switch (type){
            case 1:
                hospitalMapper.update(null, Wrappers.<Hospital>lambdaUpdate()
                        .eq(Hospital::getId, targetId)
                        .setSql("follow_count = follow_count - 1"));
                break;
            case 2:
                doctorMapper.update(null, Wrappers.<Doctor>lambdaUpdate()
                        .eq(Doctor::getId, targetId)
                        .setSql("follow_count = follow_count - 1"));
                break;
            case 3:
                diseaseMapper.update(null, Wrappers.<Disease>lambdaUpdate()
                        .eq(Disease::getId, targetId)
                        .setSql("follow_count = follow_count - 1"));
                break;
        }
    }

    @Override
    public IPage<Follow> getMyFollowPage(Long page, Long size, Integer followType) {
        Long userId = UserContext.getUserId();
        Page<Follow> pageObj = new Page<>(page, size);
        return baseMapper.selectPage(pageObj, Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(followType != null, Follow::getFollowType, followType)
                .orderByDesc(Follow::getCreateTime));
    }

    @Override
    public Boolean checkFollow(Integer followType, Long followId) {
        Long userId = UserContext.getUserId();
        Long count = baseMapper.selectCount(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, followType)
                .eq(Follow::getFollowId, followId));
        return count > 0;
    }

    @Override
    public Long getFollowRecordId(Integer followType, Long followId) {
        Long userId = UserContext.getUserId();
        Follow follow = baseMapper.selectOne(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowType, followType)
                .eq(Follow::getFollowId, followId)
                .last("LIMIT 1"));
        return follow != null ? follow.getId() : 0L;
    }
}