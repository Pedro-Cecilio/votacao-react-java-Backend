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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import com.dbserver.votacaoBackend.domain.autenticacao.Autenticacao;
import com.dbserver.votacaoBackend.domain.autenticacao.repository.AutenticacaoRepository;
import com.dbserver.votacaoBackend.domain.pauta.Pauta;
import com.dbserver.votacaoBackend.domain.pauta.dto.CriarPautaDto;
import com.dbserver.votacaoBackend.domain.pauta.dto.RespostaPautaDto;
import com.dbserver.votacaoBackend.domain.pauta.enums.Categoria;
import com.dbserver.votacaoBackend.domain.pauta.repository.PautaRepository;
import com.dbserver.votacaoBackend.domain.sessaoVotacao.enums.StatusSessaoVotacao;
import com.dbserver.votacaoBackend.domain.usuario.Usuario;
import com.dbserver.votacaoBackend.domain.usuario.repository.UsuarioRepository;
import com.dbserver.votacaoBackend.fixture.autenticacao.AutenticacaoFixture;
import com.dbserver.votacaoBackend.fixture.pauta.CriarPautaDtoFixture;
import com.dbserver.votacaoBackend.fixture.pauta.PautaFixture;
import com.dbserver.votacaoBackend.fixture.usuario.UsuarioFixture;
import com.dbserver.votacaoBackend.infra.security.token.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Stream;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureJsonTesters
class PautaControllerTest {
        private AutenticacaoRepository autenticacaoRepository;
        private UsuarioRepository usuarioRepository;
        private MockMvc mockMvc;
        private JacksonTester<CriarPautaDto> criarPautaDtoJson;
        private CriarPautaDto criarPautaDto;
        private TokenService tokenService;
        private String token;
        private Usuario usuarioCadastrado;
        private PautaRepository pautaRepository;
        private ObjectMapper objectMapper;

        @Autowired
        public PautaControllerTest(AutenticacaoRepository autenticacaoRepository,
                        UsuarioRepository usuarioRepository, MockMvc mockMvc,
                        JacksonTester<CriarPautaDto> criarPautaDtoJson,
                        TokenService tokenService, PautaRepository pautaRepository, ObjectMapper objectMapper) {
                this.usuarioRepository = usuarioRepository;
                this.autenticacaoRepository = autenticacaoRepository;
                this.mockMvc = mockMvc;
                this.criarPautaDtoJson = criarPautaDtoJson;
                this.tokenService = tokenService;
                this.pautaRepository = pautaRepository;
                this.objectMapper = objectMapper;
        }

        @BeforeEach
        void configurar() {
                this.usuarioCadastrado = UsuarioFixture.usuarioAdmin();
                this.usuarioRepository.save(this.usuarioCadastrado);
                Autenticacao autenticacao = AutenticacaoFixture.autenticacaoAdmin(usuarioCadastrado);
                this.autenticacaoRepository.save(autenticacao);
                this.token = this.tokenService.gerarToken(autenticacao);
        }

        @AfterEach
        @Transactional
        void limpar() {
                this.pautaRepository.deleteAll();
                this.autenticacaoRepository.deleteAll();
                this.usuarioRepository.deleteAll();
        }

