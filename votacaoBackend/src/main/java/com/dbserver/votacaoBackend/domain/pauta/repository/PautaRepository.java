package com.dbserver.votacaoBackend.domain.pauta.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import com.dbserver.votacaoBackend.domain.pauta.Pauta;
import com.dbserver.votacaoBackend.domain.pauta.enums.Categoria;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {
    List<Pauta> findAllByUsuarioId(Long usuarioId, Pageable pageable);
    List<Pauta> findAllByUsuarioIdAndCategoria(Long usuarioId, Categoria categoria, Pageable pageable);

    @Query("SELECT p FROM Pauta p WHERE p.categoria = :categoria AND p.sessaoVotacao.dataFechamento > :dataAtual")
    List<Pauta> findAllByCategoriaAndSessaoVotacaoAtiva(Categoria categoria, LocalDateTime dataAtual, Pageable pageable);

    @Query("SELECT p FROM Pauta p WHERE p.sessaoVotacao.dataFechamento < :dataAtual")
    List<Pauta> findAllBySessaoVotacaoAtiva(LocalDateTime dataAtual, Pageable pageable);
}
