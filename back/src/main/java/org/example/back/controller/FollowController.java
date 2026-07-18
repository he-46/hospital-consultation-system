package org.example.back.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.common.PageResult;
import org.example.back.common.Result;
import org.example.back.dto.follow.FollowAddDTO;
import org.example.back.entity.Follow;
import org.example.back.service.FollowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/follows")
public class FollowController {
    @Resource
    private FollowService followService;

    @PostMapping
    public Result<Void> add(@Validated @RequestBody FollowAddDTO dto){
        followService.addFollow(dto);
        return Result.success("关注成功",null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        followService.deleteFollow(id);
        return Result.success("取消关注成功",null);
    }

    @GetMapping
    public Result<PageResult<Follow>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer followType
    ){
        IPage<Follow> pageData = followService.getMyFollowPage(page, size, followType);
        return Result.success(PageResult.of(pageData));
    }

    @GetMapping("/check")
    public Result<Boolean> check(
            @RequestParam Integer followType,
            @RequestParam Long followId
    ){
        Boolean flag = followService.checkFollow(followType, followId);
        return Result.success(flag);
    }

    @GetMapping("/record-id")
    public Result<Long> getFollowRecordId(
            @RequestParam Integer followType,
            @RequestParam Long followId
    ){
        Long id = followService.getFollowRecordId(followType, followId);
        return Result.success(id);
    }
}