        @Test
        @DisplayName("Deve ser possível criar uma pauta corretamente")
        void dadoTenhoCriarPautaDtoComDadosCorretosQuandoTentoCriarPautaEntaoRetornarRespostaPautaDto()
                        throws Exception {
                this.criarPautaDto = CriarPautaDtoFixture.criarPautaDtoValido();
                String json = this.criarPautaDtoJson.write(criarPautaDto).getJson();

                mockMvc.perform(MockMvcRequestBuilders
                                .post("/pauta")
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.assunto").value(this.criarPautaDto.assunto()))
                                .andExpect(jsonPath("$.usuario.id").value(this.usuarioCadastrado.getId()))
                                .andExpect(jsonPath("$.usuario.sobrenome").value(this.usuarioCadastrado.getSobrenome()))
                                .andExpect(jsonPath("$.usuario.cpf").value(this.usuarioCadastrado.getCpf()))
                                .andExpect(jsonPath("$.usuario.admin").value(this.usuarioCadastrado.isAdmin()))
                                .andExpect(jsonPath("$.sessaoVotacao").isEmpty());
        }

        private static Stream<Arguments> dadosInvalidosCriarPauta() {
                return Stream.of(
                                Arguments.of(null, Categoria.TRANSPORTE,
                                                "Assunto deve ser informado."),
                                Arguments.of("", Categoria.TRANSPORTE,
                                                "Assunto deve ser informado."),
                                Arguments.of("Você sabe dirigir?", null,
                                                "Categoria deve ser informada."));
        }

        @ParameterizedTest
        @MethodSource("dadosInvalidosCriarPauta")
        @DisplayName("Não deve ser possível criar uma pauta ao informar dados inválidos")
        void dadoTenhoCriarPautaDtoComDadosInvalidosQuandoTentoCriarPautaEntaoRetornarRespostaErro(String assunto,
                        Categoria categoria, String mensagemErro) throws Exception {

                this.criarPautaDto = new CriarPautaDto(assunto, categoria);
                String json = this.criarPautaDtoJson.write(criarPautaDto).getJson();

                mockMvc.perform(MockMvcRequestBuilders
                                .post("/pauta")
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.erro").value(mensagemErro));
        }

        @Test
        @DisplayName("Deve ser possível listar pautas do usuário logado")
        void dadoNaoEnvioCategoriaQuandoBuscoTodasMinhasPautasEntaoRetornarListaDePautas() throws Exception {
                List<Pauta> pautas = PautaFixture.listaDePautas(this.usuarioCadastrado);

                this.pautaRepository.saveAll(pautas);

                MockHttpServletResponse resposta = mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/usuarioLogado")
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray()).andReturn().getResponse();

                List<RespostaPautaDto> pautasDoUsuario = this.objectMapper.readValue(resposta.getContentAsString(),
                                new TypeReference<List<RespostaPautaDto>>() {
                                });
                assertEquals(pautas.size(), pautasDoUsuario.size());
        }

        @Test
        @DisplayName("Deve ser possível listar pautas do usuário logado por categoria")
        void dadoEnvioCategoriaQuandoBuscoTodasMinhasPautasEntaoetornarListaDePautas() throws Exception {
                List<Pauta> pautas = PautaFixture.listaDePautas(this.usuarioCadastrado);

                this.pautaRepository.saveAll(pautas);

                MockHttpServletResponse resposta = mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/usuarioLogado?categoria=TRANSPORTE")
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andReturn().getResponse();

                List<RespostaPautaDto> pautasDoUsuario = this.objectMapper.readValue(resposta.getContentAsString(),
                                new TypeReference<List<RespostaPautaDto>>() {
                                });
                assertEquals(1, pautasDoUsuario.size());
        }

        @Test
        @DisplayName("deve ser possível listar todas pautas ativas")
        void dadoEstouLogadoQuandoBuscoTodasPautasAtivasEntaoRetornarListaDePautas() throws Exception {
                List<Pauta> pautas = PautaFixture.listaDePautasUmaPautaAtiva(usuarioCadastrado);
                this.pautaRepository.saveAll(pautas);

                MockHttpServletResponse resposta = mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/ativas")
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andReturn().getResponse();

                List<RespostaPautaDto> pautasDoUsuario = this.objectMapper.readValue(resposta.getContentAsString(),
                                new TypeReference<List<RespostaPautaDto>>() {
                                });

                assertEquals(1, pautasDoUsuario.size());
        }

        @Test
        @DisplayName("deve ser possível buscar pauta ativa por Id")
        void dadoPossuoPautaIdQuandoBuscoAtivaPorIdEntaoRetornarRespostaPautaDto() throws Exception {
                Pauta pautaTransporte = PautaFixture.pautaTransporteAtiva(this.usuarioCadastrado);

                this.pautaRepository.save(pautaTransporte);

                mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/{id}", pautaTransporte.getId())
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(pautaTransporte.getId()))
                                .andExpect(jsonPath("$.usuario.id").value(this.usuarioCadastrado.getId()))
                                .andExpect(jsonPath("$.sessaoVotacao.id")
                                                .value(pautaTransporte.getSessaoVotacao().getId()));
        }

        @Test
        @DisplayName("Não deve ser possível buscar pauta ativa por Id ao passar id de pauta não ativa")
        void dadoPossuoPautaIdInvalidoQuandoBuscoAtivaPorIdEntaoRetornarRespostaErro() throws Exception {

                mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/{id}", 50)
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.erro").value("Pauta informada não possui sessão ativa."));

        }

        @Test
        @DisplayName("Deve ser possível buscar detalhes de uma pauta ")
        void dadoPossuoPautaIdQuandoBuscoDetalhesPautaEntaoRetornarRespostaPautaDto() throws Exception {
                Pauta pautaTransporte = PautaFixture.pautaTransporteAtiva(usuarioCadastrado);

                this.pautaRepository.save(pautaTransporte);

                mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/detalhes/{id}", pautaTransporte.getId())
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.dadosPauta.id").value(pautaTransporte.getId()))
                                .andExpect(jsonPath("$.status").value(StatusSessaoVotacao.EM_ANDAMENTO.toString()));
        }

        @Test
        @DisplayName("Não deve ser possível buscar detalhes de uma pauta ao passar id inválido ou de pauta não ativa")
        void dadoPossuoPautaIdInvalidoQuandoBuscoDetalhesPautaEntaoRetornarRespostaErro() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders
                                .get("/pauta/detalhes/{id}", 1)
                                .header("Authorization", "Bearer " + this.token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.erro").value("Pauta não encontrada."));
        }
}
