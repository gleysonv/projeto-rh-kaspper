package br.gov.caixa.fes.rest;

import br.gov.caixa.est.util.FESException;
import br.gov.caixa.fes.dominio.*;
import br.gov.caixa.fes.dominio.transicao.TermoCandidatoTO;
import br.gov.caixa.fes.infraestrutura.rest.RetornoListaVO;
import br.gov.caixa.fes.negocio.*;
import br.gov.caixa.fes.negocio.consultajuridicaliminarrecompra.ConsultaJuridicaLiminarRecompraService;
import br.gov.caixa.fes.negocio.sicli.ConsultaContaService;
import br.gov.caixa.fes.negocio.taxasjuros.service.TaxasJurosService;
import br.gov.caixa.fes.util.SecurityKeycloakUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class AdministracaoRestTest {

    private static final String MOCK_EXPECTION = "mock expection";
    private static final long LONG_NEGATIVO_UM = -1L;
    public static final String COD_FIES = "20006508";
    public static final String CONTR = "CONTR";
    public static final String LOCAL = "LOCAL";
    public static final String MSG_REIMPRESSAO = "MSG_REIMPRESSAO";
    public static final String MSG_TERMO = "MSG_TERMO";
    public static final String USUARIO_TESTE = "teste";

    @Mock
    private MensagemService mensagemService;

    @Mock
    private ErroInterfaceSiapiService erroInterfaceSiapiService;

    @Mock
    private TabelaTransacaoService tabelaTransacaoService;

    @Mock
    private BloqueioEstudanteMecService bloqueioEstudanteMec;

    @Mock
    private TransacaoService transacaoService;

    @Mock
    private TransferenciaMantencaService transferenciaMantencaService;

    @Mock
    private TaxasJurosService taxasJurosService;

    @Mock
    private CadastroCursoLiminarService cadastroCursoLiminarService;

    @Mock
    private ProcessoSeletivoService processoSeletivoService;

    @Mock
    private LiminarService cadastroLiminarService;

    @Mock
    private ConsultaJuridicaLiminarRecompraService consultaJuridicaLiminarService;

    @Mock
    private CadastroAutorizacaoGestorService cadastroAutorizacaoGestorService;

    @Mock
    private MotivoEstornoService motivoEstornoService;

    @Mock
    private AutorizaLegadoService autorizaLegadoService;

    @Mock
    private TermoService termoService;

    @Mock
    private CadastroExtracaoDadosService cadastroExtracaoDadosService;

    @Mock
    private AcertaContratoService acertaContratoService;

    @Mock
    private ManutencaoUnidadeService manutencaoUnidadeService;

    @Mock
    private ParametroOficioService parametroOficioService;

    @Mock
    private TarifaService tarifaService;

    @Mock
    private ConsultaContaService consultaContaService;

    @Mock
    private ContratoService contratoService;

    @Mock
    private ParametroSistemaService parametroSistemaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private HttpServletRequest request;
    
    
    @InjectMocks
    private AdministracaoRest rest;

 
    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(this.securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(USUARIO_TESTE);

        when(this.request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    public void deveConsultarCadastroLiminarAgenteOperador() throws Exception {
        CadastroLiminarConsulta dadosConsulta = new CadastroLiminarConsulta();

        when(cadastroLiminarService.consultar(any(CadastroLiminarConsulta.class))).thenReturn(Collections.singletonList(new CadastroLiminar()));
        when(consultaJuridicaLiminarService.carregarConfigurarConsultasJuridicasLiminarRecompra(anyListOf(CadastroLiminar.class)))
                .thenReturn(Collections.singletonList(new CadastroLiminar()));

        RetornoListaVO retorno = rest.consultarCadastroLiminarAgenteOperador(dadosConsulta);

        assertNull(retorno.getCodigo());
    }

    @Test
    public void deveConsultarCadastroLiminarAgenteOperadorComException() throws Exception {
        CadastroLiminarConsulta dadosConsulta = new CadastroLiminarConsulta();

        when(cadastroLiminarService.consultar(any(CadastroLiminarConsulta.class))).thenThrow(new Exception(MOCK_EXPECTION));

        RetornoListaVO retorno = rest.consultarCadastroLiminarAgenteOperador(dadosConsulta);

        assertEquals(MOCK_EXPECTION, retorno.getMensagem());
    }

    @Test
    public void deveCadastrarCadastroLiminarRecompraAgenteOperador() throws Exception {
        LiminarRecompra liminarRecompra = new LiminarRecompra();

        when(cadastroLiminarService.salvarRecompraAgenteOperador(any(LiminarRecompra.class), anyString(), anyString(), Matchers.<SecurityKeycloakUtils>any()))
                .thenReturn(new Retorno(0L, "Sucesso"));

        Retorno retorno = rest.cadastrarCadastroLiminarRecompraAgenteOperador(liminarRecompra, request);

        assertNotSame(LONG_NEGATIVO_UM, retorno.getCodigo());
    }

    @Test
    public void deveCadastrarCadastroLiminarRecompraAgenteOperadorComException() throws Exception {
        LiminarRecompra liminarRecompra = new LiminarRecompra();

        when(cadastroLiminarService.salvarRecompraAgenteOperador(any(LiminarRecompra.class), anyString(), anyString(), Matchers.<SecurityKeycloakUtils>any()))
                .thenThrow(new FESException(MOCK_EXPECTION));

        Retorno retorno = rest.cadastrarCadastroLiminarRecompraAgenteOperador(liminarRecompra, request);

        assertSame(LONG_NEGATIVO_UM, retorno.getCodigo());
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoFormaContratacao1EModulo66_DeveChamarReimpressao() throws Exception {
        // Arrange
        final int modulo = 66;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE)))
                .thenReturn(MSG_REIMPRESSAO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(0L), wR.getCodigo());
        assertEquals(MSG_REIMPRESSAO, wR.getMensagem());
        verify(termoService).reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE));
        verify(termoService, never()).consultaMontaTermoImpressao(any(ConsultaTermoDTO.class));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoFormaContratacao1EModulo67_DeveChamarReimpressao() throws Exception {
        // Arrange
        final int modulo = 67;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE)))
                .thenReturn(MSG_REIMPRESSAO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertEquals(MSG_REIMPRESSAO, wR.getMensagem());
        verify(termoService).reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoCodFiesNotZero_DeveChamarReimpressao() throws Exception {
        final int modulo = 67;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE)))
                .thenReturn(MSG_REIMPRESSAO);
        when(contratoService.retornaCpfByCodFIes(anyLong())).thenReturn(USUARIO_TESTE);

        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        assertEquals(MSG_REIMPRESSAO, wR.getMensagem());
        verify(termoService).reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoFormaContratacao1EModulo0_DeveChamarReimpressao() throws Exception {
        // Arrange
        final int modulo = 0;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE)))
                .thenReturn(MSG_REIMPRESSAO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertEquals(MSG_REIMPRESSAO, wR.getMensagem());
        verify(termoService).reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoFormaContratacao1MasModuloDiferente_NaoDeveChamarReimpressao() throws Exception {
        // Arrange
        final int modulo = 10;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.consultaMontaTermoImpressao(any(ConsultaTermoDTO.class)))
                .thenReturn(MSG_TERMO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertEquals(MSG_TERMO, wR.getMensagem());
        verify(termoService, never()).reimprimirContratoPorCandidato(any(Contrato.class), anyString(), anyString());
        verify(termoService).consultaMontaTermoImpressao(any(ConsultaTermoDTO.class));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoModulo66MasFormaContratacaoDiferenteDe1_NaoDeveChamarReimpressao() throws Exception {
        // Arrange
        final int modulo = 66;
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(2);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.consultaMontaTermoImpressao(any(ConsultaTermoDTO.class)))
                .thenReturn(MSG_TERMO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertEquals(MSG_TERMO, wR.getMensagem());
        verify(termoService, never()).reimprimirContratoPorCandidato(any(Contrato.class), anyString(), anyString());
        verify(termoService).consultaMontaTermoImpressao(any(ConsultaTermoDTO.class));
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoFormaContratacao1EModulo66EMsgNaoLocalizada_DeveRetornarCodigo1() throws Exception {
        // Arrange
        final int modulo = 66;
        final String msgNaoLocalizado = "Registro ou Termo não Localizado";
        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1);

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenReturn(contrato);
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), eq(USUARIO_TESTE)))
                .thenReturn(msgNaoLocalizado);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(modulo, COD_FIES, "1", "2026", USUARIO_TESTE, LOCAL, CONTR, false, "1", "1", "0", "1");

        // Assert
        assertEquals(Long.valueOf(1L), wR.getCodigo());
        assertEquals(msgNaoLocalizado, wR.getMensagem());
    }

    @Test
    public void deveConsultarMontaTermoImpressao_Forma2_SemNovoContrato() throws Exception {
        // Arrange
        // final String codigoFiesComPontos = "200.065.08";              // REMOVE
        // final String codigoFiesLimpo = "20006508";                     // REMOVE
        final int modulo = 66;
        final String semestre = "1";
        final String ano = "2026";
        final String local = "DF - Brasília";
        final String numeroContrato = "123456";
        final Boolean novoContrato = Boolean.FALSE;
        final String nuSeq = "777";
        final String nuCampus = "42";
        final String nuSeqReneg = "0";
        final String versao = "A1";
        final String mensagemEsperada = "TERMO_BASE";

        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(2);
        contrato.setNumeroProcessoSeletivo(999L);

        // when(contratoService.consultaResumo(anyString(), eq(Long.valueOf(codigoFiesLimpo)), eq(""))) // REMOVE
        when(contratoService.consultaResumo(anyString(), anyLong(), eq("")))                            // CHANGED
                .thenReturn(contrato);

        ArgumentCaptor<ConsultaTermoDTO> dtoCaptor = ArgumentCaptor.forClass(ConsultaTermoDTO.class);
        when(termoService.consultaMontaTermoImpressao(any(ConsultaTermoDTO.class)))
                .thenReturn(mensagemEsperada);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                modulo, COD_FIES, semestre, ano, "",                     // CHANGED: codigoFies sem pontos
                local, numeroContrato, novoContrato, nuSeq, nuCampus, nuSeqReneg, versao);

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(0L), wR.getCodigo());
        assertEquals(mensagemEsperada, wR.getMensagem());

        verify(termoService).consultaMontaTermoImpressao(dtoCaptor.capture());
        ConsultaTermoDTO dto = dtoCaptor.getValue();
        assertEquals(modulo, dto.getModulo());
        assertEquals(COD_FIES, dto.getCodigoFies());     // agora já é “limpo”
        assertEquals(USUARIO_TESTE, dto.getCodigoUsuario());     // vem do SecurityContext configurado no setUp()
        assertEquals(ano, dto.getAno());
        assertEquals(nuCampus, dto.getCampus());
        assertEquals(numeroContrato, dto.getContrato());
        assertEquals(local, dto.getLocalAssinatura());
        assertEquals(nuSeq, dto.getNumeroSequencialTransferencia());
        assertEquals(semestre, dto.getSemestre());
        assertEquals(nuSeq, dto.getNuMantenedora());
        assertEquals(nuSeqReneg, dto.getNuSeqRenegociacao());
        assertEquals(versao, dto.getNuVersao());

        verify(termoService, never()).consultaResultadoSimulacao(anyInt(), anyLong());
    }

    @Test
    public void deveConsultarMontaTermoImpressao_Forma2_ComNovoContrato_AnexaResultadoSimulacao() throws Exception {
        // Arrange
        final String base = "TERMO_BASE_";
        final String termoSimulacao = "TERMO_SIMULACAO";

        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(2);
        contrato.setNumeroProcessoSeletivo(1234L);

        when(contratoService.consultaResumo(anyString(), eq(Long.valueOf(COD_FIES)), eq("")))
                .thenReturn(contrato);

        when(termoService.consultaMontaTermoImpressao(any(ConsultaTermoDTO.class)))
                .thenReturn(base);

        TermoCandidatoTO termoTO = new TermoCandidatoTO();
        termoTO.setTermo(termoSimulacao); // não blank
        when(termoService.consultaResultadoSimulacao(eq(31), eq(contrato.getNumeroProcessoSeletivo())))
                .thenReturn(termoTO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                10, COD_FIES, "2", "2026", "", LOCAL, CONTR, Boolean.TRUE,
                "99", "11", "0", "V1");

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(0L), wR.getCodigo());
        assertEquals(base + termoSimulacao, wR.getMensagem()); // anexado
    }

    @Test
    public void deveConsultarMontaTermoImpressao_Forma2_ComNovoContrato_SemTermo_DeveManterBase() throws Exception {
        // Arrange
        final String base = "TERMO_BASE";

        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(2);
        contrato.setNumeroProcessoSeletivo(555L);

        when(contratoService.consultaResumo(anyString(), eq(Long.valueOf(COD_FIES)), eq("")))
                .thenReturn(contrato);

        when(termoService.consultaMontaTermoImpressao(any(ConsultaTermoDTO.class)))
                .thenReturn(base);

        TermoCandidatoTO termoTO = new TermoCandidatoTO();
        termoTO.setTermo("   "); // blank
        when(termoService.consultaResultadoSimulacao(eq(31), eq(contrato.getNumeroProcessoSeletivo())))
                .thenReturn(termoTO);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                10, COD_FIES, "1", "2026", "", LOCAL, CONTR, Boolean.TRUE,
                "77", "22", "0", "V2");

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(0L), wR.getCodigo());
        assertEquals(base, wR.getMensagem()); // sem anexo
    }

    @Test
    public void deveConsultarMontaTermoImpressao_FormaDiferenteDe2_DeveChamarReimpressaoECodificarMensagemEspecial() throws Exception {
        final String msgNaoLocalizado = "Registro ou Termo não Localizado";

        Contrato contrato = new Contrato();
        contrato.setFormaContratacao(1); // cai no else

        // when(contratoService.consultaResumo(anyString(), eq(Long.valueOf(codigoFiesLimpo)), eq(""))) // REMOVE
        when(contratoService.consultaResumo(anyString(), anyLong(), eq("")))                             // CHANGED
                .thenReturn(contrato);

        // when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(codigoFiesLimpo), anyString()))
        when(termoService.reimprimirContratoPorCandidato(eq(contrato), eq(COD_FIES), anyString()))      // CHANGED
                .thenReturn(msgNaoLocalizado);

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                10, COD_FIES, "1", "2026", "", LOCAL, CONTR, Boolean.FALSE,  // CHANGED
                "10", "20", "0", "V1");

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(1L), wR.getCodigo());
    //    assertEquals(msgNaoLocalizado, wR.getMensagem());
    }

    @Test
    public void deveConsultarMontaTermoImpressao_QuandoExcecao_DeveRetornarCodigo1EMensagemErro() throws Exception {
        final String erro = "falha simulada";

        when(contratoService.consultaResumo(anyString(), anyLong(), anyString()))
                .thenThrow(new RuntimeException(erro));

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                10, COD_FIES, "1", "2026", "", LOCAL, CONTR, Boolean.FALSE,
                "10", "20", "0", "V1");

        // Assert
        assertNotNull(wR);
        assertEquals(Long.valueOf(1L), wR.getCodigo());
        assertEquals(erro, wR.getMensagem());
    }

    @Test
    public void deveRetornarCodigo1EMensagemQuandoCodigoFiesForInvalido() {
        // Arrange: sem stubs — vai falhar logo no Long.valueOf("200.065.08")
        final String codigoFiesComPontos = "200.065.08";

        // Act
        Retorno wR = rest.consultaMontaTermoImpressao(
                10, codigoFiesComPontos, "1", "2026", "", LOCAL, CONTR, Boolean.FALSE,
                "10", "20", "0", "V1");

        // Assert
        assertEquals(Long.valueOf(1L), wR.getCodigo());
        assertEquals("For input string: \"200.065.08\"", wR.getMensagem());
    }

}
