package com.dbserver.votacaoBackend.domain.usuario.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.dbserver.votacaoBackend.domain.autenticacao.Autenticacao;
import com.dbserver.votacaoBackend.domain.usuario.Usuario;
import com.dbserver.votacaoBackend.domain.usuario.dto.CriarUsuarioDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.CriarUsuarioRespostaDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.UsuarioRespostaDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.VerificarSeUsuarioExisteRespostaDto;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    public static final UsuarioMapper INSTANCE = Mappers.getMapper(UsuarioMapper.class);
    
    @Mapping(target = "id", ignore = true)
    Usuario toUsuario(CriarUsuarioDto dto);

    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "email", source = "autenticacao.email")
    @Mapping(target = "nome", source = "usuario.nome")
    @Mapping(target = "sobrenome", source = "usuario.sobrenome")
    @Mapping(target = "cpf", source = "usuario.cpf")
    @Mapping(target = "admin", source = "usuario.admin")
    CriarUsuarioRespostaDto toCriarUsuarioRespostaDto(Usuario usuario, Autenticacao autenticacao);

    UsuarioRespostaDto toUsuarioRespostaDto(Usuario usuario);

    default VerificarSeUsuarioExisteRespostaDto toVerificarSeUsuarioExisteRespostaDto(boolean existe){
        return new VerificarSeUsuarioExisteRespostaDto(existe);
    }
}
