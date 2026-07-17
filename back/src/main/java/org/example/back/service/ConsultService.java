package org.example.back.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.dto.consult.CreateConsultDTO;
import org.example.back.dto.consult.PayConsultDTO;
import org.example.back.entity.Consult;

public interface ConsultService {
    // 创建咨询订单
    Consult createConsult(CreateConsultDTO dto);
    // 分页我的咨询
    IPage<Consult> getMyConsultPage(Long page, Long size, Integer status);
    // 订单详情
    Consult getConsultDetail(Long id);
    // 取消咨询
    void cancelConsult(Long id);
    // 发起支付
    String payConsult(Long id, PayConsultDTO dto);
}