package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.BusinessException;
import org.example.back.common.UserContext;
import org.example.back.common.enums.ConsultStatusEnum;
import org.example.back.common.enums.OrderTypeEnum;
import org.example.back.dto.review.ReviewSubmitDTO;
import org.example.back.entity.Appointment;
import org.example.back.entity.Consult;
import org.example.back.entity.Doctor;
import org.example.back.entity.Review;
import org.example.back.mapper.AppointmentMapper;
import org.example.back.mapper.ConsultMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.ReviewMapper;
import org.example.back.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Resource
    private ConsultMapper consultMapper;
    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private AppointmentMapper appointmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(ReviewSubmitDTO dto) {
        Long userId = UserContext.getUserId();
        Integer orderType = dto.getOrderType();
        Long orderId = dto.getOrderId();
        Long doctorId = dto.getDoctorId();

        // 1. 校验订单归属与状态
        if(orderType == OrderTypeEnum.CONSULT.getCode()){
            Consult consult = consultMapper.selectById(orderId);
            if(consult == null || !consult.getUserId().equals(userId)){
                throw new BusinessException(40000,"订单不存在或无权评价");
            }
            if(!consult.getStatus().equals(ConsultStatusEnum.FINISHED.getCode())){
                throw new BusinessException(40000,"仅已完成订单可评价");
            }
        } else if(orderType == OrderTypeEnum.REGISTER.getCode()){
            Appointment appointment = appointmentMapper.selectById(orderId);
            if(appointment == null || !appointment.getUserId().equals(userId)){
                throw new BusinessException(40000,"订单不存在或无权评价");
            }
            if(appointment.getStatus() != 3){
                throw new BusinessException(40000,"仅已完成订单可评价");
            }
        } else {
            throw new BusinessException(40000,"无效的订单类型");
        }

        // 2. 校验该订单是否已评价
        Long exist = baseMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderType, orderType)
                .eq(Review::getOrderId, orderId));
        if(exist > 0){
            throw new BusinessException(40000,"该订单已提交评价，不可重复操作");
        }

        // 3. 保存评价
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderType(orderType);
        review.setOrderId(orderId);
        review.setDoctorId(doctorId);
        review.setRating(dto.getScore());
        review.setContent(dto.getContent());
        baseMapper.insert(review);

        // 4. 重新计算医生平均分
        List<Review> reviewList = baseMapper.selectList(Wrappers.<Review>lambdaQuery()
                .eq(Review::getDoctorId, doctorId));
        double total = 0;
        for(Review r : reviewList) total += r.getRating();
        BigDecimal avg = new BigDecimal(total / reviewList.size()).setScale(2, RoundingMode.HALF_UP);

        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setRating(avg);
        doctorMapper.updateById(doctor);
    }

    @Override
    public IPage<Review> getMyReviewPage(Long page, Long size) {
        Long userId = UserContext.getUserId();
        Page<Review> pageObj = new Page<>(page, size);
        return baseMapper.selectPage(pageObj, Wrappers.<Review>lambdaQuery()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
    }
}