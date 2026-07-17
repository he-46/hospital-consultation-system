package org.example.back.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.common.PageResult;
import org.example.back.common.Result;
import org.example.back.dto.consult.CreateConsultDTO;
import org.example.back.dto.consult.PayConsultDTO;
import org.example.back.entity.Consult;
import org.example.back.service.ConsultService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/consults")
public class ConsultController {
    @Resource
    private ConsultService consultService;

    @PostMapping
    public Result<Consult> create(@Validated @RequestBody CreateConsultDTO dto){
        Consult data = consultService.createConsult(dto);
        return Result.success(data);
    }

    @GetMapping
    public Result<PageResult<Consult>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status
    ){
        IPage<Consult> pageData = consultService.getMyConsultPage(page, size, status);
        return Result.success(PageResult.of(pageData));
    }

    @GetMapping("/{id}")
    public Result<Consult> detail(@PathVariable Long id){
        Consult data = consultService.getConsultDetail(id);
        return Result.success(data);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id){
        consultService.cancelConsult(id);
        return Result.success("取消成功",null);
    }

    @PostMapping("/{id}/pay")
    public Result<String> pay(@PathVariable Long id, @Validated @RequestBody PayConsultDTO dto){
        String payUrl = consultService.payConsult(id, dto);
        return Result.success(payUrl);
    }
}