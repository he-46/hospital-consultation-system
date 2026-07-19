package org.example.back.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.back.common.PageResult;
import org.example.back.common.Result;
import org.example.back.common.UserContext;
import org.example.back.dto.follow.FollowAddDTO;
import org.example.back.entity.Follow;
import org.example.back.entity.FollowVO;
import org.example.back.service.FollowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/follows")
public class FollowController {
    @Resource
    private FollowService followService;

    @PostMapping
    public Result<Void> add(@Valid @RequestBody FollowAddDTO dto){
        Long userId = UserContext.getUserId();
        System.out.println("新增userId:"+userId+" type:"+dto.getFollowType());
        followService.addFollow(dto, userId);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        followService.deleteFollow(id);
        return Result.success("取消关注成功", null);
    }

    @GetMapping
    public Result<PageResult<FollowVO>> list(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer followType
    ){
        Long userId = UserContext.getUserId();
        System.out.println("当前登录用户ID：" + userId);
        System.out.println("前端传入followType：" + followType);
        IPage<FollowVO> pageData = followService.getMyFollowPage(page, size, followType, userId);
        System.out.println("查询总条数total：" + pageData.getTotal());
        System.out.println("查到的记录：" + pageData.getRecords());
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
}
