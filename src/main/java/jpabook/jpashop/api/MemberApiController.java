package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> readMemberV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result<ReadMemberResponse> readMemberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<ReadMemberResponse> collect = findMembers.stream()
                .map(m -> {
                    if (m.getAddress() == null) {
                        return new ReadMemberResponse(
                                m.getId(),
                                m.getName(),
                                null,
                                null,
                                null);
                    } else {
                        return new ReadMemberResponse(
                                m.getId(),
                                m.getName(),
                                m.getAddress().getCity(),
                                m.getAddress().getStreet(),
                                m.getAddress().getZipcode());
                    }
                }).collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    private static class Result<T> {
        private int count;
        private List<T> data;
    }

    @Data
    @AllArgsConstructor
    private static class ReadMemberResponse {
        private Long id;
        private String name;
        private String city;
        private String street;
        private String zipcode;
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member newMember = new Member();
        newMember.setName(request.getName());

        Long id = memberService.join(newMember);

        return new CreateMemberResponse(id);
    }

    @Data
    private static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class CreateMemberResponse {
        private Long id;
    }

    @PostMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());

        Member updatedMember = memberService.findOne(id);
        return new UpdateMemberResponse(updatedMember.getId(), updatedMember.getName());
    }

    @Data
    private static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class UpdateMemberResponse {
        private Long id;
        private String name;
    }
}
