package com.dbserver.votacaoBackend.domain.autenticacao;

import com.dbserver.votacaoBackend.domain.autenticacao.dto.AutenticacaoDto;
import com.dbserver.votacaoBackend.domain.usuario.Usuario;
import com.dbserver.votacaoBackend.utils.Utils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Autenticacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Setter
    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // public Autenticacao(AutenticacaoDto dto) {
    //     setEmail(dto.email());
    //     setSenha(dto.senha());
    // }
    public Autenticacao(String email, String senha) {
        setEmail(email);
        setSenha(senha);
    }

    public void setEmail(String email) {
        if(email == null || !Utils.validarRegex(Utils.REGEX_EMAIL, email.trim())){
            throw new IllegalArgumentException("Email com formato inválido");
        }
        this.email = email.trim();
    }

    public void setSenha(String senha) {
        
        if (senha == null)
            throw new IllegalArgumentException("Senha deve ser informada");
        if (senha.trim().length() < 8)
            throw new IllegalArgumentException("Senha deve conter 8 caracteres no mínimo");

        this.senha = senha;
    }
    

}
