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
import org.example.back.mapper.ConsultMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.mapper.HospitalMapper;
import org.example.back.mapper.PaymentFlowMapper;
import org.example.back.service.ConsultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultMapper, Consult> implements ConsultService {

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private PaymentFlowMapper paymentFlowMapper;

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
        consult.setOrderNo(UUID.randomUUID().toString().replace("-",""));
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
        return baseMapper.selectConsultPage(pageObj, userId, status);
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

        // 更新咨询订单状态为已完成（模拟支付后直接完成）
        consult.setStatus(ConsultStatusEnum.FINISHED.getCode());
        consult.setPayTime(now);
        baseMapper.updateById(consult);

        // 返回支付结果
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", consult.getOrderNo());
        result.put("amount", consult.getAmount());
        result.put("thirdPartyTradeNo", tradeNo);
        result.put("paySuccessTime", now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return result;
    }
}
