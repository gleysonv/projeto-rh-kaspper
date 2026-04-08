	private Retorno salvar(String operacao, String usuario, Fiador fiador, Long codigoSolicitacao) {
		Retorno wR = new Retorno();
		wR.setCodigo(0L);

		String wParametro = ":0, :1, :2, :3, :4, :5, :6, :7, :8, :9, ";
		wParametro += ":10, :11, :12, :13, :14, :15, :16, :17, :18, :19, ";
		wParametro += ":20, :21, :22, :23, :24, :25, :26, :27, :28, :29, ";
		wParametro += ":30, :31, :32, :33, :34, :35, :36, :37, :38, :39, ";
		wParametro += ":40, :41, :42 ";
		Query qr;
		if (codigoSolicitacao == null) {
			qr = super.getEm().createNativeQuery("{call FES.FESSP947_INCLUI_FIADOR ( " + wParametro + ") }");
		}else {
			wParametro+=", :43 ";
			qr = super.getEm().createNativeQuery("{call FES.FESSPZC9_PRE_FIADOR_RNGCCO ( " + wParametro + ") }");

		}
		

		int wTemRestricao = 0;

		if (!operacao.equals("E")){
			if (fiador.getNome().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Nome não foi informado!");
				return wR;
			}
	
			if (fiador.getCpf().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("CPF não foi informado!");
				return wR;
			}
	
			if (fiador.getDataNascimento().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Data nascimento não foi informado!");
				return wR;
			}
	
			if (fiador.getIdentidade().getIdentidade().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Identidade não foi informado!");
				return wR;
			}
	
			if (fiador.getIdentidade().getOrgaoExpedidor().getCodigo() < 1) {
				wR.setCodigo(1L);
				wR.setMensagem("Orgão expedidor da identidade não foi informado!");
				return wR;
			}
	
			if (fiador.getIdentidade().getUf().getSigla().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("UF do Orgão expedidor da Identidade não foi informado!");
				return wR;
			}
	
			if (fiador.getIdentidade().getDataExpedicaoIdentidade().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Data da expedição da identidade não foi informado!");
				return wR;
			}
	
			if (fiador.getEndereco().getEndereco().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Endereço não foi informado!");
				return wR;
			}
	
			if (fiador.getEndereco().getCidade().getCodigoCidade() < 0) {
				wR.setCodigo(1L);
				wR.setMensagem("Cidade não foi informado!");
				return wR;
			}
	
			if (fiador.getEndereco().getCidade().getUf().getSigla().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("UF não foi informado!");
				return wR;
			}
	
			if (fiador.getEstadoCivil().getCodigo() == 2 || fiador.getEstadoCivil().getCodigo() == 9) {
				if (fiador.getConjuge().getNome().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("UF não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getCpf().trim().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("CPF do conjuge não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getDataNascimento().trim().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("Data nascimento do conjuge não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getIdentidade().getIdentidade().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("Identidade do conjuge não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getIdentidade().getOrgaoExpedidor().getCodigo() < 0) {
					wR.setCodigo(1L);
					wR.setMensagem("Orgão expedidor da identidade do conjuge não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getIdentidade().getUf().getSigla().trim().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("UF do Orgão expedidor da Identidade do conjuge não foi informado!");
					return wR;
				}
	
				if (fiador.getConjuge().getIdentidade().getDataExpedicaoIdentidade().trim().equals("")) {
					wR.setCodigo(1L);
					wR.setMensagem("Data da expedição da identidade do conjuge não foi informado!");
					return wR;
				}
			}
	
			if (fiador.getCodigoProfissao().trim().equals("")) {
				wR.setCodigo(1L);
				wR.setMensagem("Profissão não foi informado!");
				return wR;
			}
	
			qr.setParameter("0", "C");

			// Verifica a Idoneidade do Fiador
			try {
				if(codigoSolicitacao==null || codigoSolicitacao==0L) {
					ResumoPesquisaCadastral wR2 = sipesBean.consultar(fiador.getCodigoFies(), fiador.getCpf(), usuario);
					if (wR2 != null) {
						if (wR2.getSinad() != null)
							wTemRestricao = Boolean.TRUE.equals(wR2.getSinad().getExisteRestricao()) ? 1 : 0;
	
						if (wR2.getCadin() != null)
							wTemRestricao = Boolean.TRUE.equals(wR2.getCadin().getExisteRestricao()) ? 1 : 0;
	
						if (wR2.getSerasa() != null)
							wTemRestricao = Boolean.TRUE.equals(wR2.getSerasa().getExisteRestricao()) ? 1 : 0;
	
						if (wR2.getCcf() != null)
							wTemRestricao = Boolean.TRUE.equals(wR2.getCcf().getExisteRestricao()) ? 1 : 0;
	
						if (wR2.getSpc() != null)
							wTemRestricao = Boolean.TRUE.equals(wR2.getSpc().getExisteRestricao()) ? 1 : 0;
					}
				}
			} catch (Exception e) {
				getEm().flush();
				logger.log(Level.SEVERE, "Falha ao salvar fiador.", e);
			}
		} else {
			qr.setParameter("0", "E");
		}
	
		qr.setParameter("1", usuario.toUpperCase());
		qr.setParameter("2", fiador.getCodigoFies());
		qr.setParameter("3", getNotNullString(fiador.getNome()));
		qr.setParameter("4", fiador.getCpf());
		qr.setParameter("5", fiador.getDependenteCPF());
		qr.setParameter("6", getNotNullString(fiador.getDataNascimento()));

		qr.setParameter("7", getNotNullString(fiador.getIdentidade().getIdentidade()));
		qr.setParameter("8", getNotNullNumeric(fiador.getIdentidade().getOrgaoExpedidor().getCodigo()));
		qr.setParameter("9", getNotNullString(fiador.getIdentidade().getUf().getSigla()));
		qr.setParameter("10", getNotNullString(fiador.getIdentidade().getDataExpedicaoIdentidade()));

		qr.setParameter("11", getNotNullString(FesUtil.removerCaractereDiferenteDeNumero(fiador.getRic())));
		qr.setParameter("12", getNotNullString(fiador.getNacionalidade().getCodigo()));

		if (fiador.getEmancipado() == null || StringUtils.isBlank(fiador.getEmancipado().getCodigo()))
			qr.setParameter("13", "N");
		else
			qr.setParameter("13", "S");

		qr.setParameter("14", fiador.getEmancipado() != null && fiador.getEmancipado().getCodigo().equals("MAI") ? "" : fiador.getEmancipado().getCodigo());
		qr.setParameter("15", getNotNullString(fiador.getEndereco().getEndereco()));
		qr.setParameter("16", getNotNullString(fiador.getEndereco().getBairro()));
		qr.setParameter("17", getNotNullNumeric(fiador.getEndereco().getCidade().getCodigoCidade()));
		qr.setParameter("18", getNotNullString(fiador.getEndereco().getCidade().getUf().getSigla()));
		qr.setParameter("19", getNotNullString(fiador.getEndereco().getCep()));
		qr.setParameter("20", getNotNullString(fiador.getContato().getTelefoneResidencial().getDdd()));
		qr.setParameter("21", getNotNullString(fiador.getContato().getTelefoneResidencial().getNumero()));
		qr.setParameter("22", fiador.getSequencial());
		qr.setParameter("23", fiador.getEstadoCivil().getCodigo());
		qr.setParameter("24", fiador.getRegimeBens().getCodigo());

		if (fiador.getEstadoCivil().getCodigo() == 2 || fiador.getEstadoCivil().getCodigo() == 9) {
			qr.setParameter("25", fiador.getConjuge().getNome());
			qr.setParameter("26", fiador.getConjuge().getCpf());
			qr.setParameter("27", fiador.getConjuge().getDependenteCPF());
			qr.setParameter("28", fiador.getConjuge().getDataNascimento());
			qr.setParameter("29", fiador.getConjuge().getIdentidade().getIdentidade());
			qr.setParameter("30", fiador.getConjuge().getIdentidade().getOrgaoExpedidor().getCodigo());
			qr.setParameter("31", fiador.getConjuge().getIdentidade().getUf().getSigla());
			qr.setParameter("32", fiador.getConjuge().getIdentidade().getDataExpedicaoIdentidade());
			qr.setParameter("33", fiador.getConjuge().getRic());
			qr.setParameter("34", fiador.getConjuge().getNacionalidade().getCodigo());
		} else {
			qr.setParameter("25", "");
			qr.setParameter("26", "");
			qr.setParameter("27", 0);
			qr.setParameter("28", "");
			qr.setParameter("29", "");
			qr.setParameter("30", "");
			qr.setParameter("31", "");
			qr.setParameter("32", "");
			qr.setParameter("33", "");
			qr.setParameter("34", "");
		}

		qr.setParameter("35", getNotNullString(fiador.getContato().getTelefoneComercial().getDdd()));
		qr.setParameter("36", getNotNullString(fiador.getContato().getTelefoneComercial().getNumero()));
		qr.setParameter("37", getNotNullString(fiador.getContato().getTelefoneCelular().getDdd()));
		qr.setParameter("38", getNotNullString(fiador.getContato().getTelefoneCelular().getNumero()));
		qr.setParameter("39", fiador.getValorRendaMensal());
		qr.setParameter("40", 0);
		qr.setParameter("41", getNotNullNumeric(fiador.getCodigoProfissao()));
		qr.setParameter("42", wTemRestricao); 
		
		if (codigoSolicitacao != null)
			qr.setParameter("43", codigoSolicitacao);

		try {
			qr.executeUpdate();

			wR = new Retorno(0L, "Comando realizado com sucesso!");

		} catch (PersistenceException e) {
			logger.severe(e.getMessage());
			wR.setCodigo(1L);
			String wMsg = "Ocorreu um erro na realização do comando, tente novamente!";
			if(e.getCause() instanceof java.sql.SQLException || e.getCause().getCause() instanceof java.sql.SQLException){
				wMsg = e.getCause().getCause().getMessage().split("\n")[0].split(": ", 2)[1];
				logger.severe(wMsg);
			}
			wR.setMensagem(wMsg);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			wR.setCodigo(1L);
			wR.setMensagem("Ocorreu um erro na realização do comando, tente novamente!");
		}
		if (operacao.equals("I") || operacao.equals("E") ) {
			Retorno retornoSIAPI = comunicaSIAPI(fiador.getCodigoFies(), usuario);
		}
		return wR;
	}


       
       
       
       
       private Retorno comunicaSIAPI(Long codigoFies, String usuario) {
		Retorno retorno = new Retorno();
        String w120 = null;
        try {
            w120 = contratoBean.exportaOnline120(codigoFies);
			logger.severe("Comunicação de alteração de fiadors ao SIAPI:  "+w120.toString());

			if (w120 != null && !w120.isEmpty()) {
				Padrao wTransacao120 = new Padrao(w120);
				wTransacao120.setCodigoFies(codigoFies);
				wTransacao120.setUsuario(usuario.toUpperCase());
				Padrao wTransacaoRetorno120 = api.executeAPIPB699Codigo(wTransacao120);

				if (Integer.parseInt(wTransacaoRetorno120.getCodigoRetorno()) != 0) {
					retorno.setCodigo(Long.parseLong(wTransacaoRetorno120.getCodigoRetorno()));
					if (wTransacaoRetorno120.getCodigoErroTransacaoSiapi() == null)
						wTransacaoRetorno120.setCodigoErroTransacaoSiapi("");

					retorno.setMensagem("SIAPI #" + wTransacaoRetorno120.getCodigoErroTransacaoSiapi() + "-" + wTransacaoRetorno120.getMensagem());
					ocorrenciaRejeicaoBean.gravarOcorrenciaRejeicao(codigoFies, TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO,
							SituacaoOcorrenciaRejeicao.ENVIADO_BANCO, retorno.getMensagem(), null, null);
					return retorno;
				}
			}
		} catch (BusinessException e) {
			throw new RuntimeException(e);
		}
        return retorno;
    }


package br.gov.caixa.fes.negocio;

import br.gov.caixa.arqrefcore.excecao.BusinessException;
import br.gov.caixa.est.util.FESException;
import br.gov.caixa.fes.BaseSIFESTest;
import br.gov.caixa.fes.dominio.*;
import br.gov.caixa.fes.dominio.transicao.FiadorConsultaTO;
import br.gov.caixa.fes.dominio.transicao.FiadorSpcTO;
import br.gov.caixa.fes.dto.contrato.FiadorContratacao;
import br.gov.caixa.fes.dto.contrato.FiadorContratacaoComplementar;
import br.gov.caixa.fes.dto.contrato.ValidacaoFiadorContratacaoApp;
import br.gov.caixa.fes.dto.contrato.fiador.FiadorRenda;
import br.gov.caixa.fes.dto.contrato.fiador.FiadorRetorno;
import br.gov.caixa.fes.integracao.CicsSFWPO401;
import br.gov.caixa.fes.transacao.PadraoRetornoSICPF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.experimental.theories.suppliers.TestedOn;import org.mockito.Mockito;
@RunWith(MockitoJUnitRunner.class)
public class FiadorBeanTest extends BaseSIFESTest {

    public static final String CPF_CONJUGE = "123456786";
    public static final String FIADOR_CADASTRADO_COM_SUCESSO = "Fiador cadastrado com sucesso.";
    public static final String CPF1 = "111";
    public static final String CONJUGE = "222";
    public static final String NOME1 = "N1";
    public static final String C_1 = "C1";
    public static final String CPF2 = "333";
    public static final String NOME2 = "N2";
    public static final String CPF_CONJUGE1 = "444";
    public static final String NOME_CONJUGE = "C2";
    public final Long CODIGO_FIES = 1L;
    public final String CPF = "00011122233";
    public final String EMAIL = "teste@caix.gov.br";

    @Spy
    @InjectMocks
    private FiadorBean bean = new FiadorBean();

    @Mock
    public CicsSFWPO401 cicsSFWPO401;

    @Mock
    public ContratoService contratoService;

    @Mock
    public SipesBean sipesBean;

    @Mock
    public AuditoriaBean auditoriaBean;

    // Constantes auxiliares
    private static final String EMPTY = "";
    private static final String NO_ENTITY = "No entity found for query";
    private static final String NOME = "JOAO";
    private static final String DATA = "01/01/2000";

    // ============================================
    // Novas constantes - [Valores Comuns de Teste]
    // ============================================
    private static final String UF_DF = "DF";
    private static final String USUARIO = "USR";
    private static final String EXCEPTION_MESSAGE_X = "x";
    private static final String DATE_01_01_2001 = "01/01/2001";
    private static final String DDD_61 = "61";
    private static final String NUM_TITULO_12345 = "12345";
    private static final String CODIGO_123 = "123";
    private static final String PAIS_BR = "BR";
    private static final String MSG_COMANDO_SUCESSO = "Comando realizado com sucesso!";
    private static final String NOME_CARLINHA = "CARLINHA";
    private static final String DATA_01_01_2000_DOTS = "01.01.2000";

    // ============================================
    // Novas constantes - [Mensagens]
    // ============================================
    private static final String MSG_ERRO = "Erro";
    private static final String MSG_UF_NAO_INFORMADO = "UF não foi informado!";
    private static final String MSG_NAO_EXISTE_FIADORES = "Não existe fiadores cadastrado para o candidato.";
    public static final String STRING_QUALQUER = "qualquer";
    public static final Integer INTEGER_QUALQUER = 12;
    // ================== TESTES JÁ EXISTENTES / BÁSICOS ==================

    @Test
    public void testGetFiadorPorCpfValido() throws ParseException {
        FiadorConsultaTO to = new FiadorConsultaTO();
        to.setCpf("123456789");
        to.setNome("teste 1");
        to.setDataNascimento(new Date());
        to.setEmailFiador("email1@dominio.com");
        List<FiadorConsultaTO> result = new ArrayList<>();
        result.add(to);
        when(query.getResultList()).thenReturn(result);
        SolicitacaoAcesso acesso = bean.getFiadorPorCpf(anyString());
        assertNotNull(acesso);
    }

    @Test
    public void testGetFiadorPorCpfNull() throws ParseException {
        when(query.getResultList()).thenReturn(null);
        SolicitacaoAcesso acesso = bean.getFiadorPorCpf(anyString());
        Assert.assertNull(acesso);
    }

    @Test
    public void testGetFiadorPorCpfComExcecao() {
        when(query.getResultList()).thenThrow(new RuntimeException(MSG_ERRO));
        try {
            SolicitacaoAcesso acesso = bean.getFiadorPorCpf(anyString());
            Assert.assertNull(acesso);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetConjugeFiadorPorCpfConjugeValido() throws ParseException {
        FiadorConsultaTO to = new FiadorConsultaTO();
        to.setCpfConjuge(CPF_CONJUGE);
        to.setNomeConjuge("teste 3");
        to.setDataNacimentoConjuge(new Date());
        to.setEmailConjuge("email3@dominio.com");
        when(query.getSingleResult()).thenReturn(to);
        SolicitacaoAcesso acesso = bean.getConjugeFiadorPorCpfConjuge(anyString());
        assertNotNull(acesso);
    }

    @Test
    public void testGetConjugeFiadorPorCpfConjugeNull() throws ParseException {
        when(query.getResultList()).thenReturn(null);
        SolicitacaoAcesso acesso = bean.getConjugeFiadorPorCpfConjuge(anyString());
        Assert.assertNull(acesso);
    }

    @Test
    public void testGetConjugeFiadorPorCpfConjugeComExcecao() {
        when(query.getSingleResult()).thenThrow(new RuntimeException(MSG_ERRO));
        try {
            SolicitacaoAcesso acesso = bean.getConjugeFiadorPorCpfConjuge(anyString());
            Assert.assertNull(acesso);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void listarFiador_quandoCodigoInvalido_retornaMensagem() throws Exception {
        FiadorRetorno ret = bean.listarFiador(-1L);
        assertNotNull(ret);
        assertNotNull(ret.getMensagem());
    }

    @Test
    public void consultarSPC_quandoExcecao_retornaListaVazia() throws Exception {
        when(query.getResultList()).thenThrow(new RuntimeException(NO_ENTITY));
        List<Fiador> lista = bean.consultarSPC(UF_DF, CODIGO_123);
        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    public void alterarFiadorApp_quandoSucesso_retornaMensagemOk() throws Exception {
        FiadorContratacao fiador = new FiadorContratacao();
        fiador.setCpf(CPF);
        fiador.setEmail("a@a.com");
        fiador.setRendaFiador(new BigDecimal("1000"));
        when(query.getSingleResult()).thenReturn(new BigDecimal(1)); // validaFiadorExistente
        when(query.executeUpdate()).thenReturn(1); // alterarFiador
        Retorno ret = bean.alterarFiadorApp(fiador, CODIGO_FIES);
        assertNotNull(ret);
        assertEquals(Long.valueOf(0), ret.getCodigo());
    }

    @Test(expected = BusinessException.class)
    public void alterarFiadorApp_quandoEmailInvalido_lancaBusinessException() throws Exception {
        FiadorContratacao fiador = new FiadorContratacao();
        fiador.setCpf(CPF);
        fiador.setEmail(EXCEPTION_MESSAGE_X); // inválido
        when(query.getSingleResult()).thenReturn(new BigDecimal(1));
        bean.alterarFiadorApp(fiador, CODIGO_FIES);
    }

    @Test
    public void validarCriteriosFiador_quandoCpfNaoEncontrado_retornaComRestricao() throws Exception {
        when(query.getResultList()).thenReturn(Collections.emptyList());
        when(cicsSFWPO401.buscarDadosSICPF(any(PadraoRetornoSICPF.class))).thenReturn(new PadraoRetornoSICPF());
        ValidacaoFiadorContratacaoApp val = bean.validarCriteriosFiador(CPF, 1L, CPF);
        assertNotNull(val);
        assertEquals(Long.valueOf(-2), val.getCodigo());
        assertNotNull(val.getListaValidacao());
        assertFalse(val.getListaValidacao().isEmpty());
    }

    @Test
    public void consultarFiadores_quandoExcecao_retornaListaComMensagem() {
        when(query.getResultList()).thenThrow(new RuntimeException(NO_ENTITY));
        List<Fiador> lista = bean.consultarFiadores(USUARIO, 1L);
        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertNotNull(lista.get(0).getMensagem());
    }

    @Test
    public void mustReturnSuccessIncluirFiadorApp() throws FESException, BusinessException {
        final Long CODIOG_RETORNO = 0L;
        final String MSG_SUCCESS = FIADOR_CADASTRADO_COM_SUCESSO;

        FiadorContratacao fiador = new FiadorContratacao();
        fiador.setCpf(CPF);

        PadraoRetornoSICPF padraoSicpf = new PadraoRetornoSICPF();
        padraoSicpf.setCpf(CPF);
        padraoSicpf.setUsuario(CPF);
        padraoSicpf.setNome(NOME_CARLINHA);
        padraoSicpf.setNuTitulo(NUM_TITULO_12345);
        padraoSicpf.setDataNascimento(DATA_01_01_2000_DOTS);
        padraoSicpf.setSituacaoCPF(1);

        when(cicsSFWPO401.buscarDadosSICPF((PadraoRetornoSICPF) anyObject())).thenReturn(padraoSicpf);
        when(query.executeUpdate()).thenReturn(1);
        when(contratoService.gravarDadosParticipantesExtenosContrato(anyString(), anyInt(), anyLong(), anyInt(), anyString())).thenReturn(1);

        Retorno retorno = bean.incluirFiadorApp(fiador, CODIGO_FIES);

        assertNotNull(retorno);
        assertEquals(CODIOG_RETORNO, retorno.getCodigo());
        assertEquals(MSG_SUCCESS, retorno.getMensagem());

        verify(query, times(1)).executeUpdate();
    }

    @Test
    public void mustReturnErrorIncluirFiadorApp() throws FESException, BusinessException {
        final Long CODIOG_RETORNO = -1L;
        final String MSG_ERROR = "CPF informado com restrição. Favor regularizar o CPF para continuar a contratação!";

        FiadorContratacao fiador = new FiadorContratacao();
        fiador.setCpf(CPF);

        PadraoRetornoSICPF padraoSicpf = new PadraoRetornoSICPF();
        padraoSicpf.setCpf(CPF);
        padraoSicpf.setUsuario(CPF);
        padraoSicpf.setNome(NOME_CARLINHA);
        padraoSicpf.setNuTitulo(NUM_TITULO_12345);
        padraoSicpf.setDataNascimento(DATA_01_01_2000_DOTS);
        padraoSicpf.setSituacaoCPF(0);

        when(cicsSFWPO401.buscarDadosSICPF((PadraoRetornoSICPF) anyObject())).thenReturn(padraoSicpf);
        Retorno retorno = bean.incluirFiadorApp(fiador, CODIGO_FIES);

        assertNotNull(retorno);
        assertEquals(CODIOG_RETORNO, retorno.getCodigo());
        assertEquals(MSG_ERROR, retorno.getMensagem());
    }

    @Test
    public void mustReturnExceptionIncluirFiadorApp() throws FESException {
        FiadorContratacao fiador = new FiadorContratacao();
        fiador.setCpf(CPF);

        when(cicsSFWPO401.buscarDadosSICPF((PadraoRetornoSICPF) anyObject())).thenReturn(null);

        try {
            bean.incluirFiadorApp(fiador, CODIGO_FIES);
        } catch (BusinessException e) {
            verify(cicsSFWPO401, times(1)).buscarDadosSICPF(Matchers.<PadraoRetornoSICPF>any());
        }
    }

    @Test
    public void popularDadosFiador_quandoSucesso_retornaFiadorPreenchido() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new Object[]{CODIGO_FIES, NOME, CPF, DATA});
        Fiador fiador = bean.popularDadosFiador(CPF);
        assertNotNull(fiador);
        assertEquals(CODIGO_FIES, fiador.getCodigoFies());
        assertEquals(NOME, fiador.getNome());
        assertEquals(CPF, fiador.getCpf());
        assertEquals(DATA, fiador.getDataNascimento());
    }

    @Test
    public void popularDadosFiador_quandoExcecao_retornaNull() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new RuntimeException("ERR"));
        Fiador fiador = bean.popularDadosFiador(CPF);
        assertNull(fiador);
    }

    @Test
    public void possuiFiadorParaCandidato_quandoExiste_retornaTrue() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1);
        assertTrue(bean.possuiFiadorParaCandidato(CODIGO_FIES, CPF));
    }

    @Test
    public void possuiFiadorParaCandidato_quandoNaoExisteOuExcecao_retornaFalse() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0);
        assertFalse(bean.possuiFiadorParaCandidato(CODIGO_FIES, CPF));

        when(query.getSingleResult()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE_X));
        assertFalse(bean.possuiFiadorParaCandidato(CODIGO_FIES, CPF));
    }

    @Test
    public void excluir_quandoSucesso_retornaTrue() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        assertTrue(bean.excluir(CODIGO_FIES, CPF));
    }

    @Test
    public void excluir_quandoNadaExcluido_retornaFalse() {
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);
        assertFalse(bean.excluir(CODIGO_FIES, CPF));
    }

    @Test
    public void consultarFiadores_quandoSucessoListaVazia_retornaListaVazia() {
        when(query.getResultList()).thenReturn(new ArrayList<>());
        List<Fiador> lista = bean.consultarFiadores(USUARIO, 1L);
        assertNotNull(lista);
        assertTrue(lista.isEmpty());
    }

    @Test
    public void consultar_quandoEncontrado_retornaFiador() {
        Fiador f = new Fiador();
        f.setCpf(CPF);
        List<Fiador> lista = new ArrayList<>();
        lista.add(f);
        // bean já é um @Spy; evitar criar novo spy sobre classe já enriquecida por CGLIB
        doReturn(lista).when(bean).consultarFiadores(anyString(), anyLong());
        Fiador retorno = bean.consultar(USUARIO, 1L, CPF);
        assertNotNull(retorno);
        assertEquals(CPF, retorno.getCpf());
    }

    @Test
    public void consultar_quandoNaoEncontrado_retornaMensagem() {
        // bean já é um @Spy; utilizar diretamente para evitar MockitoException
        // Ao stubbar método de spy, utilizar doReturn(...).when(spy) para evitar execução real
        doReturn(Collections.<Fiador>emptyList()).when(bean).consultarFiadores(anyString(), anyLong());
        Fiador retorno = bean.consultar(USUARIO, 1L, CPF);
        assertNotNull(retorno);
        assertEquals(Long.valueOf(1L), retorno.getCodigo());
        assertNotNull(retorno.getMensagem());
    }

    @Test
    public void consultarSPC_quandoSucesso_retornaListaComDados() {
        FiadorSpcTO to1 = new FiadorSpcTO();
        to1.setCpf(CPF1);
        to1.setNome(NOME1);
        to1.setCpfConjuge(CONJUGE);
        to1.setNomeConjuge(C_1);
        FiadorSpcTO to2 = new FiadorSpcTO();
        to2.setCpf(CPF2);
        to2.setNome(NOME2);
        to2.setCpfConjuge(CPF_CONJUGE1);
        to2.setNomeConjuge(NOME_CONJUGE);
        when(query.getResultList()).thenReturn(Arrays.asList(to1, to2));
        List<Fiador> lista = bean.consultarSPC(UF_DF, CODIGO_123);
        assertEquals(2, lista.size());
        assertEquals(CPF1, lista.get(0).getCpf());
        assertEquals(NOME1, lista.get(0).getNome());
        assertEquals(CPF2, lista.get(1).getCpf());
        assertEquals(NOME2, lista.get(1).getNome());
    }

    @Test
    public void listarFiador_quandoSemRegistros_retornaMensagemAdequada() throws Exception {
        when(query.getResultList()).thenReturn(Collections.emptyList());
        FiadorRetorno ret = bean.listarFiador(1L);
        assertNotNull(ret);
        assertEquals(MSG_NAO_EXISTE_FIADORES, ret.getMensagem());
    }

    @Test(expected = BusinessException.class)
    public void incluirFiadorComplementar_quandoDadosObrigatoriosFaltando_lancaBusinessException() throws Exception {
        FiadorContratacaoComplementar fiador = new FiadorContratacaoComplementar();
        bean.incluirFiadorComplementar(fiador);
    }

    // ================== NOVOS TESTES: salvar(...) e renegociação ==================

    private Fiador buildFiadorValido() {
        Nacionalidade nacionalidade = new Nacionalidade(PAIS_BR, PAIS_BR);
        OrgaoExpedidor orgao = new OrgaoExpedidor(10);
        UF ufId = new UF(UF_DF);
        Identidade id = new Identidade("RG1", orgao, ufId, DATE_01_01_2001);
        EstadoCivil ec = new EstadoCivil(1);
        RegimeBens rb = new RegimeBens(1);
        UF ufEnd = new UF(UF_DF);
        Cidade cid = new Cidade(1, "Brasilia", ufEnd);
        Endereco end = new Endereco("Rua 1", "", "Bairro", "70000000", cid);
        Telefone tr = new Telefone(DDD_61, "1111");
        Telefone tc = new Telefone(DDD_61, "2222");
        Telefone tb = new Telefone(DDD_61, "3333");
        Contato contato = new Contato("", tr, tc, tb);
        Emancipado em = new Emancipado("NA");
        Conjuge cj = new Conjuge();
        cj.setNome("CONJUGE");
        cj.setCpf("999");
        cj.setDataNascimento(DATA);
        cj.setIdentidade(new Identidade("RG2", new OrgaoExpedidor(11), new UF(UF_DF), DATE_01_01_2001));
        Fiador f = new Fiador(1L, CPF, 0, "NOME", "01/01/1990", "RIC", nacionalidade, id, ec, rb, end, contato, cj, em);
        f.setConjuge(cj);
        f.setCodigoProfissao(CODIGO_123);
        f.setSequencial(1);
        f.setValorRendaMensal(1000f);
        return f;
    }

    @Test
    public void salvarRenegociacao_quandoSucesso_retornaSucesso() {
        Fiador f = buildFiadorValido();
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        Retorno r = bean.salvarRenegociacao(USUARIO, f, 10L);
        assertEquals(Long.valueOf(0), r.getCodigo());
        assertEquals(MSG_COMANDO_SUCESSO, r.getMensagem());
    }

    @Test
    public void validarRenegociacao_quandoSucesso_retornaSucesso() {
        Fiador f = buildFiadorValido();
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        Retorno r = bean.validarRenegociacao(USUARIO, f, 10L);
        assertEquals(Long.valueOf(0), r.getCodigo());
        assertEquals(MSG_COMANDO_SUCESSO, r.getMensagem());
    }

    @Test
    public void excluirRenegociacao_quandoSucesso_retornaSucesso() {
        Fiador f = buildFiadorValido();
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        Retorno r = bean.excluirRenegociacao(USUARIO, f, 10L);
        assertEquals(Long.valueOf(0), r.getCodigo());
        assertEquals(MSG_COMANDO_SUCESSO, r.getMensagem());
    }

    @Test
    public void salvar_quandoPersistenceExceptionComSQLException_retornaMensagemRaiz() {
        Fiador f = buildFiadorValido();
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        java.sql.SQLException sqlEx = new java.sql.SQLException("ORA-00001: valor duplicado");
        RuntimeException nested = new RuntimeException(sqlEx);
        javax.persistence.PersistenceException pe = new javax.persistence.PersistenceException(nested);
        when(query.executeUpdate()).thenThrow(pe);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("valor duplicado", r.getMensagem());
    }

    @Test
    public void salvar_quandoExceptionGenerica_retornaMensagemPadrao() {
        Fiador f = buildFiadorValido();
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE_X));
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Ocorreu um erro na realização do comando, tente novamente!", r.getMensagem());
    }

    @Test
    public void salvar_quandoNomeVazio_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.setNome(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Nome não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoCpfVazio_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.setNome("NOME");
        f.setCpf(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("CPF não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoDataNascVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.setDataNascimento(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Data nascimento não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoIdentidadeVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getIdentidade().setIdentidade(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Identidade não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoOrgaoExpedidorInvalido_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getIdentidade().getOrgaoExpedidor().setCodigo(0);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Orgão expedidor da identidade não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoUfIdentidadeVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getIdentidade().getUf().setSigla(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("UF do Orgão expedidor da Identidade não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoDataExpedicaoVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getIdentidade().setDataExpedicaoIdentidade(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Data da expedição da identidade não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEnderecoVazio_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getEndereco().setEndereco(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Endereço não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoCidadeInvalida_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getEndereco().getCidade().setCodigoCidade(-1);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Cidade não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoUfEnderecoVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.getEndereco().getCidade().getUf().setSigla(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals(MSG_UF_NAO_INFORMADO, r.getMensagem());
    }

    @Test
    public void salvar_quandoProfissaoVazia_retornaMensagem() {
        Fiador f = buildFiadorValido();
        f.setCodigoProfissao(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Profissão não foi informado!", r.getMensagem());
    }

    private Fiador buildFiadorComConjugeObrigatorio() {
        Fiador f = buildFiadorValido();
        f.getEstadoCivil().setCodigo(2); // ativa validações do cônjuge
        return f;
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeNomeVazio_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().setNome(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals(MSG_UF_NAO_INFORMADO, r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeCpfVazio_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().setCpf(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("CPF do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeDataNascVazia_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().setDataNascimento(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Data nascimento do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeIdentidadeVazia_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().getIdentidade().setIdentidade(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Identidade do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeOrgaoInvalido_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().getIdentidade().getOrgaoExpedidor().setCodigo(-1);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Orgão expedidor da identidade do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeUfVazia_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().getIdentidade().getUf().setSigla(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("UF do Orgão expedidor da Identidade do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void salvar_quandoEstadoCivil2_eConjugeDataExpVazia_retornaMensagem() {
        Fiador f = buildFiadorComConjugeObrigatorio();
        f.getConjuge().getIdentidade().setDataExpedicaoIdentidade(EMPTY);
        Retorno r = bean.salvar(USUARIO, f);
        assertEquals(Long.valueOf(1), r.getCodigo());
        assertEquals("Data da expedição da identidade do conjuge não foi informado!", r.getMensagem());
    }

    @Test
    public void deverAlterarRendaDoFiador() throws  BusinessException {
        final Long CODIOG_RETORNO = 0L;
        final String MSG = "Renda do Fiador Alterada com sucesso.";

        FiadorRenda fiadorRenda = new FiadorRenda();
        fiadorRenda.setCpfFiador(CPF);
        fiadorRenda.setCodFies(CODIGO_FIES);
        fiadorRenda.setValorRendaBrutaMensal(BigDecimal.TEN);
        fiadorRenda.setEmailFiador(EMAIL);

        Retorno retorno = bean.alterarRendaFiador(fiadorRenda);

        assertNotNull(retorno);
        assertEquals(CODIOG_RETORNO, retorno.getCodigo());
        assertEquals(MSG, retorno.getMensagem());
    }

    @Test
    public void deverGerarErroNoValorDaRendaInvalida() throws  BusinessException {
        final Long CODIOG_RETORNO = -1L;
        final String MSG = "Valor informado é inválido.";

        FiadorRenda fiadorRenda = new FiadorRenda();
        fiadorRenda.setCpfFiador(CPF);
        fiadorRenda.setCodFies(CODIGO_FIES);
        fiadorRenda.setValorRendaBrutaMensal(BigDecimal.ZERO);
        fiadorRenda.setEmailFiador(EMAIL);

        Retorno retorno = bean.alterarRendaFiador(fiadorRenda);

        assertNotNull(retorno);
        assertEquals(CODIOG_RETORNO, retorno.getCodigo());
        assertEquals(MSG, retorno.getMensagem());
    }

    @Test
    public void deverGerarErroNaValidacaoDoFiadorExistente() throws BusinessException {
        final Long CODIOG_RETORNO = -1L;
        final String MSG = "O usuário logado não consta na base de Fiador do SIFES.";

        FiadorRenda fiadorRenda = new FiadorRenda();
        fiadorRenda.setCpfFiador(CPF);
        fiadorRenda.setCodFies(CODIGO_FIES);
        fiadorRenda.setValorRendaBrutaMensal(BigDecimal.TEN);
        fiadorRenda.setEmailFiador(EMAIL);


        doThrow(new BusinessException("Fiador não Localizado.")).when(bean).validaFiadorExistente(fiadorRenda.getCpfFiador(), fiadorRenda.getCodFies());

        Retorno retorno = bean.alterarRendaFiador(fiadorRenda);

        assertNotNull(retorno);
        assertEquals(CODIOG_RETORNO, retorno.getCodigo());
        assertEquals(MSG, retorno.getMensagem());
    }

    @Test(expected = Exception.class)
    public void deverGerarUmaException() throws Exception {
        FiadorRenda fiadorRenda = new FiadorRenda();
        fiadorRenda.setCpfFiador(CPF);
        fiadorRenda.setCodFies(CODIGO_FIES);
        fiadorRenda.setValorRendaBrutaMensal(BigDecimal.TEN);
        fiadorRenda.setEmailFiador(EMAIL);


        doThrow(new Exception("ERRO")).when(bean).validaFiadorExistente(fiadorRenda.getCpfFiador(), fiadorRenda.getCodFies());

        bean.alterarRendaFiador(fiadorRenda);
    }
    @Test
    public void isRegimeBensValido(){
        boolean b = bean.isRegimeBensValido(null);
        Assert.assertFalse(b);
    }

    @Test
    public void isRegimeBensValidofalse(){
        boolean b = bean.isRegimeBensValido(7);
        Assert.assertFalse(b);
    }

    @Test
    public void salvarFiadorComplementar() throws BusinessException {
        FiadorContratacaoComplementar fiador  = mockFiadorContratacaoComplementar();
        Mockito.when(em.createNativeQuery(Mockito.anyString())).thenReturn(query);
        Mockito.when(query.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(query);
        Mockito.when(query.setParameter(Mockito.anyInt(), Mockito.any())).thenReturn(query);

        bean.salvarFiadorComplementar(fiador, 2L);

        Mockito.verify(em).createNativeQuery(Mockito.anyString());


    }

    public FiadorContratacaoComplementar mockFiadorContratacaoComplementar(){
        FiadorContratacaoComplementar fiador = new FiadorContratacaoComplementar();


        fiador.setIdentidade(STRING_QUALQUER);
        fiador.setCodigoOrgaoExpedidor(INTEGER_QUALQUER);
        fiador.setUfExpedicao(STRING_QUALQUER);
        fiador.setDataExpedicaoIdentidade(STRING_QUALQUER);
        fiador.setCodigoEstadoCivil(INTEGER_QUALQUER);
        fiador.setEndereco(STRING_QUALQUER);
        fiador.setBairro(STRING_QUALQUER);
        fiador.setUf(STRING_QUALQUER);
        fiador.setCodigoCidade(INTEGER_QUALQUER);
        fiador.setCep(STRING_QUALQUER);
        fiador.setDddTelefone(STRING_QUALQUER);
        fiador.setNumeroTelefone(STRING_QUALQUER);
        fiador.setDddTelefoneCelular(STRING_QUALQUER);
        fiador.setNumeroTelefoneCelular(STRING_QUALQUER);
        fiador.setDddTelefoneComercio(STRING_QUALQUER);
        fiador.setNumeroTelefoneComercio(STRING_QUALQUER);
        fiador.setNacionalidade(STRING_QUALQUER);
        fiador.setEmailFiador(STRING_QUALQUER);
        fiador.setCpfConjuge(STRING_QUALQUER);
        fiador.setNumeroDependenteConjuge(INTEGER_QUALQUER);
        fiador.setDataNacimentoConjuge(STRING_QUALQUER);
        fiador.setNomeConjuge(STRING_QUALQUER);
        fiador.setEmailConjugeFiador(STRING_QUALQUER);
        fiador.setDddConjuge(STRING_QUALQUER);
        fiador.setCelularConjuge(STRING_QUALQUER);
        fiador.setRegimeBens(INTEGER_QUALQUER);

        return fiador;
    }
}

