package com.example.demo.entity;

import com.example.demo.dto.MemberDto;
import com.example.demo.dto.QMemberDto;
import com.example.demo.dto.UserDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.example.demo.entity.QMember.member;
import static com.example.demo.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    void testEntity() {
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
    void startJpql() {
        String sql = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(sql, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {

        em.flush();
        em.clear();

        List<Team> fetch = queryFactory
                .select(team)
                .from(team)
                .leftJoin(team.members,member)
                .fetchJoin()
                .fetch();

        for (Team fetch1 : fetch) {
            System.out.println(fetch1);
        }
    }

    @Test
    void search() {

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> member1 = queryFactory.select(member)
                .from(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();


        for (Member m : member1) {
            System.out.println("me = " + m);
        }
    }

    @Test
    void paging() {

        for (int i = 0; i < 100; i++) {
            em.persist(new Member("member" + i, 100));
        }


        List<Member> member1 = queryFactory.select(member)
                .from(member)
                .where(member.age.eq(100))
                .orderBy(member.id.asc(), member.age.desc(), member.username.asc().nullsLast())
                .offset(1)
                .limit(20)
                .fetch();


        for (Member m : member1) {
            System.out.println("me = " + m);
        }
    }

    @Test
    void aggregation1() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        for (Tuple tuple1 : result) {
            System.out.println(tuple1.get(member.count()));
            System.out.println(tuple1.get(member.age.avg()));
            System.out.println(tuple1.get(member.age.sum()));
        }
    }

    @Test
    void aggregation2() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        for (Tuple tuple1 : result) {
            System.out.println(tuple1.get(member.count()));
            System.out.println(tuple1.get(member.age.avg()));
            System.out.println(tuple1.get(member.age.sum()));
        }
    }

    @Test
    void group() throws Exception {
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : fetch) {
            String s = tuple.get(team.name);
            Double age = tuple.get(member.age.avg());
            System.out.println("age = " + age);
            System.out.println("s = " + s);
        }
    }

    @Test
    void join1() throws Exception {
        List<Member> members = queryFactory
                .select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("TeamA"))
                .fetch();
        for (Member member1 : members) {
            System.out.println("member = " + member1);
        }
    }

    @Test
    void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member member1 : members) {
            System.out.println(member1);
        }
    }

    @Test
    void joinOn() throws Exception {
        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team).where(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println(tuple.get(team));
        }
    }

    @Test
    void theta_join_on_no_relation() throws Exception {

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> members = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple member1 : members) {
            System.out.println(member1);
        }
    }

    @Autowired
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();


    }

    @Test
    void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();


        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 적용").isTrue();


    }

    @Test
    void subquery() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        System.out.println(members.get(0));
    }

    @Test
    void subquery1() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        for (Member member1 : members) {
            System.out.println(member1);
        }
    }

    @Test
    void subquery2() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(10))
                ))
                .fetch();

        for (Member member1 : members) {
            System.out.println(member1);
        }
    }

    @Test
    void subquery3() throws Exception {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> members = queryFactory
                .select(memberSub.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(memberSub)
                .fetch();


        for (Tuple member1 : members) {
            System.out.println(member1);
        }
    }

    @Test
    void compleCase() {
        List<String> list = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 10)).then("0-10살")
                        .when(member.age.between(11, 20)).then("11-20살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String t : list) {
            System.out.println(t);
        }

    }

    @Test
    void constant() {
        List<Tuple> fetch = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println(tuple);
        }
    }

    @Test
    void concat() {
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void stringResult() {
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void tupleSelect() {
        List<Tuple> fetch = queryFactory
                .select(member.username, member, team)
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println(tuple);
        }
    }

    @Test
    void findDtoByJpql() {
        List<MemberDto> singleResult = em.createQuery("select new com.example.demo.dto.MemberDto(m.username,m.age) from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : singleResult) {
            System.out.println(memberDto);
        }
    }

    @Test
    void setterDto() {
        List<MemberDto> fetch = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    void filedDto() {
        List<MemberDto> fetch = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    void constructorDto() {
        List<MemberDto> fetch = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    void findUserDto() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),

                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        for (UserDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    void findDtoByQueryProjections() {
        List<MemberDto> fetch = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : fetch) {
            System.out.println(memberDto);
        }
    }

    @Test
    void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .select(member)
                .from(member)
                .where(builder)
                .fetch();

    }

    @Test
    void dynamic_whereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

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

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    void updateQuery1() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        List<Member> members = queryFactory
                .select(member)
                .from(member)
                .fetch();

        for (Member member1 : members) {
            System.out.println(member1);
        }
    }

    @Test
    void updateQuery2() {
        queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    void sqlFucntion() {
        List<String> fetch = queryFactory
                .select(Expressions.stringTemplate("function('replace',{0},{1},{2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }
    }


}
