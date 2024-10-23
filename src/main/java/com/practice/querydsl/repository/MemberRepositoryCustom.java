package com.practice.querydsl.repository;

import com.practice.querydsl.dto.MemberSearchCondition;
import com.practice.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageCountOptimization(MemberSearchCondition condition, Pageable pageable);


}
