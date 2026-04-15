package br.gov.caixa.fes.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.gov.caixa.arqrefcore.excecao.BusinessException;
import br.gov.caixa.fes.dominio.Contrato;
import br.gov.caixa.fes.dominio.Fiador;
import br.gov.caixa.fes.dominio.Retorno;
import br.gov.caixa.fes.dominio.renegociacaocontrato.vo.SolicitacaoRenegociacaoContratoVO;
import br.gov.caixa.fes.negocio.ContratoService;
import br.gov.caixa.fes.negocio.FiadorService;

public class FiadorRestTest {

	private static final String USUARIO = "c891803";
	private static final Long CODIGO_FIES = 123456L;
	private static final String CPF = "12345678900";
	private static final Integer AGENCIA = 1234;

	private FiadorRest rest;

	@Mock
	private FiadorService serv;

	@Mock
	private ContratoService contratoServ;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		rest = Mockito.spy(new FiadorRest());

		injetarCampo(rest, "serv", serv);
		injetarCampo(rest, "contratoServ", contratoServ);

		doReturn(USUARIO).when(rest).getUsuarioLogado();
	}

	private void injetarCampo(Object target, String nomeCampo, Object valor) throws Exception {
		Field field = target.getClass().getDeclaredField(nomeCampo);
		field.setAccessible(true);
		field.set(target, valor);
	}

	@Test
	public void deveConsultarFiadores() {
		List<Fiador> retornoEsperado = Arrays.asList(new Fiador(), new Fiador());

		when(serv.consultarFiadores(USUARIO, CODIGO_FIES)).thenReturn(retornoEsperado);

		List<Fiador> retorno = rest.consultaFiadores(CODIGO_FIES);

		assertSame(retornoEsperado, retorno);
		verify(serv).consultarFiadores(USUARIO, CODIGO_FIES);
	}

	@Test
	public void deveConsultarFiadorPorCodigoFiesECpf() {
		Fiador retornoEsperado = new Fiador();

		when(serv.consultar(USUARIO, CODIGO_FIES, CPF)).thenReturn(retornoEsperado);

		Fiador retorno = rest.consulta(CODIGO_FIES, CPF);

		assertSame(retornoEsperado, retorno);
		verify(serv).consultar(USUARIO, CODIGO_FIES, CPF);
	}

	@Test
	public void deveLancarExcecaoQuandoNaoInformarCpfENemCodigoFies() {
		try {
			rest.consultaFiadoresPorCpfCodFiesAgencia(null, null, AGENCIA);
			fail();
		} catch (BusinessException e) {
			assertEquals("Informe o CPF ou o Código FIES.", e.getMessage());
		}
	}

	@Test
	public void deveLancarExcecaoQuandoInformarCpfEmBrancoENemCodigoFies() {
		try {
			rest.consultaFiadoresPorCpfCodFiesAgencia(null, "   ", AGENCIA);
			fail();
		} catch (BusinessException e) {
			assertEquals("Informe o CPF ou o Código FIES.", e.getMessage());
		}
	}

	@Test
	public void deveLancarExcecaoQuandoNaoInformarAgencia() {
		try {
			rest.consultaFiadoresPorCpfCodFiesAgencia(CODIGO_FIES, CPF, null);
			fail();
		} catch (BusinessException e) {
			assertEquals("Informe a agência.", e.getMessage());
		}
	}

	@Test
	public void deveConsultarFiadoresPorCpfCodFiesAgenciaComCodigoFiesInformado() throws Exception {
		Contrato contrato = Mockito.mock(Contrato.class, Mockito.RETURNS_DEEP_STUBS);
		List<Fiador> retornoEsperado = Arrays.asList(new Fiador(), new Fiador());

		when(contrato.getEstudante().getCodigoFies()).thenReturn(CODIGO_FIES);
		when(contratoServ.consulta(USUARIO, CPF, CODIGO_FIES, AGENCIA)).thenReturn(contrato);
		when(serv.consultarFiadores(USUARIO, CODIGO_FIES)).thenReturn(retornoEsperado);

		List<Fiador> retorno = rest.consultaFiadoresPorCpfCodFiesAgencia(CODIGO_FIES, CPF, AGENCIA);

		assertSame(retornoEsperado, retorno);
		verify(contratoServ).consulta(USUARIO, CPF, CODIGO_FIES, AGENCIA);
		verify(serv).consultarFiadores(USUARIO, CODIGO_FIES);
	}

	@Test
	public void deveConsultarFiadoresPorCpfCodFiesAgenciaQuandoCodigoFiesForNulo() throws Exception {
		Contrato contrato = Mockito.mock(Contrato.class, Mockito.RETURNS_DEEP_STUBS);
		List<Fiador> retornoEsperado = Arrays.asList(new Fiador());

		when(contrato.getEstudante().getCodigoFies()).thenReturn(CODIGO_FIES);
		when(contratoServ.consulta(USUARIO, CPF, 0L, AGENCIA)).thenReturn(contrato);
		when(serv.consultarFiadores(USUARIO, CODIGO_FIES)).thenReturn(retornoEsperado);

		List<Fiador> retorno = rest.consultaFiadoresPorCpfCodFiesAgencia(null, CPF, AGENCIA);

		assertSame(retornoEsperado, retorno);
		verify(contratoServ).consulta(USUARIO, CPF, 0L, AGENCIA);
		verify(serv).consultarFiadores(USUARIO, CODIGO_FIES);
	}

	@Test
	public void deveSalvarFiador() {
		Fiador fiador = new Fiador();
		Retorno retornoEsperado = new Retorno();

		when(serv.salvar(USUARIO, fiador)).thenReturn(retornoEsperado);

		Retorno retorno = rest.salva(fiador);

		assertSame(retornoEsperado, retorno);
		verify(serv).salvar(USUARIO, fiador);
	}

	@Test
	public void deveExcluirFiador() {
		Fiador fiadorEntrada = new Fiador();
		fiadorEntrada.setCodigoFies(CODIGO_FIES);
		fiadorEntrada.setCpf(CPF);

		Fiador fiadorConsultado = new Fiador();
		fiadorConsultado.setCodigoFies(CODIGO_FIES);
		fiadorConsultado.setCpf(CPF);

		Retorno retornoEsperado = new Retorno();

		when(serv.consultar(USUARIO, CODIGO_FIES, CPF)).thenReturn(fiadorConsultado);
		when(serv.excluir(USUARIO, fiadorConsultado)).thenReturn(retornoEsperado);

		Retorno retorno = rest.excluir(fiadorEntrada);

		assertSame(retornoEsperado, retorno);
		verify(serv).consultar(USUARIO, CODIGO_FIES, CPF);
		verify(serv).excluir(USUARIO, fiadorConsultado);
	}

	@Test
	public void deveValidarRenegociacao() {
		SolicitacaoRenegociacaoContratoVO renegociacao =
				Mockito.mock(SolicitacaoRenegociacaoContratoVO.class, Mockito.RETURNS_DEEP_STUBS);

		Retorno retornoEsperado = new Retorno();

		when(renegociacao.getDadosEstudante().getCpf()).thenReturn(CPF);
		when(renegociacao.getDadosEstudante().getCodFies()).thenReturn(String.valueOf(CODIGO_FIES));
		when(renegociacao.getCodigo()).thenReturn(10L);

		when(serv.validarRenegociacao(
				eq(USUARIO),
				argThat(new org.mockito.ArgumentMatcher<Fiador>() {
					@Override
					public boolean matches(Object argument) {
						Fiador fiador = (Fiador) argument;
						return fiador != null
								&& CPF.equals(fiador.getCpf())
								&& CODIGO_FIES.equals(fiador.getCodigoFies());
					}
				}),
				eq(10L)))
			.thenReturn(retornoEsperado);

		Retorno retorno = rest.validar(renegociacao);

		assertSame(retornoEsperado, retorno);
		verify(serv).validarRenegociacao(
				eq(USUARIO),
				argThat(new org.mockito.ArgumentMatcher<Fiador>() {
					@Override
					public boolean matches(Object argument) {
						Fiador fiador = (Fiador) argument;
						return fiador != null
								&& CPF.equals(fiador.getCpf())
								&& CODIGO_FIES.equals(fiador.getCodigoFies());
					}
				}),
				eq(10L));
	}
}
