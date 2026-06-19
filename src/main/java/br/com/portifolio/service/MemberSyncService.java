package br.com.portifolio.service;

import br.com.portifolio.dto.response.ExternalMemberResponse;
import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.entity.Member;
import br.com.portifolio.exception.ResourceNotFoundException;
import br.com.portifolio.mapper.MemberMapper;
import br.com.portifolio.client.MemberExternalClient;
import br.com.portifolio.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberSyncService {

    private final MemberExternalClient memberExternalClient;
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    @Transactional
    public MemberResponse syncMember(Long externalId) {
        ExternalMemberResponse external = memberExternalClient.findById(externalId);

        Member member = memberRepository.findByExternalId(externalId)
                .map(existing -> updateMember(existing, external))
                .orElseGet(() -> createMember(external));

        return memberMapper.toResponse(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public Member getMemberEntity(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado: " + id));
    }

    private Member createMember(ExternalMemberResponse external) {
        return Member.builder()
                .externalId(external.getId())
                .nome(external.getNome())
                .atribuicao(external.getAtribuicao())
                .build();
    }

    private Member updateMember(Member member, ExternalMemberResponse external) {
        member.setNome(external.getNome());
        member.setAtribuicao(external.getAtribuicao());
        return member;
    }
}
