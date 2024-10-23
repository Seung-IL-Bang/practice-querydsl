package com.practice.querydsl.repository;

import com.practice.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>,
        MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {

    /** Spring Data JPA 가 지원해주는 QuerydslPredicateExecutor
     * 한계점: 조인 제약; 묵시적 조인은 되지만, left join이 불가능하다.
     * 클라이언트가 Querydsl에 의존해야 한다. 즉, 서비스 클래스가 Querydsl 이라는 구현 기술에 의존.
     * 복잡한 실무 환경에서 사용하기에는 한계가 명확하다.
     * 참고: Pageable, Sort 모두 지원
     */

    List<Member> findByUsername(String username);

}
