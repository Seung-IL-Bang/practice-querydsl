package com.practice.querydsl.controller;

import com.practice.querydsl.entity.Member;
import com.practice.querydsl.entity.Team;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct // PostConstruct 와 Transactional 이 동시에 동작할 수 없기 때문에 중첩 클래스에 init() 을 정의하여 사용한다.
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member"+i, i, selectedTeam));
            }
        }
    }
}
