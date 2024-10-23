package com.practice.querydsl.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.ToString;

@ToString(of = {"username", "age"})
public class MemberDto {

    private String username;
    private int age;


    public MemberDto() {
    }

    @QueryProjection // === 주의 === 컴파일 필요 => QMemberDto 생성
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
