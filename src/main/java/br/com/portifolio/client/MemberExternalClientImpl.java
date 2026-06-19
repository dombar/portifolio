package br.com.portifolio.client;

import br.com.portifolio.dto.response.ExternalMemberResponse;
import br.com.portifolio.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberExternalClientImpl implements MemberExternalClient {

    private final WebClient memberExternalWebClient;

    @Override
    public ExternalMemberResponse findById(Long id) {
        try {
            return memberExternalWebClient.get()
                    .uri("/members/{id}", id)
                    .retrieve()
                    .bodyToMono(ExternalMemberResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Membro externo não encontrado: " + id);
        }
    }

    @Override
    public List<ExternalMemberResponse> findAll() {
        return memberExternalWebClient.get()
                .uri("/members")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ExternalMemberResponse>>() {})
                .block();
    }
}
