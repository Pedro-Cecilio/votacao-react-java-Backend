package com.dbserver.votacaoBackend.fixture;

import java.util.ArrayList;
import java.util.List;

import com.dbserver.votacaoBackend.domain.pauta.Pauta;
import com.dbserver.votacaoBackend.domain.pauta.dto.CriarPautaDto;
import com.dbserver.votacaoBackend.domain.pauta.dto.RespostaPautaDto;
import com.dbserver.votacaoBackend.domain.pauta.enums.Categoria;
import com.dbserver.votacaoBackend.domain.usuario.Usuario;

public class PautaFixture {

    public static Categoria categoria = Categoria.TRANSPORTE;
    
    public static Pauta pautaTransporte(Usuario usuario) {
        return new Pauta("Sabe dirigir?", categoria.toString(),
                usuario);
    }

    public static Pauta pautaTransporteAtiva(Usuario usuario) {
        Pauta pauta = pautaTransporte(usuario);

        pauta.setSessaoVotacao(SessaoVotacaoFixture.sessaoVotacaoAtiva(pauta));
        return pauta;
    }

    public static CriarPautaDto criarPautaDtoValido() {
        return new CriarPautaDto("Sabe dirigir?", categoria.toString());
    }

    public static RespostaPautaDto respostaPautaDto(Usuario usuario) {
        return new RespostaPautaDto(pautaTransporte(usuario), null);
    }

    private static Pauta pautaSaude(Usuario usuario) {
        return new Pauta("Você está bem de saúde?", Categoria.SAUDE.toString(),
                usuario);
    }
    public static List<Pauta> listaDePautas(Usuario usuario) {
        List<Pauta> lista = new ArrayList<>();
        lista.add(pautaSaude(usuario));
        lista.add(pautaTransporte(usuario));
        return lista;
    }

    public static List<Pauta> listaDePautasUmaPautaAtiva(Usuario usuario) {
        List<Pauta> lista = new ArrayList<>();
        lista.add(pautaSaude(usuario));
        lista.add(pautaTransporteAtiva(usuario));
        return lista;
    }
}