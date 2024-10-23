package com.practice.querydsl;

import com.practice.querydsl.dto.MemberDto;
import com.practice.querydsl.dto.QMemberDto;
import com.practice.querydsl.dto.UserDto;
import com.practice.querydsl.entity.Member;
import com.practice.querydsl.entity.QMember;
import com.practice.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.practice.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslMiddleTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory; // 필드로 주입 받아서 사용 가능. 스레드 세이프

    @BeforeEach
    void setUp() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 튜플 타입의 사용은 레포지토리 계층 내에서만 다루는 것을 권장한다.
     * 그 외 계층에는 DTO 로 감싸서 보내는 것을 권장한다.
     */
    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            System.out.println("username = " + username);

            Integer age = tuple.get(member.age);
            System.out.println("age = " + age);
        }
    }


    @Test
    public void findDtoByJPQL() {

        List<MemberDto> result =
                em.createQuery("select new com.practice.querydsl.dto.MemberDto(m.username, m.age)" +
                                " from Member m", MemberDto.class)
                        .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성(Bean population)
     * 1. 프로퍼티 접근
     * 2. 필드 직접 접근
     * 3. 생성자 사용
     */

    @Test
    public void findDtoBySetter() { // 프로퍼티 접근

        // ===주의=== Projections.bean 사용 시 기본 생성자와 setter 메서드가 필수
        // setter 메서드의 필드명과 일치해야 함 ex: username => setUserName

        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() { // 필드 직접 접근 (리플렉션 사용으로 필드에 바로 접근)

        // ===주의=== 필드명이 일치해야 함. 그렇지 않으면 기본값으로 초기화
        // Alias 를 통해 DTO 필드명에 맞추면 매핑이 제대로 이루어진다.

        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByConstructor() {

        // ===주의=== select 절에 나열된 각 표현식들의 타입이 DTO 에서 매핑되는 필드의 타입과 일치해야 함.
        // select 절에 나열된 표현식들과 일치하는 시그니처 생성자가 있어야 함.

        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDtoByField() { // 필드 직접 접근 (리플렉션 사용으로 필드에 바로 접근)
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), // Alias
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDtoBySubQuery() { // 서브쿼리에 별칭 적용 ExpressionUtils.as

        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), // Alias
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub),
                                "age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDtoByConstructor() {

        // 필드명이 달라도, 매개변수의 순서와 데이터 타입(즉, 생성자 시그니처)만 일치하면 된다.

        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDtoBySetter() {
        List<UserDto> result = queryFactory
                .select(Projections.bean(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection() {
        // Projections.constructor 동작 방식과 동일하게 동작하면서 아래와 같은 장단점이 있다.
        // 장점: 컴파일 시 인수들을 체크하여 생성자가 존재하는지 컴파일 에러를 체크한다.
        // 단점: Q 클래스를 생성해줘야 하며, DTO 클래스가 Querydsl 에 의존하게 된다.

        /**
         * Projections.constructor 와의 차이점
         * Projections.constructor 방식은 일치하는 생성자가 없더라도 컴파일 오류가 발생하지 않는다.
         * 즉, 명시된 인수들에 해당하는 시그니처 생성자가 존재하는지를 체크하지 않는다.
         */

        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void distinct() {
        List<String> result = queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    /**
     * 동적 쿼리를 해결하는 두 가지 방식
     * 1. BooleanBuilder
     * 2. Where 다중 파라미터 사용
     */
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParameter = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParameter, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder booleanbuilder = new BooleanBuilder();

        if (usernameCond != null) {
            booleanbuilder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            booleanbuilder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(booleanbuilder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParameter = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParameter, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        // where 절에 null 값은 무시된다. comma 콤마로 구분할 경우 and 연산자로 체이닝된다.
        // where 절에는 Predicate 또는 BooleanExpression 둘 다 받아들일 수 있다.
        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }


    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond == null ? null : member.age.eq(ageCond);
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond == null ? null : member.username.eq(usernameCond);
    }

    /**
     * 아래 조건 조합(Composition) 방식은 매우 강력하다. 재사용성이 크다.
     * 단, null 체크는 주의해서 처리해야 한다.
     */
    private Predicate allEq(String usernameCond, int ageCond) {
        // 해당 방식처럼 조건들을 조립하려면 단위 조건이 BooleanExpression 을 반환해야 한다.
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

}
