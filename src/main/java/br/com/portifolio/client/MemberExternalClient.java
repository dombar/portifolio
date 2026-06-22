package br.com.portifolio.client;

import br.com.portifolio.dto.response.ExternalMemberResponse;

import java.util.List;

public interface MemberExternalClient {

    ExternalMemberResponse findById(Long id);

    List<ExternalMemberResponse> findAll();
}
