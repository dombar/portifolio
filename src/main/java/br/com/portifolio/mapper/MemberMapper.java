package br.com.portifolio.mapper;

import br.com.portifolio.dto.response.MemberResponse;
import br.com.portifolio.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    MemberResponse toResponse(Member member);
}
