package com.dbserver.votacaoBackend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.dbserver.votacaoBackend.domain.autenticacao.Autenticacao;
import com.dbserver.votacaoBackend.domain.autenticacao.dto.AutenticacaoDto;
import com.dbserver.votacaoBackend.domain.autenticacao.dto.AutorizarVotoExternoDto;
import com.dbserver.votacaoBackend.domain.autenticacao.repository.AutenticacaoRepository;
import com.dbserver.votacaoBackend.domain.usuario.Usuario;
import com.dbserver.votacaoBackend.domain.usuario.repository.UsuarioRepository;
import com.dbserver.votacaoBackend.fixture.AutenticacaoFixture;
import com.dbserver.votacaoBackend.fixture.UsuarioFixture;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.Stream;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureJsonTesters
class AutenticacaoControllerTest {

    private AutenticacaoRepository autenticacaoRepository;
    private UsuarioRepository usuarioRepository;
    private Autenticacao autenticacao;
    private AutenticacaoDto autenticacaoDto;
    private MockMvc mockMvc;
    private JacksonTester<AutenticacaoDto> autenticacaoDtoJson;
    private JacksonTester<AutorizarVotoExternoDto> autorizarVotoExternoDtoJson;
    private AutorizarVotoExternoDto autorizarVotoExternoDto;

    @Autowired
    public AutenticacaoControllerTest(AutenticacaoRepository autenticacaoRepository,
            UsuarioRepository usuarioRepository, MockMvc mockMvc, JacksonTester<AutenticacaoDto> autenticacaoDtoJson,
            JacksonTester<AutorizarVotoExternoDto> autorizarVotoExternoDtoJson) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacaoRepository = autenticacaoRepository;
        this.mockMvc = mockMvc;
        this.autenticacaoDtoJson = autenticacaoDtoJson;
        this.autorizarVotoExternoDtoJson = autorizarVotoExternoDtoJson;
    }

    @BeforeEach
    void configurar() {
        Usuario usuario = UsuarioFixture.usuarioAdmin();
        this.usuarioRepository.save(usuario);
        this.autenticacao = AutenticacaoFixture.autenticacaoAdmin(usuario);
        this.autenticacaoRepository.save(this.autenticacao);
        this.autenticacaoDto = null;
    }

    @AfterEach
    @Transactional
    void limpar() {
        this.autenticacaoRepository.deleteAll();
        this.usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve ser possível realizar login corretamente")
    void dadoPossuoDadosDeAutenticacaCorretosoQuandoTentoRealizarLoginEntaoRetornarAutenticacaoRespostaDto()
            throws Exception {

        this.autenticacaoDto = AutenticacaoFixture.autenticacaoDtoAdminValido();

        String json = this.autenticacaoDtoJson.write(this.autenticacaoDto).getJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.admin").value(this.autenticacao.getUsuario().isAdmin()));
    }

    @Test
    @DisplayName("Não deve ser possível realizar login com senha incorreta")
    void dadoPossuoUmaSenhaIncorretaQuandoTentoRealizarLoginEntaoRetornarRespostaErro()
            throws Exception {

        this.autenticacaoDto = AutenticacaoFixture.autenticacaoDtoSenhaIncorreta();

        String json = this.autenticacaoDtoJson.write(this.autenticacaoDto).getJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Dados de login inválidos."));
    }

    @Test
    @DisplayName("Não deve ser possível realizar login com email inexistente")
    void dadoPossuoUmaEmailInexistenteQuandoTentoRealizarLoginEntaoRetornarRespostaErro()
            throws Exception {

        this.autenticacaoDto = AutenticacaoFixture.autenticacaoDtoEmailIncorreto();

        String json = this.autenticacaoDtoJson.write(this.autenticacaoDto).getJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Dados de login inválidos."));
    }

    @ParameterizedTest
    @DisplayName("Testes de login com dados inválidos")
    @MethodSource("dadosInvalidosParaRealizarLogin")
    void dadoAutenticacaoDtoDadosInvalidosQuandoTentoRealizarLoginEntaoRetornarRespostaErro(String email, String senha,
            String mensagemErro)
            throws Exception {

        this.autenticacaoDto = new AutenticacaoDto(email, senha);

        String json = this.autenticacaoDtoJson.write(autenticacaoDto).getJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(mensagemErro));
    }

    private static Stream<Arguments> dadosInvalidosParaRealizarLogin() {
        return Stream.of(
                Arguments.of("email", AutenticacaoFixture.SENHA, "Email com formato inválido."),
                Arguments.of("", AutenticacaoFixture.SENHA, "Email deve ser informado."),
                Arguments.of(null, AutenticacaoFixture.SENHA, "Email deve ser informado."),

                Arguments.of(AutenticacaoFixture.EMAIL_ADMIN_CORRETO, "", "Senha deve ser informada."),
                Arguments.of(AutenticacaoFixture.EMAIL_ADMIN_CORRETO, null, "Senha deve ser informada."));
    }

    @Test
    @DisplayName("Deve ser possível validar usuário existente com dados para validar voto externo validos ao tentar votar externamente")
    void dadoPossuoDadosValidarVotoExternoCorretosQuandoTentoValidarVotoExternoEntaoRetornarValidarVotoExternoComTrue()
            throws Exception {
        String cpf = this.autenticacao.getUsuario().getCpf();

        this.autorizarVotoExternoDto = AutenticacaoFixture.autorizarVotoExternoDtoValido(cpf);

        String json = this.autorizarVotoExternoDtoJson.write(this.autorizarVotoExternoDto).getJson();
        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/votoExterno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    @DisplayName("Não deve ser possível validar usuário existente com ao passar cpf não cadastrado ao tentar votar externamente")
    void dadoCpfNaoCadastradoQuandoTentoValidarVotoExternoEntaoRetornarRespostaErro()
            throws Exception {
        this.autorizarVotoExternoDto = AutenticacaoFixture.autorizarVotoExternoDtoCpfInvalido();

        String json = this.autorizarVotoExternoDtoJson.write(this.autorizarVotoExternoDto).getJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/votoExterno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Dados de autenticação inválidos."));
    }

    @Test
    @DisplayName("Não deve ser possível validar usuário existente com ao passar senha incorreta ao tentar votar externamente")
    void dadoSenhaIncorretaQuandoTentoValidarVotoExternoEntaoRetornarRespostaErro()
            throws Exception {
        String cpf = this.autenticacao.getUsuario().getCpf();

        this.autorizarVotoExternoDto = AutenticacaoFixture.autorizarVotoExternoDtoSenhaIncorreta(cpf);

        String json = this.autorizarVotoExternoDtoJson.write(this.autorizarVotoExternoDto).getJson();
        
        mockMvc.perform(MockMvcRequestBuilders
                .post("/auth/votoExterno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Dados de autenticação inválidos."));
    }
}
