package org.example.back.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.back.entity.FamilyMember;
import java.util.List;

public interface FamilyMemberService extends IService<FamilyMember> {

    List<FamilyMember> getList(Long userId);

    FamilyMember add(FamilyMember member, Long userId);

    FamilyMember update(FamilyMember member, Long userId);

    void delete(Long id, Long userId);
}
