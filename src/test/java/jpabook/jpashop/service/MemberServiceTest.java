package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void 회원가입() {
        // given
        Member member = new Member();
        member.setName("memberA");

        // when
        Long savedId = memberService.join(member);

        // then
        assertThat(member).isEqualTo(memberRepository.findById(savedId).get());
    }

    @Test
    public void 중복_회원_예외() {
        // given
        Member memberA = new Member();
        memberA.setName("memberA");
        Member memberB = new Member();
        memberB.setName("memberA");

        // when
        memberService.join(memberA);
        assertThatThrownBy(() -> memberService.join(memberB))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 존재하는 회원입니다.");

        // then
    }
}