package br.com.portifolio.service;

import br.com.portifolio.client.MemberExternalClient;
import br.com.portifolio.dto.response.ExternalMemberResponse;
import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.entity.Member;
import br.com.portifolio.enums.MemberRole;
import br.com.portifolio.exception.ResourceNotFoundException;
import br.com.portifolio.mapper.MemberMapper;
import br.com.portifolio.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberSyncServiceTest {

    @Mock
    private MemberExternalClient memberExternalClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberSyncService memberSyncService;

    @Test
    @DisplayName("Should sync new member from external API")
    void shouldSyncNewMember() {
        ExternalMemberResponse external = ExternalMemberResponse.builder()
                .id(1L).nome("João").atribuicao(MemberRole.GERENTE).build();
        Member saved = Member.builder()
                .id(1L).externalId(1L).nome("João").atribuicao(MemberRole.GERENTE).build();

        when(memberExternalClient.findById(1L)).thenReturn(external);
        when(memberRepository.findByExternalId(1L)).thenReturn(Optional.empty());
        when(memberRepository.save(any())).thenReturn(saved);
        when(memberMapper.toResponse(saved)).thenReturn(
                MemberResponse.builder().id(1L).externalId(1L).nome("João").atribuicao(MemberRole.GERENTE).build());

        MemberResponse result = memberSyncService.syncMember(1L);

        assertThat(result.getNome()).isEqualTo("João");
        assertThat(result.getAtribuicao()).isEqualTo(MemberRole.GERENTE);
        verify(memberRepository).save(argThat(m ->
                m.getExternalId().equals(1L) && m.getNome().equals("João")));
    }

    @Test
    @DisplayName("Should update existing member during sync")
    void shouldUpdateExistingMember() {
        ExternalMemberResponse external = ExternalMemberResponse.builder()
                .id(1L).nome("João Atualizado").atribuicao(MemberRole.GERENTE).build();
        Member existing = Member.builder()
                .id(1L).externalId(1L).nome("João").atribuicao(MemberRole.GERENTE).build();

        when(memberExternalClient.findById(1L)).thenReturn(external);
        when(memberRepository.findByExternalId(1L)).thenReturn(Optional.of(existing));
        when(memberRepository.save(existing)).thenReturn(existing);
        when(memberMapper.toResponse(existing)).thenReturn(
                MemberResponse.builder().id(1L).nome("João Atualizado").build());

        MemberResponse result = memberSyncService.syncMember(1L);

        assertThat(result.getNome()).isEqualTo("João Atualizado");
        assertThat(existing.getNome()).isEqualTo("João Atualizado");
        assertThat(existing.getAtribuicao()).isEqualTo(MemberRole.GERENTE);
    }

    @Test
    @DisplayName("Should return Member entity when ID exists")
    void shouldReturnMemberEntityById() {
        Member member = Member.builder()
                .id(5L).externalId(5L).nome("Ana").atribuicao(MemberRole.FUNCIONARIO).build();
        when(memberRepository.findById(5L)).thenReturn(Optional.of(member));

        Member result = memberSyncService.getMemberEntity(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getNome()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when member does not exist")
    void shouldThrowWhenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberSyncService.getMemberEntity(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
