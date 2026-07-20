package org.example.back.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.back.common.BusinessException;
import org.example.back.common.UserContext;
import org.example.back.common.enums.ConsultStatusEnum;
import org.example.back.dto.consult.CreateConsultDTO;
import org.example.back.dto.consult.PayConsultDTO;
import org.example.back.entity.Consult;
import org.example.back.entity.Doctor;
import org.example.back.entity.Hospital;
import org.example.back.entity.PaymentFlow;
import org.example.back.entity.Review;
import org.example.back.mapper.ConsultMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.mapper.PaymentFlowMapper;
import org.example.back.mapper.ReviewMapper;
import org.example.back.service.ConsultService;
import org.example.back.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultMapper, Consult> implements ConsultService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private PaymentFlowMapper paymentFlowMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private ReviewMapper reviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Consult createConsult(CreateConsultDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(30001, "请先登录");
        }
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(40000, "医生不存在");
        }
        // 金额 = 医生咨询价格
        BigDecimal amount = doctor.getPrice() != null ? doctor.getPrice() : BigDecimal.ZERO;

        Consult consult = new Consult();
        consult.setOrderNo(generateOrderNo());
        consult.setUserId(userId);
        consult.setDoctorId(dto.getDoctorId());
        consult.setPatientName(dto.getPatientName());
        consult.setPatientPhone(dto.getPatientPhone());
        consult.setDiseaseDesc(dto.getDiseaseDesc());
        consult.setAppointmentTime(dto.getAppointmentTime());
        consult.setDuration(30);
        consult.setAmount(amount);
        consult.setStatus(ConsultStatusEnum.PENDING_PAY.getCode());
        consult.setCreateTime(LocalDateTime.now());
        baseMapper.insert(consult);
        System.out.println("=== 创建咨询成功, ID=" + consult.getId() + ", orderNo=" + consult.getOrderNo());
        return consult;
    }

    @Override
    public IPage<Consult> getMyConsultPage(Long page, Long size, Integer status) {
        Long userId = UserContext.getUserId();
        Page<Consult> pageObj = new Page<>(page, size);
        IPage<Consult> result = baseMapper.selectConsultPage(pageObj, userId, status);

        // 批量查询已评价订单，设置 hasReview 标记
        List<Consult> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> consultIds = records.stream()
                    .map(Consult::getId).collect(Collectors.toList());
            Set<Long> reviewedIds = new HashSet<>();
            List<Review> reviews = reviewMapper.selectList(
                    Wrappers.<Review>lambdaQuery()
                            .eq(Review::getOrderType, 2)
                            .in(Review::getOrderId, consultIds));
            for (Review r : reviews) {
                reviewedIds.add(r.getOrderId());
            }
            for (Consult c : records) {
                c.setHasReview(reviewedIds.contains(c.getId()));
            }
        }
        return result;
    }

    @Override
    public Consult getConsultDetail(Long id) {
        Long userId = UserContext.getUserId();
        System.out.println("=== 查询咨询详情, 收到的ID=" + id);
        Consult consult = baseMapper.selectById(id);
        if (consult == null) {
            throw new BusinessException(40000, "订单不存在");
        }
        if (!consult.getUserId().equals(userId)) {
            throw new BusinessException(40000, "无权查看该订单");
        }
        // 补充医生信息
        if (consult.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(consult.getDoctorId());
            if (doctor != null) {
                consult.setDoctorName(doctor.getName());
                consult.setDoctorTitle(doctor.getTitle());
                // 补充医院名称
                if (doctor.getHospitalId() != null) {
                    Hospital hospital = hospitalMapper.selectById(doctor.getHospitalId());
                    if (hospital != null) {
                        consult.setHospitalName(hospital.getName());
                    }
                }
            }
        }
        return consult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelConsult(Long id) {
        Long userId = UserContext.getUserId();
        Consult consult = baseMapper.selectById(id);
        if (consult == null) {
            throw new BusinessException(40000, "订单不存在");
        }
        if (!consult.getUserId().equals(userId)) {
            throw new BusinessException(40000, "无权操作");
        }
        Integer status = consult.getStatus();
        if (!status.equals(ConsultStatusEnum.PENDING_PAY.getCode())
                && !status.equals(ConsultStatusEnum.PAID.getCode())) {
            throw new BusinessException(40000, "当前订单不可取消");
        }
        consult.setStatus(ConsultStatusEnum.CANCEL.getCode());
        baseMapper.updateById(consult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payConsult(Long id, PayConsultDTO dto) {
        Long userId = UserContext.getUserId();
        Consult consult = baseMapper.selectById(id);
        if (consult == null) {
            throw new BusinessException(40000, "订单不存在");
        }
        if (!consult.getUserId().equals(userId)) {
            throw new BusinessException(40000, "无权操作");
        }
        if (!consult.getStatus().equals(ConsultStatusEnum.PENDING_PAY.getCode())) {
            throw new BusinessException(40000, "订单状态不支持支付");
        }

        // 检查是否已支付
        Long existCount = paymentFlowMapper.selectCount(Wrappers.<PaymentFlow>lambdaQuery()
                .eq(PaymentFlow::getBusinessOrderNo, consult.getOrderNo())
                .eq(PaymentFlow::getPayStatus, 1));
        if (existCount > 0) {
            throw new BusinessException(40000, "该订单已支付");
        }

        LocalDateTime now = LocalDateTime.now();
        String tradeNo = "SIM" + now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 创建已支付流水
        PaymentFlow flow = new PaymentFlow();
        flow.setBusinessOrderNo(consult.getOrderNo());
        flow.setBusinessType(2);
        flow.setPayMethod(dto.getPayType() != null ? dto.getPayType() : 1);
        flow.setActualAmount(consult.getAmount());
        flow.setPayStatus(1); // 直接已支付
        flow.setThirdPartyTradeNo(tradeNo);
        flow.setPaySuccessTime(now);
        flow.setCreateTime(now);
        paymentFlowMapper.insert(flow);

        // 更新咨询订单状态为已支付（用户需手动确认完成）
        consult.setStatus(ConsultStatusEnum.PAID.getCode());
        consult.setPayTime(now);
        baseMapper.updateById(consult);

        try {
            messageService.sendSystemMsg(consult.getUserId(),
                "电话咨询预约成功",
                "您已成功预约电话咨询。咨询人：" + consult.getPatientName()
                    + "，咨询费：¥" + consult.getAmount()
                    + "，订单号：" + consult.getOrderNo()
                    + "。请保持电话畅通。");
        } catch (Exception e) {
            // 消息发送失败不影响支付流程
        }

        // 返回支付结果
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", consult.getOrderNo());
        result.put("amount", consult.getAmount());
        result.put("thirdPartyTradeNo", tradeNo);
        result.put("paySuccessTime", now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }

    private String generateOrderNo() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return "CONSUL" + System.currentTimeMillis() + sb;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmConsult(Long id) {
        Long userId = UserContext.getUserId();
        Consult consult = baseMapper.selectById(id);
        if (consult == null) {
            throw new BusinessException(40000, "订单不存在");
        }
        if (!consult.getUserId().equals(userId)) {
            throw new BusinessException(40000, "无权操作");
        }
        if (!consult.getStatus().equals(ConsultStatusEnum.PAID.getCode())) {
            throw new BusinessException(40000, "当前状态不可确认完成");
        }
        consult.setStatus(ConsultStatusEnum.FINISHED.getCode());
        consult.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(consult);
    }
}
