package br.gov.caixa.fes.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.gov.caixa.arqrefcore.excecao.BusinessException;
import br.gov.caixa.fes.dominio.Contrato;
import br.gov.caixa.fes.dominio.Estudante;
import br.gov.caixa.fes.dominio.Fiador;
import br.gov.caixa.fes.dominio.Retorno;
import br.gov.caixa.fes.dominio.renegociacaocontrato.vo.SolicitacaoRenegociacaoContratoVO;
import br.gov.caixa.fes.negocio.ContratoService;
import br.gov.caixa.fes.negocio.FiadorService;

@RunWith(MockitoJUnitRunner.class)
public class FiadorRestTest {

    private static final String USUARIO_TESTE = "teste";
    private static final Long CODIGO_FIES = 20006508L;
    private static final String CPF = "12345678900";
    private static final Integer AGENCIA = 1234;

    @Mock
    private FiadorService serv;

    @Mock
    private ContratoService contratoServ;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private FiadorRest rest;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(this.securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(USUARIO_TESTE);
    }

    @Test
    public void deveConsultarFiadores() {
        List<Fiador> retornoEsperado = Arrays.asList(new Fiador(), new Fiador());

        when(serv.consultarFiadores(anyString(), anyLong())).thenReturn(retornoEsperado);

        List<Fiador> retorno = rest.consultaFiadores(CODIGO_FIES);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
        verify(serv).consultarFiadores(USUARIO_TESTE, CODIGO_FIES);
    }

    @Test
    public void deveConsultarFiador() {
        Fiador fiador = new Fiador();

        when(serv.consultar(anyString(), anyLong(), anyString())).thenReturn(fiador);

        Fiador retorno = rest.consulta(CODIGO_FIES, CPF);

        assertNotNull(retorno);
        assertSame(fiador, retorno);
        verify(serv).consultar(USUARIO_TESTE, CODIGO_FIES, CPF);
    }

    @Test
    public void deveConsultarFiadoresPorCpfCodFiesAgencia() throws Exception {
        Contrato contrato = new Contrato();
        Estudante estudante = new Estudante();
        estudante.setCodigoFies(CODIGO_FIES);
        contrato.setEstudante(estudante);

        List<Fiador> retornoEsperado = Arrays.asList(new Fiador(), new Fiador());

        when(contratoServ.consulta(anyString(), anyString(), anyLong(), anyInt())).thenReturn(contrato);
        when(serv.consultarFiadores(anyString(), anyLong())).thenReturn(retornoEsperado);

        List<Fiador> retorno = rest.consultaFiadoresPorCpfCodFiesAgencia(CODIGO_FIES, CPF, AGENCIA);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
        verify(contratoServ).consulta(USUARIO_TESTE, CPF, CODIGO_FIES, AGENCIA);
        verify(serv).consultarFiadores(USUARIO_TESTE, CODIGO_FIES);
    }

    @Test
    public void deveConsultarFiadoresPorCpfCodFiesAgenciaQuandoCodigoFiesForNulo() throws Exception {
        Contrato contrato = new Contrato();
        Estudante estudante = new Estudante();
        estudante.setCodigoFies(CODIGO_FIES);
        contrato.setEstudante(estudante);

        List<Fiador> retornoEsperado = Arrays.asList(new Fiador());

        when(contratoServ.consulta(anyString(), anyString(), anyLong(), anyInt())).thenReturn(contrato);
        when(serv.consultarFiadores(anyString(), anyLong())).thenReturn(retornoEsperado);

        List<Fiador> retorno = rest.consultaFiadoresPorCpfCodFiesAgencia(null, CPF, AGENCIA);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
        verify(contratoServ).consulta(USUARIO_TESTE, CPF, 0L, AGENCIA);
        verify(serv).consultarFiadores(USUARIO_TESTE, CODIGO_FIES);
    }

    @Test
    public void deveLancarExcecaoQuandoNaoInformarCpfENemCodigoFies() {
        try {
            rest.consultaFiadoresPorCpfCodFiesAgencia(null, null, AGENCIA);
        } catch (BusinessException e) {
            assertEquals("Informe o CPF ou o Código FIES.", e.getMessage());
        }
    }

    @Test
    public void deveLancarExcecaoQuandoInformarCpfVazioENemCodigoFies() {
        try {
            rest.consultaFiadoresPorCpfCodFiesAgencia(null, "   ", AGENCIA);
        } catch (BusinessException e) {
            assertEquals("Informe o CPF ou o Código FIES.", e.getMessage());
        }
    }

    @Test
    public void deveLancarExcecaoQuandoNaoInformarAgencia() {
        try {
            rest.consultaFiadoresPorCpfCodFiesAgencia(CODIGO_FIES, CPF, null);
        } catch (BusinessException e) {
            assertEquals("Informe a agência.", e.getMessage());
        }
    }

    @Test
    public void deveSalvarFiador() {
        Fiador fiador = new Fiador();
        Retorno retornoEsperado = new Retorno(0L, "Sucesso");

        when(serv.salvar(anyString(), any(Fiador.class))).thenReturn(retornoEsperado);

        Retorno retorno = rest.salva(fiador);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
        verify(serv).salvar(USUARIO_TESTE, fiador);
    }

    @Test
    public void deveExcluirFiador() {
        Fiador fiadorEntrada = new Fiador();
        fiadorEntrada.setCodigoFies(CODIGO_FIES);
        fiadorEntrada.setCpf(CPF);

        Fiador fiadorConsultado = new Fiador();
        fiadorConsultado.setCodigoFies(CODIGO_FIES);
        fiadorConsultado.setCpf(CPF);

        Retorno retornoEsperado = new Retorno(0L, "Sucesso");

        when(serv.consultar(anyString(), anyLong(), anyString())).thenReturn(fiadorConsultado);
        when(serv.excluir(anyString(), any(Fiador.class))).thenReturn(retornoEsperado);

        Retorno retorno = rest.excluir(fiadorEntrada);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
        verify(serv).consultar(USUARIO_TESTE, CODIGO_FIES, CPF);
        verify(serv).excluir(USUARIO_TESTE, fiadorConsultado);
    }

    @Test
    public void deveValidarRenegociacao() {
        SolicitacaoRenegociacaoContratoVO renegociacao = mock(SolicitacaoRenegociacaoContratoVO.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        Retorno retornoEsperado = new Retorno(0L, "Sucesso");

        when(renegociacao.getDadosEstudante().getCpf()).thenReturn(CPF);
        when(renegociacao.getDadosEstudante().getCodFies()).thenReturn(String.valueOf(CODIGO_FIES));
        when(renegociacao.getCodigo()).thenReturn(10L);
        when(serv.validarRenegociacao(anyString(), any(Fiador.class), anyLong())).thenReturn(retornoEsperado);

        Retorno retorno = rest.validar(renegociacao);

        assertNotNull(retorno);
        assertSame(retornoEsperado, retorno);
    }
}
