package br.com.portifolio.repository;

import br.com.portifolio.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByExternalId(Long externalId);

    boolean existsByExternalId(Long externalId);
}
