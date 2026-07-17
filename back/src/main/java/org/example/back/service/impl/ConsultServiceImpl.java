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
import org.example.back.mapper.ConsultMapper;
import org.example.back.mapper.DoctorMapper;
import org.example.back.service.ConsultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultMapper, Consult> implements ConsultService {

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Consult createConsult(CreateConsultDTO dto) {
        Long userId = UserContext.getUserId();
        Doctor doctor = doctorMapper.selectById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException(40000, "医生不存在");
        }
        BigDecimal amount = doctor.getPrice();

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
        return consult;
    }

    @Override
    public IPage<Consult> getMyConsultPage(Long page, Long size, Integer status) {
        Long userId = UserContext.getUserId();
        Page<Consult> pageObj = new Page<>(page, size);
        return baseMapper.selectPage(pageObj, Wrappers.<Consult>lambdaQuery()
                .eq(Consult::getUserId, userId)
                .eq(status != null, Consult::getStatus, status)
                .orderByDesc(Consult::getCreateTime));
    }

    @Override
    public Consult getConsultDetail(Long id) {
        Long userId = UserContext.getUserId();
        Consult consult = baseMapper.selectById(id);
        if (consult == null) {
            throw new BusinessException(40000, "订单不存在");
        }
        if (!consult.getUserId().equals(userId)) {
            throw new BusinessException(40000, "无权查看该订单");
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
    public String payConsult(Long id, PayConsultDTO dto) {
        Consult consult = getConsultDetail(id);
        if (!consult.getStatus().equals(ConsultStatusEnum.PENDING_PAY.getCode())) {
            throw new BusinessException(40000, "订单状态不支持支付");
        }
        consult.setStatus(ConsultStatusEnum.PAID.getCode());
        consult.setPayTime(LocalDateTime.now());
        baseMapper.updateById(consult);
        return "模拟支付单号:" + consult.getOrderNo();
    }
}