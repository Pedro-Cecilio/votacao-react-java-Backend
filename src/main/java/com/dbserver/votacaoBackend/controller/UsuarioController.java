package com.dbserver.votacaoBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dbserver.votacaoBackend.domain.usuario.dto.CriarUsuarioDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.CriarUsuarioRespostaDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.UsuarioRespostaDto;
import com.dbserver.votacaoBackend.domain.usuario.dto.VerificarSeUsuarioExisteRespostaDto;
import com.dbserver.votacaoBackend.domain.usuario.service.UsuarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/usuario")
public class UsuarioController {
    private UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @SecurityRequirement(name = "bearer-key")
    @PostMapping
    public ResponseEntity<CriarUsuarioRespostaDto> criarUsuario(@RequestBody @Valid CriarUsuarioDto dto) {
        CriarUsuarioRespostaDto resposta = usuarioService.criarUsuario(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/usuarioLogado")
    public ResponseEntity<UsuarioRespostaDto> buscarUsuarioLogado() {
        UsuarioRespostaDto resposta = this.usuarioService.buscarUsuarioLogadoComoDto();

        return ResponseEntity.ok().body(resposta);
    }

    @GetMapping("/existe")
    public ResponseEntity<VerificarSeUsuarioExisteRespostaDto> verificarSeUsuarioExistePorCpf(@RequestParam(name = "cpf", required = false, defaultValue = "") final String cpf) {
        VerificarSeUsuarioExisteRespostaDto resposta = this.usuarioService.verificarSeExisteUsuarioPorCpfComoDto(cpf);
        
        return ResponseEntity.ok().body(resposta);
    }

}
