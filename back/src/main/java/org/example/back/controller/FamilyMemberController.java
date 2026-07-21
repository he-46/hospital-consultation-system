package org.example.back.controller;

import org.example.back.common.Result;
import org.example.back.entity.FamilyMember;
import org.example.back.service.FamilyMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/family-members")
public class FamilyMemberController {

    @Autowired
    private FamilyMemberService familyMemberService;

    @GetMapping
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<FamilyMember> list = familyMemberService.getList(userId);
        return Result.success(list);
    }

    @PostMapping
    public Result<?> add(@RequestBody FamilyMember member, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        FamilyMember saved = familyMemberService.add(member, userId);
        return Result.success(saved);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FamilyMember member, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        member.setId(id);
        FamilyMember updated = familyMemberService.update(member, userId);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        familyMemberService.delete(id, userId);
        return Result.success("删除成功");
    }
}
