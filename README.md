package br.gov.caixa.fes.rest;

import br.gov.caixa.arqrefcore.excecao.BusinessException;
import br.gov.caixa.est.util.FESException;
import br.gov.caixa.fes.dominio.*;
import br.gov.caixa.fes.dominio.builder.MovimentacaoTitulosBuilder;
import br.gov.caixa.fes.dominio.relatorio.repasse.ItemConsultaRepasse;
import br.gov.caixa.fes.dominio.renegociacaocontrato.vo.DadosCadastraisEstudanteAPI;
import br.gov.caixa.fes.dominio.transicao.ParametroProcessoTO;
import br.gov.caixa.fes.dominio.transicao.TermoCandidatoTO;
import br.gov.caixa.fes.dto.contrato.*;
import br.gov.caixa.fes.infraestrutura.rest.RetornoListaVO;
import br.gov.caixa.fes.infraestrutura.securitysso.SecurityKeycloakUtils;
import br.gov.caixa.fes.negocio.*;
import br.gov.caixa.fes.negocio.acompanharprocessoestudante.AcompanharProcessoEstudanteService;
import br.gov.caixa.fes.negocio.agencia.AgenciaService;
import br.gov.caixa.fes.negocio.dto.ResultadoApiDTO;
import br.gov.caixa.fes.negocio.enums.MensagemValidacaoDadosEstudante;
import br.gov.caixa.fes.rest.api.HttpResponseMessage;
import br.gov.caixa.fes.util.Constantes;
import br.gov.caixa.fes.util.DataUtil;
import br.gov.caixa.fes.util.FesUtil;
import br.gov.caixa.fes.util.Utilities;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.HttpResponseCodes;

import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequestScoped
@Path("/contrato")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class ContratoRest extends UserProfileRest {

    private static final String FES_MANUT = "FES_MANUT";
    private static final String FES_MANUTJUR = "FES_MANUTJUR";
    private static final String FES_GESTOR = "FES_GESTOR";
    //RTC_24491053
    private static final String FES_CECOQGO = "FES_CECOQGO";
    private static final String ERRO_DADOS_SEGURADORA = "Não existe Valor de Seguro cadastrado para o período da contratação. Regularize o cadastro para continuar com a contratação.";
    private static final String ERRO_TARIFA_CONTRATO = "Não existe Tarifa cadastrada para o período da contratação. Regularize o cadastro para continuar com a contratação.";
    private static final String ERRO_TAXA_JUROS = "Não existe Taxa de Juros cadastrada para o período da contratação. Regularize o cadastro para continuar com a contratação.";
    private static final String ERRO_403 = "error403";
    private static final String ERRO_API_CONJUGE = "######## ERRO NA EXECUÇÃO DO MÉTODO ==> listarDadosConjuge";
    private static final String INICIANDO = "INICIANDO";
    private static final String ANALISANDO_LOG_APP_INICIO = "######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: ";
    private static final String ANALISANDO_LOG_APP_SAIDA = "######## ANALISANDO_LOG_APP - SAÍDA DO MÉTODO: ";
    private static final String RETORNANDO_CODIGO_401 = "######## RETORNANDO CÓDIGO 401 ######  Método ==> ";
    private static final String CPF_LOG = " - CPF: ";
    private static final String DADOS_USUARIO_LOGADO = " ==> DADOS USUÁRIO LOGADO: ";

    private static final String ERRO_API = "######## ERRO NA EXECUÇÃO DO MÉTODO ==> ";
    private static final String INCLUIR_FIADOR_LOG = "incluir fiador";
    private static final String LISTAR_FIADOR_LOG = "listar fiador";
    private static final String ATUALIZAR_SEGURADORA_LOG = "atualizar seguradora";
    private static final String LISTAR_SEGURADORAS_LOG = "listar seguradoras";
    private static final String INCLUIR_DADOS_COMPLEMENTARES_FIADOR_LOG = "incluir dados complementares fiador";
    private static final String LISTAR_REPRESENTANTE_LEGAL_LOG = "listar representante legal";
    private static final String REJEITAR_PARTICIPACAO_CONTRATO_LOG = "rejeitar participacao contrato";
    private static final String ATUALIZA_DADOS_BASICOS_CONJUGE_LOG = "atualizaDadosBasicosConjugeApp";
    private static final String LISTAR_TIPOS_DOCUMENTOS_LOG = "listarTiposDocumentos";
    private static final String INCLUIR_DADOS_COMPLEMENTARES_CONJUGE_LOG = "incluirDadosComplementaresConjugeApp";
    private static final String INCLUIR_DADOS_BASICOS_REPRESENTANTE_LEGAL_LOG = "incluirDadosBasicosRepresentanteLegalApp";
    private static final String INCLUIR_DADOS_COMPLEMENTARES_REPRESENTANTE_LEGAL_LOG = "incluirDadosComplementaresRepresentanteLegalApp";
    private static final String DADOS_DO_SERVIDOR_LOG = "dadosDoServidor";
    private static final String VALIDAR_CRITERIOS_CONTRATACAO_LOG = "validarCriteriosContratacaoEstudante";
    private static final String ALTERAR_DADOS_CADASTRAIS_LOG = "alterarDadosCadastraisEstudante";
    private static final String DETALHAR_CURSO_ESTUDANTE_API_LOG = "detalharCursoEstudanteAPI";
    private static final String ATUALIZA_CONTA_CONTRATO_LOG = "atualizaContaContrato";
    private static final String ATUALIZAR_STATUS_CONTRATO_LOG = "atualizarStatusContrato";
    private static final String REJEITAR_CONTRATACAO_LOG = "rejeitarContratacao";
    private static final String LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG = "listarContasCaixaEstudante";
    private static final String CONFIRMAR_DADOS_CURSO_IES_LOG = "confirmarDadosCursoIes";
    private static final String DETALHAR_CONTRATO_LOG = "detalharContrato";
    private static final String ENVIAR_CONTRATO_SIAPI_LOG = "enviarContratoSIAPI";
    private static final String ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG = "alterarAgenciaVinculacaoContrato";
    private static final String DISPONIBILIZAR_TERMO_LOG = "disponibilizarTermo";
    private static final String UPLOAD_DOCUMENTO_LOG = "uploadDocumento";
    private static final String LISTA_TIPO_DOCUMENTO_PESSOA_LOG = "listaTipoDocumentoPessoa";
    private static final String OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG = "obterListaDocumentosEnviados";
    private static final String LISTAR_AGENCIAS_CAIXA_LOG = "listarAgenciasCaixa";
    private static final String ALTERAR_FIADOR_LOG = "alterar fiador";
    private static final String OBTER_RESUMO_CONTRATO_ESTUDANTE_LOG = "obterResumoContratoEstudante";
    private static final String DETALHAR_CONTRATO_EXTERNO_LOG = "detalharContratoExterno";

    public static final String CONSULTA_REALIZADA_COM_SUCESSO = "Consulta Realizada com Sucesso.";

    Retorno retorno400D = new Retorno(400L, "Não há informações de documentos", ERRO);
    Retorno retorno400E = new Retorno(400L, " Erro ao buscar os tipos de documentos", ERRO);

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILE_NAME = "filename=\"";
    private static final String FILE_XLS_EXT = ".xls\"";

    protected Logger logger = Logger.getLogger(ContratoRest.class.getSimpleName());

    @Inject protected ContratoService contratoServ;

    @Inject protected SeguradoraService seguradoraService;

    @Inject
    private AlteraPrazoService alteraPrazoService;

    @Inject
    private ConsultaEstudanteRecadastradoService consultaEstudanteRecadastradoService;

    @Inject
    private CadastroCREDUCService cadastroCREDUCService;

    @Inject
    private ReimpressaoDocumentoService reimpressaoDocumentoService;

    @Inject
    private BoletoService boletoService;

    @Inject
    private PlanilhaService planilhaService;

    @Inject
    private PeriodoAditamentoService periodoAditamentoService;

    @Inject
    private SipesService sipesService;

    @Inject
    private EstudanteService estudanteService;

    @Inject
    private CotacaoService cotacaoService;

    @Inject
    private AcompanharProcessoEstudanteService acompanharProcessoService;

    @Inject
    private TermoService termoService;

    @Inject
    private AgenciaService agenciaService;

    @Inject
    private SecurityKeycloakUtils securityKeycloakUtils;

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private InformacaoServidorService servidorService;

    private static final String USER_SICLI = "sifes.usuario.sicli";
    private static final String CODIGO = "######## Código: ";
    private static final String TIPO = " Tipo: ";
    private static final String MSG = "  Mensagem: ";

    private static final String ID_TERMO_CONTRATO_DIGITAL = System.getProperty("SIFES_ID_TERMO_CONTRATACAO_DIGITAL");
    private static final String ID_TERMO_CONTRATO_DIGITAL_PROUNI = System.getProperty("SIFES_ID_TERMO_CONTRATACAO_DIGITAL_PROUNI");

    private static final String SUCESSO = "SUCESSO";
    private static final String ERRO = "ERRO";


    @GET
    @Path("/consulta")
    public Contrato consulta(@QueryParam("cpf") String cpf, @QueryParam("codigoFies") Long codigoFies,
                             @QueryParam("agencia") int agencia) {
        logger.log(Level.INFO, "Chamada ao endpoint /consulta. CPF: {0}, CodigoFies: {1}, Agencia: {2}",
                new Object[]{cpf, codigoFies, agencia});
        try {
            return consultarContrato(cpf, codigoFies, agencia);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro crítico durante a consulta do contrato: ");
            return new Contrato(1L, "Ocorreu um erro inesperado ao processar sua solicitação.");
        }
    }

    @GET
    @Path("/consultaEstudante")
    public Contrato consultaEstudante(@QueryParam("cpf") String cpf, @QueryParam("codigoFies") Long codigoFies,
                                      @QueryParam("agencia") int agencia) {

        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
        } catch (Exception e) {
            return new Contrato(-1L, e.getMessage());
        }

        String usuario = getUsuarioLogado();
        try {
            return contratoServ.consulta(usuario, cpf, codigoFies, agencia);
        } catch (Exception e) {
            String mensagemErro = e.getMessage();

            if(mensagemErro.toLowerCase().contains("aluno n") && mensagemErro.toLowerCase().contains("encontrado")
                    && mensagemErro.toLowerCase().contains("contato com a cpsa")) {
                mensagemErro = "Aluno não encontrado, solicitamos que o mesmo entre em contato com a CPSA de sua Instituição de Ensino.";
            }

            return new Contrato(1L, mensagemErro);
        }
    }

    @GET
    @Path("/consultaResumo")
    public Contrato consultaResumo(@QueryParam("codigoFies") Long codigoFies) {
        String usuario = getUsuarioLogado();
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }

            return contratoServ.consultaResumo(usuario, codigoFies, "");
        } catch (Exception e) {
            return new Contrato(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/tipoGarantia")
    public List<TipoGarantia> tipoGarantia() {
        return contratoServ.tipoGarantia();
    }

    @GET
    @Path("/verificaEstornoContrato")
    public Retorno verificaEstornoContrato(@QueryParam("codigoFies") Long codigoFies) {

        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
        } catch (FESException e) {
            e.printStackTrace();
        }

        String usuario = "0";

        //RTC_24491053
        if ( this.possuiFuncao(FES_GESTOR) || this.possuiFuncao(FES_MANUT) || this.possuiFuncao(FES_MANUTJUR) || this.possuiFuncao(FES_CECOQGO)) {
            usuario = "1";
        }
        return contratoServ.verificaEstornoContrato(usuario, codigoFies);
    }

    @GET
    @Path("/alteraStatus")
    public Retorno alteraStatus(@QueryParam("codigoFies") Long codigoFies, @QueryParam("status") int status,
                                @QueryParam("numeroContrato") String numeroContrato, @QueryParam("codigoMotivo") int codigoMotivo,
                                @QueryParam("motivo") String motivo, @QueryParam("indNovoContrato") boolean indNovoContrato) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
        } catch (FESException e) {
            e.printStackTrace();
        }
        String usuario = getUsuarioLogado().toUpperCase();
        return contratoServ.alteraStatus(usuario, codigoFies, status, numeroContrato, codigoMotivo, motivo, indNovoContrato);
    }

    @GET
    @Path("/confirmaContratacao")
    public Retorno confirmaContratacao(@QueryParam("codigoFies") Long codigoFies, @QueryParam("status") int status,
                                       @QueryParam("diaVencimento") String diaVencimento, @QueryParam("tipoGarantia") int tipoGarantia) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
        } catch (FESException e) {
            e.printStackTrace();
        }

        String usuario = getUsuarioLogado();

        ContratacaoFase1 wC = new ContratacaoFase1();
        wC.setCodigoFies(codigoFies);
        wC.setDiaVencimento(diaVencimento);
        wC.setCodigoTipoGarantia(tipoGarantia);

        return contratoServ.confirmaContratacao(usuario, status, wC);
    }

    @GET
    @Path("/confirmaAditamento")
    public Retorno confirmaAditamento(@QueryParam("codigoFies") Long codigoFies,
                                      @QueryParam("codigoAditamento") int codigoAditamento, @QueryParam("dataAssinatura") String dataAssinatura) {
        Usuario usuario = new Usuario();
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
            usuario = usuarioService.consultarUsuario(getUsuarioLogado());
            if(usuario.getCodigoUsuario() == null) {
                usuario.setCodigoUsuario(getUsuarioLogado());
            }
            usuario.setCodigoTerminal(getIpAddress());
        } catch (FESException | BusinessException e) {
            e.printStackTrace();
        }

        ContratacaoFase1 wC = new ContratacaoFase1();
        wC.setCodigoFies(codigoFies);
        wC.setCodigoTipoGarantia(codigoAditamento);
        return contratoServ.confirmaAditamento(usuario, 1, wC);
    }

    @GET
    @Path("/confirmaAditamentoReenvio")
    public Retorno confirmaAditamentoReenvio(
            @QueryParam("codigoFies") Long codigoFies,
            @QueryParam("codigoAditamento") int codigoAditamento,
            @QueryParam("dataAssinatura") String dataAssinatura ,
            @QueryParam("numeroContrato") String numeroContrato	) {

        Usuario usuario = new Usuario();

        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
            usuario = usuarioService.consultarUsuario(getUsuarioLogado());
            if(usuario.getCodigoUsuario() == null) {
                usuario.setCodigoUsuario(getUsuarioLogado());
            }
            usuario.setCodigoTerminal(getIpAddress());
        } catch (FESException | BusinessException e) {
            e.printStackTrace();
        }

        return contratoServ.confirmaAditamentoSiapi(usuario,
                new ContratacaoFase1(codigoFies, codigoAditamento, dataAssinatura, numeroContrato));
    }

    @GET
    @Path("/confirmaAditamentoEstorno")
    public Retorno confirmaAditamentoEstorno(@QueryParam("codigoFies") Long codigoFies,
                                             @QueryParam("codigoAditamento") int codigoAditamento,
                                             @QueryParam("status") int status,
                                             @QueryParam("codigoMotivo") String codigoMotivo,
                                             @QueryParam("motivo") String motivo
    ) {
        Usuario usuario = new Usuario();
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new Contrato(403L, ERRO_403);
            }
            usuario = usuarioService.consultarUsuario(getUsuarioLogado());
            if(usuario.getCodigoUsuario() == null) {
                usuario.setCodigoUsuario(getUsuarioLogado());
            }
            usuario.setCodigoTerminal(getIpAddress());
        } catch (FESException | BusinessException e) {
            e.printStackTrace();
        }

        ContratacaoFase1 wC = new ContratacaoFase1();
        wC.setCodigoFies(codigoFies);
        wC.setCodigoTipoGarantia(codigoAditamento);
        wC.setOperacao(String.valueOf(status));
        wC.setCodigoMotivo(codigoMotivo);
        wC.setMotivo(motivo);

        return contratoServ.confirmaAditamentoEstorno(usuario, 1, wC);
    }

    @POST
    @Path("/cadastroMovimentacao")
    public RetornoListaVO<Movimentacao> cadastroMovimentacao(Movimentacao movimentacao) {
        return new RetornoListaVO<>(MovimentacaoTitulosBuilder.criarListaMovimentacao());
    }

    @POST
    @Path("/excluirMovimentacao")
    public RetornoListaVO<Movimentacao> excluirMovimentacao(Movimentacao movimentacao) {
        return new RetornoListaVO<>(MovimentacaoTitulosBuilder.criarListaMovimentacao());
    }

    @POST
    @Path("/alteraMovimentacao")
    public RetornoListaVO<Movimentacao> alteraMovimentacao(Movimentacao movimentacao) {
        return new RetornoListaVO<>(MovimentacaoTitulosBuilder.criarListaMovimentacao());
    }

    @POST
    @Path("/consultaUsuario")
    public Retorno consultaUsuario(Prazo prazo) {
        try {
            return alteraPrazoService.consultaUsuario(getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaContrato")
    public Retorno consultaContrato(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf) {
        try {
            this.consultaEstudanteRecadastradoService.consultaCodigoFies(codigoFies);
            return alteraPrazoService.consultaContrato(codigoFies, cpf);
        } catch (Exception e) {
            return new Retorno(1L, e.getMessage());
        }
    }

    @GET
    @Path("/alteraPrazo")
    public Retorno alteraPrazo(@QueryParam("codigoFies") String codigoFies) {
        try {
            return alteraPrazoService.alteraPrazo(codigoFies, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaDadosEstudanteAlteraSemestre")
    public Retorno consultaDadosEstudanteAlteraSemestre(@QueryParam("codigoFiesConsulta") Long codigoFiesConsulta,
                                                        @QueryParam("cpf") String cpf, @QueryParam("ano") String ano, @QueryParam("semestre") String semestre) {

        try {
            return contratoServ.consultarDadosEstudante(cpf, codigoFiesConsulta, ano, semestre);
        } catch (Exception e) {
            return new Retorno(1L, e.getMessage());
        }
    }

    @POST
    @Path("/confirmaAlteracaoSemestre")
    public Retorno confirmaAlteracaoSemestre(Semestre semestre) {
        String usuario = getUsuarioLogado();

        try {
            return contratoServ.efetivarAlteracaoSemestre(semestre, usuario);
        } catch (Exception e) {
            return new Retorno(0L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaCREDUC")
    public ConsultaCREDUC consultaCREDUC(@QueryParam("cpf") String cpf) {
        ConsultaCREDUC wObj = new ConsultaCREDUC();
        try {

            String usuarioLogado = getUsuarioLogado();

            wObj = cadastroCREDUCService.consulta(cpf, usuarioLogado);

        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/reimpressaoDocumentoAditamento")
    public ReimpressaoDocumentoAditamento reimpressaoDocumentoAditamento(@QueryParam("cpf") String cpf,
                                                                         @QueryParam("fies") long fies, @QueryParam("mes") int mes, @QueryParam("ano") int ano) {

        ReimpressaoDocumentoAditamento wObj = new ReimpressaoDocumentoAditamento();
        try {
            wObj = reimpressaoDocumentoService.consultarAditamento(cpf, fies, mes, ano);
        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/reimpressaoDocumentoContrato")
    public ReimpressaoDocumentoContrato reimpressaoDocumentoContrato(@QueryParam("cpf") String cpf,
                                                                     @QueryParam("codigoFiesConsulta") long fies) {

        ReimpressaoDocumentoContrato wObj = new ReimpressaoDocumentoContrato();
        try {
            wObj = reimpressaoDocumentoService.consultarContrato(cpf, fies);
        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/reimpressaoDocumentoEncerramento")
    public ReimpressaoDocumentoEncerramento reimpressaoDocumentoEncerramento(@QueryParam("cpf") String cpf,
                                                                             @QueryParam("fies") long fies, @QueryParam("mes") int mes, @QueryParam("ano") int ano) {

        ReimpressaoDocumentoEncerramento wObj = new ReimpressaoDocumentoEncerramento();
        try {
            wObj = reimpressaoDocumentoService.consultarEncerramento(cpf, fies, mes, ano);
        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/reimpressaoDocumentoSuspensao")
    public ReimpressaoDocumentoSuspensao reimpressaoDocumentoSuspensao(@QueryParam("cpf") String cpf,
                                                                       @QueryParam("fies") long fies, @QueryParam("mes") int mes, @QueryParam("ano") int ano) {

        ReimpressaoDocumentoSuspensao wObj = new ReimpressaoDocumentoSuspensao();
        try {
            wObj = reimpressaoDocumentoService.consultarSuspensao(cpf, fies, mes, ano);
        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/reimpressaoDocumentoTransferencia")
    public ReimpressaoDocumentoTransferencia reimpressaoDocumentoTransferencia(@QueryParam("cpf") String cpf,
                                                                               @QueryParam("fies") long fies, @QueryParam("mes") int mes, @QueryParam("ano") int ano) {

        ReimpressaoDocumentoTransferencia wObj = new ReimpressaoDocumentoTransferencia();
        try {
            wObj = reimpressaoDocumentoService.consultarTransferencia(cpf, fies, mes, ano);
        } catch (Exception e) {
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
        }

        return wObj;
    }

    @GET
    @Path("/consultaBoleto")
    public RetornoListaVO<BoletoLista> consultaBoleto(@QueryParam("codigoFies") String codigoFies,
                                                      @QueryParam("contrato") String contrato) {
        try {
            if(!validaDadosPertenceUsuarioLogado(Long.parseLong(codigoFies), null)){
                return new RetornoListaVO<>(403, ERRO_403);
            }
        } catch (FESException e) {
            e.printStackTrace();
        }

        try {
            //return new RetornoListaVO<>(boletoService.consulta(codigoFies, contrato, this.getUsuarioLogado() ));
            return new RetornoListaVO<>(boletoService.consultaBoleto(Long.valueOf(codigoFies)));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultaBoleto2")
    public BoletoLista consultaBoleto2(@QueryParam("codigoFies") String codigoFies,
                                       @QueryParam("contrato") String contrato, @QueryParam("prestacao") String prestacao,
                                       @QueryParam("dataPagamento") String dataPagamento) {
        try {
            //return boletoService.consulta(codigoFies, contrato, prestacao, dataPagamento, this.getUsuarioLogado() );
            return boletoService.consultaBoletoEspecifico(contrato, prestacao, dataPagamento);
        } catch (Exception e) {
            BoletoLista wObj = new BoletoLista();
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
            return wObj;
        }
    }

    @GET
    @Path("/consultaBoletosAbertos")
    public RetornoListaVO<BoletoLista> consultaBoletosAbertos(@QueryParam("codigoFies") String codigoFies,
                                                              @QueryParam("contrato") String contrato) {
/*
        try {
            if(!validaDadosPertenceUsuarioLogado(Long.parseLong(codigoFies), null)){
                return new RetornoListaVO<>(403, ERRO_403);
            }
        } catch (FESException e) {
            e.printStackTrace();
        }
*/
        try {
            return new RetornoListaVO<>(boletoService.consultaBoletosAbertos(Long.parseLong(codigoFies), contrato ));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultaBoletoImpressao")
    public Boleto consultaBoletoImpressao(@QueryParam("codigoFies") String codigoFies,
                                          @QueryParam("contrato") String contrato, @QueryParam("prestacao") String prestacao,
                                          @QueryParam("dataPagamento") String dataPagamento) {
        try {
            //return boletoService.consultaBoletoImpressao(codigoFies, contrato, prestacao, dataPagamento, getUsuarioLogado());
            return boletoService.consultaBoletoEspecificoImpressao(contrato, prestacao, dataPagamento);
        } catch (Exception e) {
            Boleto wObj = new Boleto();
            wObj.setCodigo(1L);
            wObj.setMensagem(e.getMessage());
            return wObj;
        }
    }

    @GET
    @Path("/imprimirBoletoRegistrado")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public BoletoRegistradoSaida consultaBoletoImpressao(@QueryParam("contrato") String contrato,
                                                         @QueryParam("prestacao") String prestacao, @QueryParam("dataPagamento") String dataPagamento) {
        try {
            log.log(Level.SEVERE, "######### INICIO DO MÉTODO CONSULTA BOLETO IMPRESSÃO EM ContratoRest - endpoint ==> /imprimirBoletoRegistrado");
            //BoletoRegistradoEntrada entrada = new BoletoRegistradoEntrada(prestacao, contrato, dataPagamento, getUsuarioLogado(), getIpAddress());
            //return boletoService.consultaBoletoRegistradoUrlSIFEC(entrada, getToken());
            return boletoService.consultaBoletoRegistrado(contrato, prestacao, dataPagamento);
        } catch (Exception e) {
            BoletoRegistradoSaida wObj = new BoletoRegistradoSaida();
            wObj.setCodigo(-1L);
            wObj.setMensagem(e.getMessage());
            return wObj;
        }
    }

    @GET
    @Path("/consultaBoletoImpressaoBarra")
    @Produces("image/*")
    public Response consultaBoletoImpressao(@QueryParam("codigoBarras") String codigoBarras,
                                            @QueryParam("width") int width, @QueryParam("height") int height) {

        try {
            BufferedImage wImg = Utilities.codigoBarras25I(codigoBarras, width, height);
            ByteArrayOutputStream wOut = new ByteArrayOutputStream();
            ImageIO.write(wImg, "png", wOut);

            final byte[] imgData = wOut.toByteArray();
            final InputStream wI = new ByteArrayInputStream(imgData);
            return Response.ok(wI).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Ocorreu um erro na consulta boleto.", e);
        }

        return Response.noContent().build();
    }

    @GET
    @Path("/consultaPlanilha")
    public Retorno consultaPlanilha(@QueryParam("codigoFies") String codigoFies,
                                    @QueryParam("contrato") String contrato) {
        try {
            Retorno retorno = new Retorno();
            retorno.setCodigo(0l);
            retorno.setMensagem(planilhaService.gerarPlanilhaHTML(contrato));
            return retorno;
        }catch(BusinessException e) {
            return new Retorno(1L, e.getMessage());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/consultarPeriodoAditamento")
    public RetornoListaVO<PeriodoAditamento> consultarPeriodoAditamento(PeriodoAditamento objeto) {
        try {
            return new RetornoListaVO<>(periodoAditamentoService.consultar(String.valueOf(objeto.getCampus())));
        } catch (Exception e) {
            return new RetornoListaVO<>(0, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarPeriodoAditamento")
    public Retorno cadastrarPeriodoAditamento(PeriodoAditamento objeto) {
        try {
            return periodoAditamentoService.salvar(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/excluirPeriodoAditamento")
    public Retorno excluirPeriodoAditamento(PeriodoAditamento objeto) {
        try {
            return periodoAditamentoService.excluir(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarNumeroContrato")
    public Retorno cadastrarNumeroContrato(NumeroContrato objeto) {
        try {
            return contratoServ.cadastrarNumeroContrato(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/excluirNumeroContrato")
    public Retorno excluirNumeroContrato(NumeroContrato objeto) {
        try {
            return contratoServ.excluirNumeroContrato(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/reenvioEncerramentoIncondicional/consulta")
    public EncerramentoIncondicional consultaEncerramentoIncondicional(@QueryParam("codigoFies") Long codigoFies,
                                                                       @QueryParam("contrato") String contrato, @QueryParam("cpf") String cpf) {
        try {
            return contratoServ.consultaEncerramentoIncondicional(codigoFies, cpf, contrato);
        } catch (Exception e) {
            return new EncerramentoIncondicional();
        }
    }

    @POST
    @Path("/reenvioEncerramentoIncondicional/reenviar")
    public Retorno enviaEncerramentoIncondicional(@QueryParam("cpf") String cpf,
                                                  @QueryParam("observacao") String observacao) {
        try {
            return contratoServ.envioEncerramentoIncondicional(cpf, observacao, this.getUsuarioLogado().toUpperCase());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/manutencao/consultaSituacao")
    public ConsultaSituacaoContrato consultaSituacao(@QueryParam("codigoFies") Long codigoFies,
                                                     @QueryParam("cpf") String cpf) {
        try {
            return contratoServ.consultaSituacaoContrato(codigoFies, cpf, getUsuarioLogado().toUpperCase());
        } catch (Exception e) {
            return new ConsultaSituacaoContrato(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaSimulador")
    public SimulaFinanciamento consultaSimulador(@QueryParam("codigoFies") Long codigoFies,
                                                 @QueryParam("indContratoNovoFies") Boolean indContratoNovoFies) {
        try {
            SimulaFinanciamento simulador = contratoServ.consultaSimulador(codigoFies);
            if (Boolean.TRUE.equals(indContratoNovoFies)) {
                Contrato contrato = consultaResumo(codigoFies);
                simulador.setVrCoparticipacao(contrato.getVrCoParticipacao());
                simulador.setVrSeguro(contrato.getValorSeguro());
                simulador.setVrTarifa(contrato.getValorTarifa());
                Estudante estudante = estudanteService.consultar(getUsuarioLogado().toUpperCase(), codigoFies);
                simulador.setVrRendaMensalPrevista(BigDecimal.valueOf(estudante.getRendaFamiliar()));
                simulador.setValorIPCA(cotacaoService.calcularValorIpca());
                simulador.setVrAmortizacao(BigDecimal.ZERO);
            }
            return simulador;
        } catch (Exception e) {
            return new SimulaFinanciamento(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaSipes")
    public ResumoPesquisaCadastral consultaSipes(@QueryParam("codigoFies") long codigoFies,
                                                 @QueryParam("cpf") String cpf) {
        ResumoPesquisaCadastral wR1;
        try {
            wR1 = sipesService.consultar(codigoFies, cpf, getUsuarioLogado());
        } catch (Exception e) {
            wR1 = new ResumoPesquisaCadastral();
            wR1.setCodigo(1L);
            wR1.setMensagem(e.getMessage());
        }
        return wR1;
    }

    @GET
    @Path("/ContratoReeimpressao")
    public Testemunha contratoReeimpressao(@QueryParam("codigoFies") Long codigoFies) {
        try {
            return contratoServ.consultaTestemunha(codigoFies, getUsuarioLogado());
        } catch (Exception e) {
            return null;
        }
    }

    @POST
    @Path("/cadastrarTestemunha")
    public Retorno cadastrarTestemunha(Testemunha objeto) {
        try {
            return contratoServ.cadastrarTestemunha(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaInicioContratacao")
    public ContratoInicio consultaInicioContratacao(@QueryParam("codigoFies") Long codigoFies) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new ContratoInicio(403L, ERRO_403);
            }
            String usuario = isUsuarioInterno() ? getUsuarioLogado() : System.getProperty(USER_SICLI);
            ContratoInicio contratoInicio = contratoServ.consultaInicioContratacao(codigoFies,
                    usuario.toUpperCase(), grupoUsuario());

            //seguradora somente para novos contratos
            if (contratoInicio.getContrato().getIndContratoNovoFies()
                    && contratoInicio.getContrato().getIdSeguradora() != null ) {
                Seguradora seguradora = seguradoraService.getSeguradora(contratoInicio.getContrato().getIdSeguradora(),
                        contratoInicio.getContrato().getEstudanteCurso().getCurso().getNome());
                if (seguradora != null && seguradora.getIdSeguradora() != null) {
                    contratoInicio.getContrato().setValorSeguro(setValorSeguro(seguradora));
                }
            }
            return contratoInicio;
        } catch (Exception e) {
            return new ContratoInicio(-1L, e.getMessage());
        }
    }

    private BigDecimal setValorSeguro(Seguradora seguradora) {
        BigDecimal valorSeguro = BigDecimal.ZERO;
        if (seguradora.getValorSeguroVigente() != null) {
            valorSeguro = BigDecimal.valueOf(seguradora.getValorSeguroVigente());
        } else if (seguradora.getValorSeguro() != null) {
            valorSeguro = BigDecimal.valueOf(seguradora.getValorSeguro());
        }
        return valorSeguro;
    }

    @GET
    @Path("/consultaInicioAditamento")
    public ContratoInicio consultaInicioAditamento(@QueryParam("codigoFies") Long codigoFies,
                                                   @QueryParam("codigoAditamento") String codigoAditamento) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new ContratoInicio(403L, ERRO_403);
            }
            String usuario = isUsuarioInterno() ? getUsuarioLogado() : System.getProperty(USER_SICLI);
            return contratoServ.consultaInicioAditamento(codigoFies, codigoAditamento, usuario.toUpperCase(),
                    super.grupoUsuario());
        } catch (Exception e) {
            return new ContratoInicio(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultaUnidadeAditamento")
    public Retorno consultaUnidadeAditamento(@QueryParam("codigoFies") Long codigoFies,
                                             @QueryParam("codigoAditamento") String codigoAditamento) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return new ContratoInicio(403L, ERRO_403);
            }
            return contratoServ.consultaUnidadeAditamento(codigoFies, codigoAditamento);
        } catch (Exception e) {
            return new ContratoInicio(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarContratacaoFase1")
    public Retorno cadastrarContratacaoFase1(ContratacaoFase1 objeto) {
        try {
            //RTC_24491053
            return contratoServ.cadastrarContratacaoFase1(objeto, getUsuarioLogado(), getEmpregado(getUsuarioLogado()).getNumeroUnidade(),
                    (this.possuiFuncao(FES_GESTOR) || this.possuiFuncao(FES_MANUTJUR) || this.possuiFuncao(FES_MANUT) || this.possuiFuncao(FES_CECOQGO)));
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarAditamentoFase1")
    public Retorno cadastrarAditamentoFase1(ContratacaoFase1 objeto) {
        try {
            return contratoServ.cadastrarAditamentoFase1(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarContratacaoFase2")
    public Retorno cadastrarContratacaoFase2(ContratacaoFase2 objeto) {
        try {
            return contratoServ.cadastrarContratacaoFase2(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarContratacaoFase3")
    public Retorno cadastrarContratacaoFase3(ContratacaoFase3 objeto) {
        try {
            return contratoServ.cadastrarContratacaoFase3(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @POST
    @Path("/cadastrarContratacaoFase4")
    public Retorno cadastrarContratacaoFase4(ContratacaoFase4 objeto) {
        try {
            return contratoServ.cadastrarContratacaoFase4(objeto, getUsuarioLogado());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/seguradorasVigentes/{nomeCurso}")
    public RetornoListaVO<Seguradora> getSeguradoras(@PathParam(value = "nomeCurso") String nomeCurso) {
        try {
            return new RetornoListaVO<>(seguradoraService.getSeguradorasVigentes(nomeCurso));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/diasVencimento")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Retorno buscarTarifa(@QueryParam("nuProcesso") Long nuProcesso) {
        try {
            ParametroProcessoTO to = contratoServ.buscarDiasVencimento(nuProcesso);
            return new TarifaVO(to.getDiasVencimento());
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/verificarRenegociacao")
    public Retorno verificarRenegociacao() {
        try {
            return contratoServ.verificarRenegociacao(grupoUsuario());
        } catch (Exception e) {
            return new Retorno(BigDecimal.ZERO.longValue(), e.getMessage());
        }
    }

    @GET
    @Path("/consultarContratosAcompanharProcesso")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RetornoListaVO<ContratoAcompanhamento> consultarContratosAcompanharProcesso(@QueryParam("cpf") String cpf) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(StringUtils.EMPTY, cpf, StringUtils.EMPTY);
            String usuarioLogado = getUsuarioLogado();
            String codMantenedora = "";
            String codIes = "";
            String codCampus = "";
            if(!isUsuarioInterno()) {
                UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());

                if(usuarioExterno.getRoles().contains("FES_MANTENEDORA")) {
                    codMantenedora = usuarioExterno.getIdMantenedora().toString();
                }else if(usuarioExterno.getRoles().contains("FES_IES")) {
                    codMantenedora = usuarioExterno.getIdMantenedora().toString();
                    codIes = usuarioExterno.getIdIes().toString();
                }else if(usuarioExterno.getRoles().contains("FES_CAMPUS")) {
                    codMantenedora = usuarioExterno.getIdMantenedora().toString();
                    codIes = usuarioExterno.getIdIes().toString();
                    codCampus = usuarioExterno.getIdCampus().toString();
                }
            }
            return new RetornoListaVO<>(acompanharProcessoService.consultarContratosAcompanharProcesso(consultaAcompanharProcesso, usuarioLogado, codMantenedora, codIes, codCampus));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheTipoFinanciamentoLegado")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarDetalheTipoFinanciamentoLegado(@QueryParam("codigoFies") String codigoFies,
                                                           @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato) {
        try {
            if(StringUtils.isNotBlank(codigoFies) &&
                    !validaDadosPertenceUsuarioLogado(Long.parseLong(codigoFies), null)){
                return new Retorno(403L, ERRO_403);
            }
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato);
            String usuarioLogado = getUsuarioLogado();
            return acompanharProcessoService.consultarDetalheTipoFinanciamentoLegado(consultaAcompanharProcesso, usuarioLogado);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheTipoFinanciamentoNovoFIES")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarDetalheTipoFinanciamentoNovoFIES(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato);
            String usuarioLogado = getUsuarioLogado();
            return acompanharProcessoService.consultarDetalheTipoFinanciamentoNovoFIES(consultaAcompanharProcesso, usuarioLogado);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultarContratosEstudanteNovoFIES")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RetornoListaVO<ContratoAcompanhamento> consultarContratosEstudanteNovoFIES(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato);
            return new RetornoListaVO<>(acompanharProcessoService.consultarContratosEstudanteNovoFIES(consultaAcompanharProcesso));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheInscricao")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarDetalheInscricao(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato,
                                             @QueryParam("nuOperacao") String nuOperacao) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato, nuOperacao);
            return acompanharProcessoService.consultarDetalheInscricao(consultaAcompanharProcesso);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheAditamento")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RetornoListaVO<DetalheAditamento> consultarDetalheAditamento(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato,
                                                                        @QueryParam("nuOperacao") String nuOperacao) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato, nuOperacao);
            return new RetornoListaVO<>(acompanharProcessoService.consultarDetalheAditamento(consultaAcompanharProcesso));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheFinanciamento")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarDetalheFinanciamento(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato,
                                                 @QueryParam("nuOperacao") String nuOperacao) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato, nuOperacao);
            return acompanharProcessoService.consultarDetalheFinanciamento(consultaAcompanharProcesso);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheMantenedora")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RetornoListaVO<DetalheMantenedora> consultarDetalheMantenedora(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato,
                                                                          @QueryParam("nuOperacao") String nuOperacao) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato, nuOperacao);
            return new RetornoListaVO<>(acompanharProcessoService.consultarDetalheMantenedora(consultaAcompanharProcesso));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultarMantenedoraExtratoEstudante")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RetornoListaVO<ExtratoEstudanteMantenedora> consultarMantenedoraExtratoEstudante(@QueryParam("codigoFies") String codigoFies, @QueryParam("cpf") String cpf, @QueryParam("numeroContrato") String numeroContrato) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codigoFies, cpf, numeroContrato);
            return new RetornoListaVO<>(acompanharProcessoService.consultarMantenedoraExtratoEstudante(consultaAcompanharProcesso));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultarCursosIES")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarCursosIES(@QueryParam("nuIES") String nuIES, @QueryParam("numeroContrato") String numeroContrato) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(nuIES, numeroContrato);
            return acompanharProcessoService.consultarCursosIES(consultaAcompanharProcesso);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/consultarDetalheExtratoEstudante")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Retorno consultarDetalheExtratoEstudante(@QueryParam("numeroContrato") String numeroContrato, @QueryParam("numeroMantenedora") String numeroMantenedora) {
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso();
            consultaAcompanharProcesso.setContrato(numeroContrato);
            consultaAcompanharProcesso.setNuMantenedora(numeroMantenedora);
            String usuarioLogado = getUsuarioLogado();
            return acompanharProcessoService.consultarDetalheExtratoEstudante(consultaAcompanharProcesso, usuarioLogado);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @GET
    @Path("/exportar")
    @Produces({"binary/octet-stream"})
    public Response exportarDetalheExtratoEstudante(@QueryParam("codigoFies") String codigoFies, @QueryParam("numeroMantenedora") String numeroMantenedora) {

        Response.ResponseBuilder response;
        try {
            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso();
            consultaAcompanharProcesso.setCodigoFies(codigoFies);
            consultaAcompanharProcesso.setNuMantenedora(numeroMantenedora);
            String usuarioLogado = getUsuarioLogado();
            Arquivo arquivo = acompanharProcessoService.exportarDetalheExtratoEstudante(consultaAcompanharProcesso, usuarioLogado);
            InputStream is = new ByteArrayInputStream(arquivo.getDados());
            response = Response.ok(is);
            response.header(CONTENT_DISPOSITION, FILE_NAME + arquivo.getNome() + FILE_XLS_EXT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "ordemBancariaService.exportarOrdemBancaria", e);
            response = Response.ok(e.getMessage());
        }

        return response.build();
    }

    @GET
    @Path("/gerarDetalheBoleto")
    @Produces({"binary/octet-stream"})
    public Response gerarDetalheBoleto(@QueryParam("codigoFies") String codigoFies, @QueryParam("contrato") String contrato, @QueryParam("prestacao") String prestacao) throws IOException {
        ResponseBuilder response;
        try {

            log.log(Level.SEVERE, "######## INÍCIO DO MÉTODO: gerarDetalheBoleto - Parâmetros Recebidos ==> codigoFies = {0} contrato = {1} prestacao = {2}",
                    new Object[]{codigoFies, contrato, prestacao});

            UsuarioExterno usuarioExterno = null;

            if (!isUsuarioInterno()) {
                usuarioExterno = getUsuarioExterno(getUsuarioLogado());
                Set<String> perfil = usuarioExterno.getRoles();
                for(String set : perfil) {
                    if(Boolean.TRUE.equals(set.equalsIgnoreCase(Constantes.FES_ESTUDANTE))) {

                        //Valida se o código FIES do usuário logado corresponde ao mesmo passado no serviço
                        if(!usuarioExterno.getCodFies().toString().equals(codigoFies)) {
                            logger.log(Level.INFO, "Usuario nao possui permissao acessar detalhe boleto outro usuario. Usuario: {0}", codigoFies);
                            return Response.status(Response.Status.UNAUTHORIZED).build();
                        }

                        //Valida se a prestação informada está em aberto
                        Boolean possuiPrestacao = validarNumeroPrestacao(codigoFies, contrato, prestacao);

                        if(Boolean.FALSE.equals(possuiPrestacao)) {
                            logger.log(Level.SEVERE, "O usuario ({0}) passou uma prestacao inexistente ({1}) para gerar detalhe do boleto.", new Object[]{codigoFies, prestacao});
                            return Response.status(Response.Status.NOT_FOUND).build();
                        }

                    }
                }
            }

            //Gera o html do extrato boleto
            String htmlExtratoBoleto = boletoService.gerarExtratoBoleto(getUsuarioLogado(), Long.valueOf(codigoFies), prestacao);

            response = FesUtil.exportarPDF("Detalhe_Boleto", htmlExtratoBoleto);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ocorreu um erro na geracao do detalhe do boleto.", e);
            return Response.ok(e.getMessage()).build();
        }
        return response.build();
    }

    @GET
    @Path("/consultaRepasse")
    public RetornoListaVO<ItemConsultaRepasse> consultaRepasse(@QueryParam("codigoFies") String codigoFies,
                                                               @QueryParam("contrato") String contrato) {
        try {
            return new RetornoListaVO<>(boletoService.detalheRepasse(this.getUsuarioLogado(), contrato));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/gerarRelatorioRepasse")
    @Produces({"binary/octet-stream"})
    public Response gerarRelatorioRepasse(@QueryParam("codigoFies") Long codigoFies, @QueryParam("contrato") String contrato,
                                          @QueryParam("ano") String ano) {
        ResponseBuilder response;
        try {
            //Gera o html do relatório de repasse
            String htmlRelatorioRepasse = boletoService.gerarRelatorioRepasse(getUsuarioLogado(), contrato, codigoFies, ano);

            //Exportando o html para PDF no tamanho A4 paisagem
            Rectangle a4Paisagem = new Rectangle(842,595);
            response = FesUtil.exportarPDFPageSize("Relatorio_Repasse_IES", htmlRelatorioRepasse, a4Paisagem);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ocorreu um erro na geração do Relatório de Repasse da IES.", e);
            return Response.ok(e.getMessage()).build();
        }
        return response.build();
    }

    private Boolean validarNumeroPrestacao(String codigoFies, String contrato, String prestacao) throws Exception {

        // Faz uma validação defensiva dos parâmetros de entrada

        if (codigoFies == null || codigoFies.trim().isEmpty()) {
            return false;
        }
        if (prestacao == null || prestacao.trim().isEmpty()) {
            return false;
        }

        try {

            final int numPrestacaoAlvo = Integer.parseInt(prestacao);
            final long idFies = Long.parseLong(codigoFies);

            List<BoletoLista> listaBoleto = boletoService.consultaBoleto(idFies);

            if (listaBoleto == null || listaBoleto.isEmpty()) {
                return false;
            }

            for (BoletoLista boleto : listaBoleto) {
                if (boleto != null && boleto.getPrestacao() == numPrestacaoAlvo) {
                    return true;
                }
            }

        } catch (NumberFormatException e) {
            throw new BusinessException("Erro de formato nos parâmetros recebidos ==> codigoFies = " +
                    codigoFies + " contrato = " + contrato + " prestacao =  " + prestacao + " Erro ==> " +  e.getMessage(), e);
        }

        return false;
    }

    @GET
    @Path("/gerarBoletoPersonalizado")
    @Produces({"binary/octet-stream"})
    public Response gerarBoletoPersonalizado(@QueryParam("codigoFies") String codigoFies,
                                             @QueryParam("contrato") String contrato, @QueryParam("prestacao") String prestacao,
                                             @QueryParam("dataPagamento") String dataPagamento) throws IOException {
        ResponseBuilder response;
        File downloadfile = null;
        FileWriter fr = null;
        try {
            String prefixoArquivos = contrato + "_" + prestacao + "_" + DataUtil.formatar(new Date(), DataUtil.PADRAO_DATA_YYYY_MM_DD);

            //Gerar boleto registrado
            logger.info("Gera o boleto registrado.");

            //BoletoRegistradoEntrada entrada = new BoletoRegistradoEntrada(prestacao, contrato, dataPagamento, getUsuarioLogado(), getIpAddress());
            //BoletoRegistradoSaida boletoRegistrado = boletoService.consultaBoletoRegistradoUrlSIFEC(entrada, getToken());
            BoletoRegistradoSaida boletoRegistrado =  boletoService.consultaBoletoRegistrado(contrato, prestacao, dataPagamento);

            logger.info("Boleto registrado gerado.");

            //Faz o download do boleto
            logger.log(Level.INFO, "Inicia o download do boleto na URL {0}.", boletoRegistrado.getUrlBoleto());
            ClientRequest clientRequest = new ClientRequest(boletoRegistrado.getUrlBoleto()).accept("*/*");
            clientRequest.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
            ClientResponse<String> clientResponse = clientRequest.get(String.class);

            if (clientResponse.getStatus() != HttpResponseCodes.SC_OK) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Erro ao gerar o boleto registrado! (HTTP " + clientResponse.getStatus() + ")").build();
            }

            File res = clientResponse.getEntity(File.class);
            String nomeArquivoDownload = prefixoArquivos + "_boletodownload.pdf";
            downloadfile = new File(nomeArquivoDownload);

            if(!res.renameTo(downloadfile)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Erro ao renomear o nmoe do arquivo no boleto.").build();
            }

            fr = new FileWriter(res);
            fr.flush();
            logger.info("Download do boleto concluido.");

            //Gera o html do extrato boleto
            String htmlExtratoBoleto = boletoService.gerarExtratoBoleto(getUsuarioLogado(), Long.valueOf(codigoFies), prestacao);

            //Concatenar os dois PDFs
            List<InputStream> pdfs = new ArrayList<>();
            pdfs.add(FesUtil.gerarPDF(htmlExtratoBoleto));
            pdfs.add(new FileInputStream(downloadfile));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ContratoRest.concatPDFs(pdfs, output, true);

            response = FesUtil.exportarArquivo(output.toByteArray(), "Boleto" + prefixoArquivos, ".pdf");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ocorreu um erro na geracao do boleto.", e);
            return Response.ok(e.getMessage()).build();
        }finally {
            if(downloadfile != null && downloadfile.exists()) {
                java.nio.file.Path path = Paths.get(downloadfile.getAbsolutePath());
                Files.delete(path);
            }
            if(fr != null) fr.close();
        }
        return response.build();
    }

    public static ByteArrayInputStream concatPDFs(List<InputStream> streamOfPDFFiles, ByteArrayOutputStream outputStream, boolean paginate) {

        Document document = new Document();
        try {
            List<InputStream> pdfs = streamOfPDFFiles;
            List<PdfReader> readers = new ArrayList<>();
            int totalPages = 0;
            Iterator<InputStream> iteratorPDFs = pdfs.iterator();

            // Create Readers for the pdfs.
            while (iteratorPDFs.hasNext()) {
                InputStream pdf = iteratorPDFs.next();
                PdfReader pdfReader = new PdfReader(pdf);
                readers.add(pdfReader);
                totalPages += pdfReader.getNumberOfPages();
            }
            // Create a writer for the outputstream
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
                    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
            // data

            PdfImportedPage page;
            int currentPageNumber = 0;
            int pageOfCurrentReaderPDF = 0;
            Iterator<PdfReader> iteratorPDFReader = readers.iterator();

            // Loop through the PDF files and add to the output.
            while (iteratorPDFReader.hasNext()) {
                PdfReader pdfReader = iteratorPDFReader.next();

                // Create a new page in the target for each source page.
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;
                    page = writer.getImportedPage(pdfReader,
                            pageOfCurrentReaderPDF);
                    cb.addTemplate(page, 0, 0);

                    // Code for pagination.
                    if (paginate) {
                        cb.beginText();
                        cb.setFontAndSize(bf, 9);
                        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
                                        + currentPageNumber + " of " + totalPages, 520,
                                5, 0);
                        cb.endText();
                    }
                }
                pageOfCurrentReaderPDF = 0;
            }
            outputStream.flush();
            document.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen())
                document.close();
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return new ByteArrayInputStream (outputStream.toByteArray());
    }

    @GET
    @Path("/getDadosAlunoCabecalho")
    @Produces(MediaType.APPLICATION_JSON)
    public RetornoListaVO<AcompanharProcessamentoVO> getDadosAlunoCabecalho(@QueryParam("codigoFies") Long codigoFies, @QueryParam("cpf") String cpf) {
        try {
            return new RetornoListaVO<>(acompanharProcessoService.getDadosAlunoCabecalho(codigoFies));
        } catch (Exception e) {
            return new RetornoListaVO<>(-1, e.getMessage());
        }
    }

    @GET
    @Path("/consultaStatusContratoCandidato")
    public Long consultaStatusContratoCandidato(@QueryParam("codigoFies") Long codigoFies) {
        try {
            if(!validaDadosPertenceUsuarioLogado(codigoFies, null)){
                return null;
            }
            return contratoServ.consultaStatusContratoCandidato(codigoFies);
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("/validarCriteriosContratacaoEstudante")
    public ValidacaoInicioContratacao validarCriteriosContratacaoEstudante(@QueryParam("cpf")
                                                                           String cpf, @QueryParam("codigoFies") Long codigoFies) {
        try {
            return contratoServ.validarCriteriosContratacaoEstudante(cpf, codigoFies);
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("/gerarRelatorioContratacaoDigital")
    @Produces({ "binary/octet-stream" })
    public Response gerarRelatorioContratacaoDigital(@QueryParam("dataInicial") String dataInicio, @QueryParam("dataFinal") String dataFim,
                                                     @QueryParam("canal") String canal, @QueryParam("uf") String uf) {
        ResponseBuilder response = null;
        try {
            Arquivo arquivo = contratoServ.gerarRelatorioContratacaoDigital(dataInicio, dataFim, canal, uf);
            response = FesUtil.exportarArquivo(arquivo.getDados(), arquivo.getNome(), ".xls");
        } catch (Exception e) {
            response = Response.ok(e.getMessage());
        }
        return response.build();
    }

    //IB-22564241
    protected Contrato consultarContrato(String cpf, Long codigoFies, int agencia) throws Exception {
        logger.log(Level.INFO, "Iniciando consultarContrato para cpf={0}, codigoFies={1}, agencia={2}",
                new Object[]{cpf, codigoFies, agencia});

        String usuario = getUsuarioLogado();
        Contrato contrato = null;

        contrato = contratoServ.consulta(usuario, cpf, codigoFies, agencia);
        contrato = consultaResumo(contrato.getEstudante().getCodigoFies());

        // PASSO 1: VALIDAÇÃO DE PERFIL DE ACESSO ESPECIAL
        //RTC_24491053
        if (this.possuiFuncao(FES_GESTOR) || this.possuiFuncao(FES_MANUT) || this.possuiFuncao(FES_MANUTJUR) || this.possuiFuncao(FES_CECOQGO)) {
            logger.log(Level.INFO, "Usuário ''{0}'' possui perfil especial. Acesso concedido.", usuario);
            return contrato;
        }


        if (INICIANDO.equals(contrato.getStatusContrato())) {
            logger.info("Acesso permitido: Status do contrato é INICIANDO.");
            return contrato;
        }

        // PASSO 2: USUÁRIO AGENCIA - BUSCAR DADOS E VALIDAR
        logger.log(Level.INFO, "Usuário ''{0}'' sem perfil especial. Prosseguindo com validações padrão.", usuario);

        if (!validaDadosPertenceUsuarioLogado(codigoFies, null)) {
            logger.log(Level.WARNING, "Acesso negado para dados do contrato {0} pelo usuário {1}.",
                    new Object[]{codigoFies, usuario});
            return new Contrato(403L, ERRO_403);
        }


        Contrato contratoParaValidacao = contrato;
        if (contratoParaValidacao == null) {
            logger.log(Level.WARNING, "Contrato não encontrado na consulta resumo para CPF: {0}, Código FIES: {1}", new Object[]{cpf, codigoFies});
            return new Contrato(-1L, "Contrato não encontrado.");
        }

        Contrato dadosDeAgencia = contratoServ.consulta(usuario, cpf, codigoFies, agencia);
        if (dadosDeAgencia == null) {
            logger.log(Level.WARNING, "Não foi possível obter os dados de agência para o contrato {0}.", codigoFies);
            return new Contrato(-1L, "Falha ao validar dados de agência do contrato.");
        }
        int agenciaContratoReal = dadosDeAgencia.getAgencia();


        Contrato resultadoAgenciaVinculada = contratoServ.consultaAgenciaVinculada(contratoParaValidacao, agenciaContratoReal);
        if (resultadoAgenciaVinculada != null && resultadoAgenciaVinculada.getAgenciaDeVinculacao() != null) {
            contratoParaValidacao.setAgenciaDeVinculacao(resultadoAgenciaVinculada.getAgenciaDeVinculacao());
        }


        int agenciaDeVinculacaoInt = 0;
        try {
            if (contratoParaValidacao.getAgenciaDeVinculacao() == null) {
                throw new NumberFormatException("Agência de Vinculação é nula");
            }

            agenciaDeVinculacaoInt = Integer.parseInt(contratoParaValidacao.getAgenciaDeVinculacao());
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Erro de formato na Agência de Vinculação ({0}).", contratoParaValidacao.getAgenciaDeVinculacao());
            return new Contrato(-1L, "Agência de Vinculação inválida: " + contratoParaValidacao.getAgenciaDeVinculacao());
        }


        // PASSO 3: LÓGICA DE NEGÓCIO
        if (contratoParaValidacao.getIndContratoNovoFies()) {
            logger.log(Level.INFO, "Contrato é Novo FIES. Aplicando validações específicas.");
            return validarContratoNovoFies(contratoParaValidacao, agencia, agenciaContratoReal, cpf, codigoFies);
        } else {
            logger.log(Level.INFO, "Aplicando validação de acesso por agência para FIES antigo.");
            return validarAcessoAgenciaParaUsuarioComum(contratoParaValidacao, agencia, agenciaContratoReal, cpf, codigoFies);
        }
    }

    //IB-22564241
    private Contrato validarContratoNovoFies(Contrato contrato, int agenciaUsuario, int agenciaContratoReal, String cpf, Long codigoFies) throws BusinessException {
        logger.log(Level.INFO, "Iniciando validarContratoNovoFies para contrato: {0}", contrato);
        Contrato validationResult = verificarTaxaJuros(contrato);
        if (validationResult != null) return validationResult;

        validationResult = verificarTarifaContrato(contrato);
        if (validationResult != null) return validationResult;

        validationResult = verificarSeguradorasVigentes(contrato);
        if (validationResult != null) return validationResult;

        return validarAcessoAgenciaParaUsuarioComum(contrato, agenciaUsuario, agenciaContratoReal, cpf, codigoFies);
    }

    //IB-22564241
    private Contrato verificarTaxaJuros(Contrato contrato) {
        if (contrato.getTaxaJuros() == null) {
            logger.warning("Verificação de juros: Taxa de juros nula.");
            return new Contrato(-1L, ERRO_TAXA_JUROS);
        }
        return null;
    }

    //IB-22564241
    private Contrato verificarTarifaContrato(Contrato contrato) {
        if (contrato.getExisteTarifaContrato() == null || !contrato.getExisteTarifaContrato()) {
            logger.warning("Verificação de tarifa: Tarifa de contrato não existe ou é falsa.");
            return new Contrato(-1L, ERRO_TARIFA_CONTRATO);
        }
        return null;
    }

    //IB-22564241
    private Contrato verificarSeguradorasVigentes(Contrato contrato) {
        if (contrato.getEstudanteCurso() == null || contrato.getEstudanteCurso().getCurso() == null) {
            logger.warning("Verificação de seguradoras: Dados do curso ou estudante ausentes no contrato.");
            return new Contrato(-1L, ERRO_DADOS_SEGURADORA);
        }
        List<Seguradora> seguradoras = seguradoraService.getSeguradorasVigentes(contrato.getEstudanteCurso().getCurso().getNome());
        if (seguradoras == null || seguradoras.isEmpty()) {
            logger.warning("Verificação de seguradoras: Lista de seguradoras nula ou vazia.");
            return new Contrato(-1L, ERRO_DADOS_SEGURADORA);
        }
        return null;
    }

    //IB-22564241
    private Contrato validarAcessoAgenciaParaUsuarioComum(Contrato contrato, int agenciaUsuario, int agenciaContratoReal, String cpf, Long codigoFies) throws BusinessException {
        logger.log(Level.INFO, "Iniciando validarAcessoAgenciaParaUsuarioComum para agência do contrato {0} e agência do usuário {1}",
                new Object[]{agenciaContratoReal, agenciaUsuario});


        int agenciaDeVinculacaoInt = Integer.parseInt(contrato.getAgenciaDeVinculacao());

        if ((agenciaUsuario == agenciaContratoReal) ||
                (agenciaUsuario == agenciaDeVinculacaoInt)) {

            logger.info("Acesso permitido: A agência do usuário corresponde a uma das agências do contrato OU o contrato já está na agência de vinculação.");
            return contrato;

        } else {
            logger.log(Level.WARNING, "Acesso negado: Agência do usuário ({0}) não corresponde à agência do contrato ({1}) nem à de vinculação ({2}), e as agências do contrato são diferentes.",
                    new Object[]{agenciaUsuario, agenciaContratoReal, agenciaDeVinculacaoInt});
            return new Contrato(-1L,"Você não tem acesso ao contrato pesquisado. Contrato está em andamento na Agência: "
                    + agenciaContratoReal + "." + "Agencia Lotação: " + agenciaUsuario + "." +
                    " Unidade Responsável Após Extincao: " + agenciaDeVinculacaoInt);
        }
    }

    @GET
    @Path("/listarTipoDocumentoPessoaWeb")
    public TipoDocumentoPessoaWeb listarTipoDocumentoPessoaWeb() {
        try {
            return contratoServ.listarTipoDocumentoPessoaWeb();
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("/buscarTipoDocumentoPessoa")
    public List<GenericTipoDocumentoPessoa> buscarTipoDocumentoPessoa() {
        try {
            return contratoServ.buscarTipoDocumentoPessoa();
        } catch (Exception e) {
            return null;
        }
    }

    @GET
    @Path("/buscarTipoPessoa")
    public List<GenericTipoDocumentoPessoa> buscarTipoPessoa() {
        try {
            return contratoServ.buscarTipoPessoa();
        } catch (Exception e) {
            return null;
        }
    }

    @POST
    @Path("/confirmarTipoDocumentoPessoa")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Retorno confirmarTipoDocumentoPessoa(ConfirmarTipoDocumentoPessoaDTO obj) {
        try {
            return contratoServ.confirmarTipoDocumentoPessoa(obj);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }

    @PUT
    @Path("/ativarInativar")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Retorno ativarInativar(@QueryParam("acao") Integer acao, @QueryParam("idDocumentoPessoa") Integer idDocumentoPessoa) {
        try {
            return contratoServ.ativarInativarDocumentoPessoa(acao, idDocumentoPessoa);
        } catch (Exception e) {
            return new Retorno(-1L, e.getMessage());
        }
    }


    /*	#################################
     * 	#						     	#
     * 	# API - CONTRATAÇÃO             #
     * 	#								#
     * 	# Outubro de 2023	            #
     *	#							 	#
     *	#################################
     */

    Retorno retorno401 = new Retorno(401L, "O token fornecido para acesso à API é inválido.", ERRO);
    Retorno retorno404 = new Retorno(404L, "Não foi localizado um contrato para o código Fies fornecido.", ERRO);
    Retorno retorno412 = new Retorno(412L, "Erro negocial ou estrutural na chamada da API.", ERRO);
    Retorno retorno412B = new Retorno(412L, "Código de operação do contrato inválido!", ERRO);
    Retorno retorno400 = new Retorno(400L, "\"CPF não cadastrado no SIFES.", ERRO);
    Retorno retorno400C = new Retorno(400L, " Erro ao gerar informações do cônjuge do estudante", ERRO);
    Retorno retorno500 = new Retorno(412L, "Erro interno do servidor.", ERRO);
    private static final String ERRO_BACKEND = "Erro na execução da funcionalidade no backend.";
    private String mensagemSucesso = "Sucesso";

    @GET
    @Path("/v1/validarCriteriosContratacaoEstudante")
    @ApiOperation(value = "Este serviço deve permitir validar todos os critérios de contratação do "
            + " estudante. Esses critérios incluem, por exemplo, verificar se o "
            + " estudante possui restrições cadastrais, se a data limite para "
            + " contratação está expirada, se já existe um contrato não liquidado, entre "
            + " outros.",
            notes = "O campo de entrada é o CPF/Código Fies obtidos através do token de login.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = ValidacaoInicioContratacao.class),
            @ApiResponse(code = 201, message = HttpResponseMessage.CODE_201, response = ValidacaoInicioContratacao.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    public Response validarCriteriosContratacaoEstudanteAPI() {
        try {

            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.getCodFiesPorCPFValidacaoContratoEstudante(cpf);
            String nomeMetodoLog = VALIDAR_CRITERIOS_CONTRATACAO_LOG + CPF_LOG;

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + VALIDAR_CRITERIOS_CONTRATACAO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + VALIDAR_CRITERIOS_CONTRATACAO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.validarCriteriosContratacaoEstudante(cpf, codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            } else if(retorno.getCodigo() == -2L) {
                retorno.setCodigo(-1L);
                retorno.setTipo(ERRO);
                retorno.setEditavel(false);
                retorno.setMensagem("Estudante com restrições que não permitem o processo da contratação.");
                return Response.status(201).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            if(e instanceof BusinessException) {
                Retorno retornoException = new Retorno();
                retornoException.setMensagem(e.getLocalizedMessage());
                retornoException.setCodigo(-1L);
                retornoException.setTipo(ERRO);
                return Response.status(201).entity(retornoException).build();
            }
            log.log(Level.SEVERE,ERRO_API + VALIDAR_CRITERIOS_CONTRATACAO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/alterarDadosCadastraisEstudante")
    @ApiOperation(value = "DadosCadastraisEstudanteAPI, com as informações atualizadas/confirmadas pelo estudante. "
            + " Deve ser informado também o canal de origem (1 - APP, 2 - WEB, 3 - AGÊNCIA)",
            notes = "O campo de entrada deve ser o objeto da classe DadosCadastraisEstudanteAPI, com as informações "
                    + " atualizadas/confirmadas pelo estudante. Deve ser informado também o canal de origem (1 - APP, 2 - WEB, 3 - AGÊNCIA).." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = DadosCadastraisEstudanteAPI.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response alterarDadosCadastraisEstudante(DadosCadastraisEstudanteAPI dadosCadastraisEstudante) {
        try {
            Gson gson = new Gson();
            mensagemSucesso = "Os dados cadastrais do estudante foram atualizados com sucesso.";
            String nomeMetodoLog = ALTERAR_DADOS_CADASTRAIS_LOG + CPF_LOG;

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ALTERAR_DADOS_CADASTRAIS_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ALTERAR_DADOS_CADASTRAIS_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.alterarDadosCadastraisEstudante(dadosCadastraisEstudante, codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/detalharCursoEstudante")
    @ApiOperation(value = "Este serviço deve permitir uma consulta aos dados do curso do estudante.",
            notes = "O campo de entrada é o CPF obtido através do token de login.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 400, message = HttpResponseMessage.CODE_400),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500)
    })
    public Response detalharCursoEstudanteAPI() {

        try {

            Gson gson = new Gson();
            mensagemSucesso = "";

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            String codFies = usuarioExterno.getCodFies().toString();

            log.log(Level.INFO, "ANALISANDO_LOG_APP - PASSO 1 - ENTRADA METODO: " + DETALHAR_CURSO_ESTUDANTE_API_LOG + " " +  gson.toJson(usuarioExterno));
            log.log(Level.INFO, DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if(!validaDadosPertenceUsuarioLogado(null, cpf)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DETALHAR_CURSO_ESTUDANTE_API_LOG + ": " + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            ConsultaAcompanharProcesso consultaAcompanharProcesso = new ConsultaAcompanharProcesso(codFies, cpf, null);

            Retorno retorno = acompanharProcessoService.consultarCursosIESbyCPF(consultaAcompanharProcesso);

            if (retorno == null) {
                return preparaRetornoAPI(retorno404, DETALHAR_CURSO_ESTUDANTE_API_LOG + " - CPF " + cpf);

            } else {
                return preparaRetornoAPI(retorno, DETALHAR_CURSO_ESTUDANTE_API_LOG + CPF_LOG + cpf);
            }

        } catch (FESException e) {
            log.log(Level.SEVERE, ERRO_API + DETALHAR_CURSO_ESTUDANTE_API_LOG);
            return trataExceptionAPI(e);
        }

    }

    @PUT
    @Path("/v1/alterarDadosContaContrato")
    @ApiOperation(value = "Este serviço permite atualizar no Sifes qual a conta Caixa que o estudante deseja "
            + "utilizar para débito das parcelas do financiamento.",
            notes = "O campo de entrada deve ser o objeto da classe ContaCorrente." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    public Response atualizaContaContrato(final ContaContrato conta) {

        Gson gson = new Gson();
        try {

            String mensagemSucesso = "A Conta Contrato foi atualizada com sucesso.";
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            Long codFies = usuarioExterno.getCodFies();
            String cpf = usuarioExterno.getCpf();

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ATUALIZA_CONTA_CONTRATO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));
            log.log(Level.SEVERE,"######## DADOS DE ENTRADA RECEBIDOS ==> OBJETO DA CLASSE ContaContrato: " + gson.toJson(conta));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ATUALIZA_CONTA_CONTRATO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.atualizarDadosBancarios(conta, codFies);
            if(retorno.getCodigo().equals(Long.valueOf(0L))) {
                return Response.status(Response.Status.OK).entity(new Retorno(200L, mensagemSucesso)).build();
            }
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(retorno).build();
        } catch(Exception e) {
            log.log(Level.SEVERE,ERRO_API + ATUALIZA_CONTA_CONTRATO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/confirmarDadosContrato")
    @ApiOperation(value = "Este serviço deve permitir a confirmação dos dados relacionados ao Contrato de Financiamento do estudante.",
            notes = "Os parâmetros de entrada são o CPF do estudante, que é obtido através do token, e o canal de origem (1 - APP, 2 - WEB, 3 - AGÊNCIA).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    public Response atualizarStatusContrato(@QueryParam("origem") String origem) {
        Gson gson = new Gson();
        try {

            String mensagemSucesso = "Processamento executado com sucesso.";
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            Long codFies = usuarioExterno.getCodFies();
            String cpf = usuarioExterno.getCpf();

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ATUALIZAR_STATUS_CONTRATO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ATUALIZAR_STATUS_CONTRATO_LOG + CPF_LOG + cpf);
                return Response.status(Response.Status.UNAUTHORIZED).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.atualizarStatusContrato(origem, codFies);
            if(retorno.getCodigo().equals(Long.valueOf(0L))) {
                return Response.status(Response.Status.OK).entity(new Retorno(200L, mensagemSucesso)).build();
            }

            return Response.status(Response.Status.PRECONDITION_FAILED).entity(retorno412).build();

        } catch(Exception e) {
            log.log(Level.SEVERE,ERRO_API + ATUALIZAR_STATUS_CONTRATO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/rejeitarContratacao")
    @ApiOperation(value = "Este serviço deve permitir rejeitar o processo de contratação.",
            notes = "Os parâmetros de entrada devem ser: o número da fase da contratação (1 - "
                    + " Confirmação de Dados Cadastrais, 2 - Confirmação de Curso/IES, 3 - "
                    + " Confirmação de Dados do Contrato); e o canal de origem (1 - APP, 2 - "
                    + " WEB, 3 - AGÊNCIA).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response cancelarValidacaoDadosEstudante(@QueryParam("fase") String fase, @QueryParam("origem") String origem) throws FESException {
        Gson gson = new Gson();

        try {
            final String meuMetodo = REJEITAR_CONTRATACAO_LOG;
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + REJEITAR_CONTRATACAO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(usuarioExterno.getCodFies(), null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + REJEITAR_CONTRATACAO_LOG + CPF_LOG + usuarioExterno.getCpf());
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.cancelarValidacaoDadosEstudante(fase, origem, usuarioExterno.getCodFies());

            if (retorno.getCodigo() != 0L) {
                return Response.status(412).entity(retorno412).build();
            }

            return Response.ok().entity(new ResultadoApiDTO(true, MensagemValidacaoDadosEstudante.SUCCESS, retorno.getCodigo(), retorno.getTipo(), retorno.getEditavel())).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + REJEITAR_CONTRATACAO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarContasCaixaEstudante")
    @ApiOperation(value = "Este serviço deve permitir lista contas do estudante.",
            notes = "O campo de entrada deve ser o cpf." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = ListaContaCorrenteEstudante.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarContasCaixaEstudante() {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            String usuario = isUsuarioInterno() ? getUsuarioLogado() : System.getProperty(USER_SICLI);
            Retorno retorno = contratoServ.listarContasCaixaEstudante(cpf, usuario);
            mensagemSucesso = retorno.getMensagem();

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG + CPF_LOG + cpf);
            }
            else {
                return preparaRetornoAPI(retorno, LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + LISTAR_CONTAS_CAIXA_ESTUDANTE_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/confirmarDadosCursoIesEstudante")
    @ApiOperation(value = "Este serviço deve permitir a confirmação dos dados relacionados ao Curso e a IES do estudante.",
            notes = "Os campos de entrada são o CPF do estudante que é obtido através do token e a origem (1 - APP, 2 - WEB, 3 - AGÊNCIA).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response confirmarDadosCursoIes(@QueryParam("origem") String origem) {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + CONFIRMAR_DADOS_CURSO_IES_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + CONFIRMAR_DADOS_CURSO_IES_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.confirmarDadosCursoIes(codFies, origem);
            mensagemSucesso = retorno.getMensagem();

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, CONFIRMAR_DADOS_CURSO_IES_LOG + CPF_LOG + cpf);
            }
            else {
                return preparaRetornoAPI(retorno, CONFIRMAR_DADOS_CURSO_IES_LOG + CPF_LOG + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + CONFIRMAR_DADOS_CURSO_IES_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/detalharContrato")
    @ApiOperation(value = "Este serviço deve permitir uma consulta aos dados do contrato do estudante.",
            notes = "O campo de entrada é o CPF obtido através do token de login.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response detalharContrato() {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.getCodFiesPorCPFValidacaoContratoEstudante(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + DETALHAR_CONTRATO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DETALHAR_CONTRATO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno contrato = (Contrato)consultaResumo(codFies);
            contrato = contratoServ.getNomeCidadeEstudantePorMunicipio((Contrato)contrato);

            if(contrato != null) {
                if(((Contrato) contrato).getEstudante() == null ) {
                    return Response.status(404).entity(retorno404).build();
                } else {
                    log.log(Level.INFO, "ANALISANDO_LOG_APP - RETORNO METODO: " + DETALHAR_CONTRATO_LOG + " - RESPONSE 200 - CPF_ENTRADA: " + cpf + " OBJETO DE RETORNO: " + gson.toJson(contrato));
                    mensagemSucesso = CONSULTA_REALIZADA_COM_SUCESSO;
                    contrato.setCodigo(0L);
                    return preparaRetornoAPI(contrato, DETALHAR_CONTRATO_LOG + CPF_LOG + cpf);
                }
            } else {
                return Response.status(412).entity(retorno412).build();
            }

        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + DETALHAR_CONTRATO_LOG);
            return trataExceptionAPI(e);
        }
    }

    // TODO - ver se preencheu todas as fase e ver os documentos
    @POST
    @Path("/v1/efetivarContratacao")
    @ApiOperation(value = "Este serviço deve permitir efetuar os procedimentos inerentes à realização da Contratação.",
            notes = "O campo de entrada é o CPF obtido através do token de login.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response enviarContratoSIAPI() {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ENVIAR_CONTRATO_SIAPI_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ENVIAR_CONTRATO_SIAPI_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            RetornoEfetivarContratacao retornoSiapi = contratoServ.efetivarContratacao("C000000", codFies, true, cpf);
            mensagemSucesso = retornoSiapi.getMensagem();

            if(retornoSiapi.getCodigo() == -1L) {
                retornoSiapi.setTipo(ERRO);
                return preparaRetornoAPI(retornoSiapi, ENVIAR_CONTRATO_SIAPI_LOG + CPF_LOG + cpf);
            } else {
                return preparaRetornoAPI(retornoSiapi, ENVIAR_CONTRATO_SIAPI_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            if(e instanceof BusinessException) {
                String msgErro = "";
                if(e.getMessage().contains("SIAPI #"))
                    msgErro = e.getMessage().substring(e.getMessage().indexOf("SIAPI #"), e.getMessage().length());
                RetornoEfetivarContratacao retorno = new RetornoEfetivarContratacao(-1L,
                        "Erro ao efetivar contratação.",ERRO, msgErro);
                return Response.ok().entity(retorno).build();
            } else {
                log.log(Level.SEVERE,ERRO_API + ENVIAR_CONTRATO_SIAPI_LOG);
                return trataExceptionAPI(e);
            }
        }
    }

    @PUT
    @Path("/v1/alterarAgenciaVinculacaoContrato")
    @ApiOperation(value = "Este serviço deve permitir alterar a agência que está vinculada ao contrato possibilitando que o estudante escolha uma nova "
            + "agência próxima a sua residência ou ao local de estudo.",
            notes = "O campo de entrada é o CPF obtido através do token do usuário e o número da nova agência de vinculação desejada.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response alterarAgenciaVinculacaoContrato(@QueryParam("agencia") Integer agencia) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retornoSiapi = contratoServ.alterarAgenciaVinculacaoContrato(codFies, agencia);
            mensagemSucesso = "Alteração de agência vinculada realizada com sucesso.";

            if(retornoSiapi.getCodigo() == -1L) {
                retornoSiapi.setTipo(ERRO);
                return preparaRetornoAPI(retornoSiapi, ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG + CPF_LOG + cpf);
            } else {
                return preparaRetornoAPI(retornoSiapi, ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + ALTERAR_AGENCIA_VINCULACAO_CONTRATO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/disponibilizarTermo/{cidade}/{tipo}")
    @ApiOperation(value = "Este serviço deve retornar o termo do contrato para ser revisado pelo estudante.",
            notes = "O parâmetro de entrada é a cidade de residência do estudante que será colocada na assinatura do contrato.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = TermoContratoDTO.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response disponibilizarTermo(@PathParam("cidade") String cidade, @PathParam("tipo") String tipo) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.getCodFiesPorCPFValidacaoContratoEstudante(cpf);
            String formaContratacao = contratoServ.getFormaContracao(codFies);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + DISPONIBILIZAR_TERMO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DISPONIBILIZAR_TERMO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            TermoContratoDTO retornoTermoContrato = new TermoContratoDTO();

            boolean hasProuni = contratoServ.hasProuni(codFies);

            Contrato contrato = contratoServ.consultaResumo(getUsuarioLogado(), codFies, "");
            int modulo = hasProuni ? Integer.parseInt(ID_TERMO_CONTRATO_DIGITAL_PROUNI) : Integer.parseInt(ID_TERMO_CONTRATO_DIGITAL);

            TermoCandidatoTO termoCandidatoTO = termoService.consultaResultadoSimulacaoPorContratacaoDigital(modulo, contrato, tipo, codFies);

            log.severe(String.format("{'ID_TERMO_CONTRATO_DIGITAL_PROUNI': '%s', 'ID_TERMO_CONTRATO_DIGITAL': '%s'}", ID_TERMO_CONTRATO_DIGITAL_PROUNI, ID_TERMO_CONTRATO_DIGITAL ));


            ConsultaTermoDTO consultaTermoDTO = new ConsultaTermoDTO();
            consultaTermoDTO.setModulo(modulo);
            consultaTermoDTO.setCodigoFies(codFies.toString());
            consultaTermoDTO.setCodigoUsuario(getUsuarioLogado());
            consultaTermoDTO.setLocalAssinatura(cidade);
            consultaTermoDTO.setTermo(termoCandidatoTO.getTermo());

            String termoPreenchido = termoService.consultaMontaTermoImpressao(consultaTermoDTO);
            termoCandidatoTO.setTermo(termoPreenchido);

            String mensagem = termoCandidatoTO.getTermo();

            if (StringUtils.isBlank(mensagem) || mensagem.equals("Registro ou Termo não Localizado")) {
                retornoTermoContrato.setCodigo(1L);
                retornoTermoContrato.setMensagem(mensagem);
            } else {
                retornoTermoContrato.setCodigo(0L);
                retornoTermoContrato.setMensagem("Termo obtido com sucesso.");
                retornoTermoContrato.setTermoContrato(termoCandidatoTO.getTermo());
            }

            return preparaRetornoAPI(retornoTermoContrato, DISPONIBILIZAR_TERMO_LOG + CPF_LOG + cpf);
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + DISPONIBILIZAR_TERMO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/realizarUploadDocumentos")
    @ApiOperation(value = "Este serviço deve permitir realizar o Upload de documentos.",
            notes = "O campo de entrada é um objeto da classe UploadDocumentoContratacao, que contém o canal de origem "
                    + " (1 - APP, 2 - WEB, 3 - AGÊNCIA); reenvio (true ou false); e uma lista contendo tuplas ('tipoDocumento', 'binario').")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 400, message = HttpResponseMessage.CODE_400 ,response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response uploadDocumento(UploadDocumentoContratacao uploadDocumentoContratacao) {
        try {
            Gson gson = new Gson();
            String nomeMetodoLog = UPLOAD_DOCUMENTO_LOG + CPF_LOG;

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + UPLOAD_DOCUMENTO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + UPLOAD_DOCUMENTO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.uploadReenvioDocumento(uploadDocumentoContratacao, codFies, cpf, securityKeycloakUtils.getTokenApiManager());

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, UPLOAD_DOCUMENTO_LOG + CPF_LOG + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return Response.status(400).entity(retorno).build();
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, UPLOAD_DOCUMENTO_LOG + CPF_LOG + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + UPLOAD_DOCUMENTO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarTipoDocumentoPessoa")
    @ApiOperation(value = "Este serviço deve retornar uma lista dos tipos de documentos exigidos para o usuário concluir a Contratação do FIES.",
            notes = "Os parâmetros de entrada são: ingressoProcesso, que  informa se é "
                    + " Contratação (C), Aditamento (A) ou Renegociação (R); tipoPessoa, que "
                    + " informa se é um ESTUDANTE(1), CÔNJUGE ESTUDANTE(2), "
                    + " FIADOR(3), CÔNJUGE FIADOR(4) ou REPRESENTANTE LEGAL(5); "
                    + " e idDocumento, que informa se é um DOCUMENTO DE IDENTIFICAÇÃO(1) ou COMPROVANTE DE ENDEREÇO(2).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listaTipoDocumentoPessoa(@QueryParam("tipoPessoa") Long tipoPessoa, @QueryParam("idDocumento") Long idDocumento,
                                             @QueryParam("ingressoProcesso") String ingressoProcesso) {

        try {
            Gson gson = new Gson();
            String nomeMetodoLog = LISTA_TIPO_DOCUMENTO_PESSOA_LOG + CPF_LOG;

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTA_TIPO_DOCUMENTO_PESSOA_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTA_TIPO_DOCUMENTO_PESSOA_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.listaTipoDocumentoPessoa(tipoPessoa, idDocumento, ingressoProcesso);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, LISTA_TIPO_DOCUMENTO_PESSOA_LOG + CPF_LOG + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, LISTA_TIPO_DOCUMENTO_PESSOA_LOG + CPF_LOG + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, LISTA_TIPO_DOCUMENTO_PESSOA_LOG + CPF_LOG + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + LISTA_TIPO_DOCUMENTO_PESSOA_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/obterListaDocumentosEnviados")
    @ApiOperation(value = "Este serviço deve permitir obter uma lista de todos os documentos enviados.",
            notes = "O campo de entrada deve ser o CPF de quem se deseja obter a lista dos documentos enviados.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarDocumentosEnviados() {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();

            String nomeMetodoLog = OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + CPF_LOG + cpf;

            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.obterListaDocumentosEnviados(cpf, codFies, securityKeycloakUtils.getTokenApiManager());

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + CPF_LOG + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + CPF_LOG + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG + CPF_LOG + cpf);
            }
        } catch (Exception e) {
            if(e instanceof BusinessException) {
                if(e.getLocalizedMessage().contains("Documentos não enviados")) {
                    Retorno retornoException = new Retorno();
                    retornoException.setMensagem(e.getLocalizedMessage());
                    retornoException.setCodigo(0L);
                    retornoException.setTipo(SUCESSO);
                    return Response.ok().entity(retornoException).build();
                }
            }
            log.log(Level.SEVERE, ERRO_API + OBTER_LISTA_DOCUMENTOS_ENVIADOS_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/agencia")
    @ApiOperation(value = "Este serviço deve permitir listar as agências da caixa próximas.",
            notes = "Os campos de entrada podem ser o Codigo do Municipio, UF e Agencia." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = ListaAgencia.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarAgenciasCaixa(@QueryParam("codigoMunicipio") Long codigoMunicipio, @QueryParam("uf") String uf,
                                        @QueryParam("agencia") Long agencia) {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.getCodFiesPorCPFValidacaoContratoEstudante(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_AGENCIAS_CAIXA_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTAR_AGENCIAS_CAIXA_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = agenciaService.consultar(codigoMunicipio, uf, agencia);
            mensagemSucesso = retorno.getMensagem();

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, LISTAR_AGENCIAS_CAIXA_LOG + CPF_LOG + cpf);
            }
            else {
                return preparaRetornoAPI(retorno, LISTAR_AGENCIAS_CAIXA_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + LISTAR_AGENCIAS_CAIXA_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/incluirDadosBasicosFiador")
    @ApiOperation(value = "Este serviço deve permitir incluir um fiador para o candidato.",
            notes = "O campos de entrada é o obj FiadorContratacao." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = ListaContaCorrenteEstudante.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response incluirFiador(FiadorContratacao fiadorContratacao) {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## INCLUIR FIADOR - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + INCLUIR_FIADOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + INCLUIR_FIADOR_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.incluirFiadorApp(fiadorContratacao, codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + INCLUIR_FIADOR_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/alterarDadosBasicosFiador")
    @ApiOperation(value = "Este serviço deve permitir alterar um fiador para o candidato.",
            notes = "O campos de entrada é o obj FiadorContratacao." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response alterarFiador(FiadorContratacao fiadorContratacao) {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## ALTERAR FIADOR - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ALTERAR_FIADOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ALTERAR_FIADOR_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.alterarFiadorApp(fiadorContratacao, codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + ALTERAR_FIADOR_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/validarCpf/{CPF}")
    @ApiOperation(value = "Este serviço deve permitir incluir um fiador para o candidato.",
            notes = "O campos de entrada é o obj FiadorContratacao." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = DadosCpfRetorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = DadosCpfRetorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = DadosCpfRetorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = DadosCpfRetorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response validarCpf(@PathParam("CPF") final String CPF) {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String usuarioExternoCpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(usuarioExternoCpf);

            String nomeMetodoLog = "######## INCLUIR FIADOR - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + INCLUIR_FIADOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + INCLUIR_FIADOR_LOG + CPF_LOG + usuarioExternoCpf);
                return Response.status(401).entity(retorno401).build();
            }

            DadosCpfRetorno retorno = contratoServ.consultarCpfNoSICPF(CPF);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, INCLUIR_FIADOR_LOG + CPF_LOG + usuarioExternoCpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, INCLUIR_FIADOR_LOG + CPF_LOG + usuarioExternoCpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, INCLUIR_FIADOR_LOG + CPF_LOG + usuarioExternoCpf);
            }
        } catch (Exception e) {
            if(e instanceof FESException) {
                Retorno retorno404 = new Retorno(-1L, e.getLocalizedMessage(), ERRO);
                logger.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 404 ########");
                return Response.status(Response.Status.NOT_FOUND).entity(retorno404).build();
            }
            log.log(Level.SEVERE, ERRO_API + INCLUIR_FIADOR_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/validarCriteriosFiador/{cpfFiador}")
    @ApiOperation(value = "Este serviço deve permitir validar todos os critérios de contratação do "
            + " fiador. Esses critérios incluem, por exemplo, verificar se o "
            + " fiador possui restrições cadastrais, "
            + " contrato ativo, dentre outros.",
            notes = "O campo de entrada é o CPF/Código Fies obtidos através do token de login e o cpf do fiador.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = ValidacaoFiadorContratacaoApp.class),
            @ApiResponse(code = 201, message = HttpResponseMessage.CODE_201, response = ValidacaoFiadorContratacaoApp.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = ValidacaoFiadorContratacaoApp.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = ValidacaoFiadorContratacaoApp.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = ValidacaoFiadorContratacaoApp.class)
    })
    public Response validarCriteriosFiador(@PathParam("cpfFiador") String cpfFiador) {
        try {

            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## VALIDAR FIADOR CADASTRO - CPF: ";

            log.log(Level.SEVERE, "######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: validarCriteriosFiador ==> DADOS USUÁRIO LOGADO: " + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 401 ######  Método ==> validarCriteriosFiador - CPF: " + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            ValidacaoFiadorContratacaoApp retorno = contratoServ.validarCriteriosFiador(cpfFiador, codFies, cpf);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            } else if(retorno.getCodigo() == -2L) {
                retorno.setCodigo(-1L);
                retorno.setTipo(ERRO);
                retorno.setEditavel(false);
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, "validarCriteriosFiador - CPF: " + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + "validarCriteriosFiador");
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarFiadores")
    @ApiOperation(value = "Este serviço retorna uma lista de objetos da classe Fiador que são todos os Fiadores do estudante.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarFiador() {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## LISTAR FIADOR - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_FIADOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTAR_FIADOR_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.listarFiador(codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {

            log.log(Level.SEVERE, ERRO_API + LISTAR_FIADOR_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/atualizarSeguradoraContrato")
    @ApiOperation(value = "Este serviço permite atualizar a seguradora do candidato.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token e o obj SeguradoraContratacao com dados de entrada,"
                    + " idSeguradora, valorSeguroVigente." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response atualizarSeguradoraContrato(SeguradoraContratacao seguradora) {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## ATUALIZAR SEGURADORA - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + ATUALIZAR_SEGURADORA_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + ATUALIZAR_SEGURADORA_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = contratoServ.atualizarSeguradoraContrato(codFies, seguradora, cpf);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + ATUALIZAR_SEGURADORA_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarSeguradoras")
    @ApiOperation(value = "Este serviço retorna uma lista de de seguradoras.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token e o nome do curso." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = RetornoSeguradorasContratacao.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarSeguradoras(@QueryParam("curso") String curso) {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## LISTAR SEGURADORAS - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_SEGURADORAS_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTAR_SEGURADORAS_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            RetornoSeguradorasContratacao retorno = contratoServ.listarSeguradorasVigentesPorCurso(curso);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + LISTAR_SEGURADORAS_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/incluirDadosComplementaresFiador")
    @ApiOperation(value = "Este serviço deve permitir incluir dados complementares do fiador.",
            notes = "O campos de entrada é o obj FiadorContratacao e o código Fies do estudante que é obtido através do token." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404, response = Retorno.class),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response incluirDadosComplementaresFiador(FiadorContratacaoComplementar fiadorContratacao) {

        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            String nomeMetodoLog = "######## INCLUIR DADOS COMPLEMENTARES FIADOR - CPF CANDIDATO: ";
            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + INCLUIR_DADOS_COMPLEMENTARES_FIADOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            Retorno retorno = contratoServ.incluirDadosComplementaresFiador(fiadorContratacao);

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + INCLUIR_DADOS_COMPLEMENTARES_FIADOR_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarRepresentanteLegal")
    @ApiOperation(value = "Este serviço retorna uma lista de de representante legal.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = RetornoSeguradorasContratacao.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarRepresentanteLegal() {

        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## LISTAR REPRESENTANTE LEGAL - CPF CANDIDATO: ";

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_REPRESENTANTE_LEGAL_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + LISTAR_REPRESENTANTE_LEGAL_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            RepresentanteLegalContratacao retorno = contratoServ.listarRepresentanteLegal(codFies);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            }
            else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, LISTAR_REPRESENTANTE_LEGAL_LOG + CPF_LOG + cpf);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + LISTAR_REPRESENTANTE_LEGAL_LOG);
            if(e instanceof BusinessException) {
                Retorno retorno = new Retorno();
                retorno.setCodigo(-1L);
                retorno.setTipo(ERRO);
                retorno.setEditavel(false);
                retorno.setMensagem(e.getLocalizedMessage());
                return Response.status(200).entity(retorno).build();
            } else {
                return trataExceptionAPI(e);
            }
        }
    }

    @POST
    @Path("/v1/rejeitarParticipacaoContrato/{participante}")
    @ApiOperation(value = "Este serviço rejeita a participação no contrato.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = RetornoSeguradorasContratacao.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response rejeitarParticipacaoContrato(@PathParam("participante") Integer participante) {

        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + REJEITAR_PARTICIPACAO_CONTRATO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            String nomeMetodoLog = "######## REJEITAR PARTICIPAÇÃO CONTRATO: ";

            Retorno retorno = contratoServ.rejeitarParticipacaoContrato(cpf, participante);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + REJEITAR_PARTICIPACAO_CONTRATO_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/enviarEmail")
    @ApiOperation(value = "Este serviço rejeita a participação no contrato.",
            notes = "O campo de entrada é o código Fies do estudante que é obtido através do token." )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = RetornoSeguradorasContratacao.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = RetornoSeguradorasContratacao.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response enviarEmail(EmailMessageTO email) {

        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            log.log(Level.SEVERE, "######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: enviar email ==> DADOS USUÁRIO LOGADO: " + gson.toJson(usuarioExterno));

            String nomeMetodoLog = "######## ENVIAR EMAIL: ";

            Retorno retorno = contratoServ.enviarEmail(email);

            if(retorno ==  null) {
                return preparaRetornoAPI(retorno404, nomeMetodoLog + cpf);
            }

            if(retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, nomeMetodoLog + cpf);
            }
        } catch (Exception e) {
            if(e instanceof BusinessException) {
                Retorno retornoException = new Retorno();
                retornoException.setMensagem(e.getLocalizedMessage());
                retornoException.setCodigo(-1L);
                retornoException.setTipo(ERRO);
                return Response.serverError().entity(retornoException).build();
            } else {
                log.log(Level.SEVERE, ERRO_API.concat("enviarEmail"));
                return trataExceptionAPI(e);
            }
        }
    }

    @GET
    @Path("/v1/listarDadosConjuge")
    @ApiOperation(value = "Este serviço retorna os dados do Conjuge do estudante.", notes = "O campo de entrada é o código Fies do estudante que é obtido através do token.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarDadosConjuge() {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## LISTAR DADOS CONJUGE - CPF CANDIDATO: ";
            log.log(Level.SEVERE,"######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: listar dados conjuge ==> DADOS USUÁRIO LOGADO: " + gson.toJson(usuarioExterno));

            DadosBasicosConjugeCandidatoTO dadosBasicosConjugeCandidatoTO = contratoServ.listarDadosBasicosConjugeApp(codFies, null);
            log.log(Level.SEVERE, "######## RETORNANDO CÓDIGO 200 ######  Método ==> listar dados conjuge - CPF: " + nomeMetodoLog + cpf);

            return Response.ok().entity(dadosBasicosConjugeCandidatoTO).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/alterarDadosBasicosConjuge")
    @ApiOperation(value = "DadosBasicosConjugeCandidatoTO, Este serviço deve permitir atualizar os dados do cônjuge do candidato.", notes = "O campo de entrada é o obj DadosBasicosConjugeCandidatoTO.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response atualizaDadosBasicosConjugeApp(DadosBasicosConjugeCandidatoTO conjuge) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();

            log.log(Level.SEVERE,
                    ANALISANDO_LOG_APP_INICIO + ATUALIZA_DADOS_BASICOS_CONJUGE_LOG + DADOS_USUARIO_LOGADO
                            + gson.toJson(usuarioExterno));

            String nomeMetodoLog = "######## ATUALIZAR DADOS CONJUGE - CPF CANDIDATO: ";
            contratoServ.validarCamposConjuge(conjuge);
            DadosCpfRetorno retornoCpf = contratoServ.consultarCpfNoSICPF(conjuge.getCPF());

            if (retornoCpf.getSituacaoCPF() != 0) {
                String msg = montaMensagemErroSicPF(retornoCpf.getSituacaoCPF());
                Retorno retornoException = new Retorno(-1L, msg, ERRO);
                return Response.status(200).entity(retornoException).build();
            }

            Retorno retorno = contratoServ.validaAtualizaDadosBasicosConjugeApp(cpf, conjuge, getUsuarioLogado());

            if (retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, ATUALIZA_DADOS_BASICOS_CONJUGE_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + ATUALIZA_DADOS_BASICOS_CONJUGE_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarTiposDocumentos")
    @ApiOperation(value = "Este serviço retorna os tipos de documentos exigidos para enviar para o SIMTR.", notes = "O campo de entrada é o código Fies do estudante que é obtido através do token.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarTiposDocumentos() {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);
            String nomeMetodoLog = "######## LISTAR TIPOS DE DOCUMENTOS - CPF CANDIDATO: ";
            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + LISTAR_TIPOS_DOCUMENTOS_LOG + DADOS_USUARIO_LOGADO
                            + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE,
                        RETORNANDO_CODIGO_401 + LISTAR_TIPOS_DOCUMENTOS_LOG + CPF_LOG
                                + cpf);
                return Response.status(400).entity(retorno400).build();
            }
            List<TipoDocumentoDto> tiposDocumentoTO = contratoServ.listarTiposDocumentos();
            if (tiposDocumentoTO.isEmpty()) {
                return Response.status(400).entity(retorno400D).build();
            }

            log.log(Level.SEVERE, "######## RETORNANDO CÓDIGO 200 ######  Método ==> " + LISTAR_TIPOS_DOCUMENTOS_LOG + ": "
                    + nomeMetodoLog + cpf);
            return Response.ok().entity(tiposDocumentoTO).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + LISTAR_TIPOS_DOCUMENTOS_LOG, e);
            return Response.status(400).entity(retorno400E).build();
        }
    }



    public String montaMensagemErroSicPF(Integer codigo) {

        String msg = "CPF informado com restrição. Favor regularizar o CPF para continuar a contratação: ";
        if (codigo == 2) {
            msg += "Suspensa";
        }
        if (codigo == 3) {
            msg += "Titular Falecido";
        }
        if (codigo == 4) {
            msg += "Pendente de Regularização";
        }
        if (codigo == 5) {
            msg += "Cancelada por Multiplicidade";
        }
        if (codigo == 8) {

            msg += "Nula";
        }
        if (codigo == 9) {
            msg += "Cancelada de Oficio";
        }

        return msg;
    }

    @PUT
    @Path("/v1/incluirDadosComplementaresConjuge")
    @ApiOperation(value = "DadosComplementaresConjugeCandidatoTO, Este serviço deve permitir incluir os dados complementares do cônjuge do candidato.", notes = "O campo de entrada é o obj DadosComplementaresConjugeCandidatoTO.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

    public Response incluirDadosComplementaresConjugeApp(DadosComplementaresConjugeTO dadosComplementares) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            dadosComplementares.setCpfDependente(0);
            String nomeMetodoLog = "######## INCLUIR DADOS COMPLEMENTARES CONJUGE DO ESTUDANTE OU FIADOR - CPF CANDIDATO: ";

            log.log(Level.SEVERE,
                    ANALISANDO_LOG_APP_INICIO + INCLUIR_DADOS_COMPLEMENTARES_CONJUGE_LOG + DADOS_USUARIO_LOGADO
                            + gson.toJson(usuarioExterno));

            Retorno retorno = contratoServ.validaIncluirDadosComplementaresConjugeApp(cpf, dadosComplementares,
                    getUsuarioLogado());

            if (retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, INCLUIR_DADOS_COMPLEMENTARES_CONJUGE_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + INCLUIR_DADOS_COMPLEMENTARES_CONJUGE_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/incluirDadosBasicosDoRepresentanteLegal")
    @ApiOperation(value = "DadosBasicosDoRepresentelegalTo, Este serviço deve permitir incluir os dados basicos do representante legal do candidato.", notes = "O campo de entrada é o obj DadosBasicosRepresenteLegalTo.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

    public Response incluirDadosBasicosRepresentanteLegalApp(DadosBasicosRepresentanteLegalTO dadosRepresentanteLegal) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);

            String nomeMetodoLog = "######## INCLUIR DADOS BASICOS DO REPRESENTANTE LEGAL - CPF CANDIDATO: ";

            log.log(Level.SEVERE,
                    ANALISANDO_LOG_APP_INICIO + INCLUIR_DADOS_BASICOS_REPRESENTANTE_LEGAL_LOG + DADOS_USUARIO_LOGADO
                            + gson.toJson(usuarioExterno));

            if (dadosRepresentanteLegal.getCpf() != null) {
                DadosCpfRetorno retornoCpf = contratoServ.consultarCpfNoSICPF(dadosRepresentanteLegal.getCpf());
                if (retornoCpf.getSituacaoCPF() != 0) {
                    String msg = montaMensagemErroSicPF(retornoCpf.getSituacaoCPF());
                    Retorno retornoException = new Retorno(-1L, msg, ERRO);
                    return Response.status(200).entity(retornoException).build();
                }
            }

            Retorno retorno = contratoServ.validaIncluirDadosBasicosRepresentanteLegalApp(cpf, codFies,
                    dadosRepresentanteLegal, getUsuarioLogado());

            if (retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, INCLUIR_DADOS_BASICOS_REPRESENTANTE_LEGAL_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + INCLUIR_DADOS_BASICOS_REPRESENTANTE_LEGAL_LOG);
            return trataExceptionAPI(e);
        }
    }

    @PUT
    @Path("/v1/incluirDadosComplementaresRepresentanteLegal")
    @ApiOperation(value = "DadosComplementaresRepresentelegalTo, Este serviço deve permitir incluir os dados complementares do representante legal do candidato.", notes = "O campo de entrada é o obj DadosComplementaresRepresenteLegalTo.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

    public Response incluirDadosComplementaresRepresentanteLegalApp(
            DadosComplementaresRepresentanteLegalTO dadosRepresentanteLegal) {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();

            dadosRepresentanteLegal.setnDependenteEstudante(0);

            log.log(Level.SEVERE,
                    ANALISANDO_LOG_APP_INICIO + INCLUIR_DADOS_COMPLEMENTARES_REPRESENTANTE_LEGAL_LOG + DADOS_USUARIO_LOGADO
                            + gson.toJson(usuarioExterno));

            String nomeMetodoLog = "######## INCLUIR DADOS COMPLEMENTARES DO REPRESETANTE LEGAL: ";


            DadosCpfRetorno retornoCpf = contratoServ.consultarCpfNoSICPF(cpf);
            if (retornoCpf.getSituacaoCPF() != 0) {
                String msg = montaMensagemErroSicPF(retornoCpf.getSituacaoCPF());
                Retorno retornoException = new Retorno(-1L, msg, ERRO);
                return Response.status(200).entity(retornoException).build();
            }


            Retorno retorno = contratoServ.validaIncluirDadosComplementaresRepresentanteLegalApp(cpf,
                    dadosRepresentanteLegal, getUsuarioLogado());

            if (retorno.getCodigo() == -1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, INCLUIR_DADOS_COMPLEMENTARES_REPRESENTANTE_LEGAL_LOG + CPF_LOG + cpf);
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + INCLUIR_DADOS_COMPLEMENTARES_REPRESENTANTE_LEGAL_LOG);
            return trataExceptionAPI(e);
        }
    }

    @POST
    @Path("/v1/uploadDocumentoPDF")
    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPDF(@MultipartForm FormUploadDTO form) throws IOException {

        Retorno retorno = new Retorno();
        try {
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);
            log.log(Level.SEVERE,"######## INICIO METODO UPLOAD-DOCUMENTO: " + codFies);

            byte[] pdfBytes = new byte[0];
            try(ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = form.getFile().read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                pdfBytes = baos.toByteArray();
            } catch (Exception e) {
                log.log(Level.SEVERE,"######## ERRO AO LER ARQUIVO PDF: ");
            } finally {
                if(form.getFile() != null)
                    form.getFile().close();
            }

            String base64 = Base64.encodeBase64String(pdfBytes).replace("\r\n","");

            log.log(Level.SEVERE,"######## BASE64 CONVERTIDO: " + base64);

            UploadDocumentoContratacao up = new UploadDocumentoContratacao();
            up.setOrigem(form.getOrigem());
            up.setReenvio(form.getReenvio());
            List<UploadArquivoDTO> list = new ArrayList<UploadArquivoDTO>();
            list.add(new UploadArquivoDTO(form.getTipoDocumento(), base64));
            up.setUploadArquivoDTO(list);

            retorno = contratoServ.uploadReenvioDocumento(up, codFies, cpf, securityKeycloakUtils.getTokenApiManager());

        } catch (Exception e) {
            log.log(Level.SEVERE,"######## ERRO AO PROCESSAR UPLOAD ARQUIVO: " + e.getLocalizedMessage());
            return Response.status(Response.Status.OK).entity(new Retorno(-1L, "Erro ao processar upload arquivo.", ERRO)).build();
        }

        log.log(Level.SEVERE,"######## FIM METODO UPLOAD-DOCUMENTO: " + retorno.getMensagem());
        return Response.ok(retorno).build();
    }

    @DELETE
    @Path("/v1/excluirFiador/{cpf}")
    @ApiOperation(value = "Os campos de entrada devem ser o CPF do Fiador e o código Fies do estudante que é obtido através do token.", notes = "O campo de entrada deve ser OBJECT com a propriedade 'body'")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404 ,response = Retorno.class),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    public Response excluirFiadorPorCpf(@PathParam("cpf") String cpf) {
        try {
            if(!cpf.matches("[0-9]+")) {
                retorno412.setMensagem("O CPF do fiador que foi informado está vazio, mal-formatado ou inválido.");
                return Response.status(412).entity(retorno412).build();
            }

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            usuarioExterno.setCodFies(contratoServ.retornarCodFiesPorCPF(usuarioExterno.getCpf()));

            final String NOME_METHOD = "######## EXCLUIR FIADOR POR CPF: %s";
            final String MSG_INICIO = String.format("######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: excluir por cpf do fiador ==> DADOS USUÁRIO LOGADO: %s", new Gson().toJson(usuarioExterno));
            log.log(Level.SEVERE, MSG_INICIO);

            if (!validaDadosPertenceUsuarioLogado(usuarioExterno.getCodFies(), null)) {
                final String MSG_ERRO_401 = String.format("######## RETORNANDO CÓDIGO 401 ######  Método ==> excluir fiador - CPF: %s", usuarioExterno.getCpf());
                log.log(Level.SEVERE,MSG_ERRO_401);
                return preparaRetornoAPI(retorno401, String.format(NOME_METHOD, usuarioExterno.getCpf()));
            }

            Retorno retorno = contratoServ.excluirFiador(usuarioExterno, cpf);
            mensagemSucesso = retorno.getMensagem();

            if (retorno.getCodigo() == 404L) {
                retorno.setCodigo(0L);
                retorno.setTipo(SUCESSO);
                return preparaRetornoAPI(retorno, String.format(NOME_METHOD, usuarioExterno.getCpf()));
            }

            if(retorno.getCodigo() == 500L) {
                retorno.setTipo(ERRO);
                retorno.setCodigo(-1L);
                return preparaRetornoAPI(retorno,  String.format(NOME_METHOD, usuarioExterno.getCpf()));
            }

            return preparaRetornoAPI(retorno,  String.format(NOME_METHOD, usuarioExterno.getCpf()));
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/listarSituacaoComplementar")
    @ApiOperation(value = "Este serviço retorna a Situação do cadastro complementar.", notes = "O campo de entrada é o código Fies do estudante que é obtido através do token.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401, response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412, response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class) })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response listarSituacaoCadastroComplementarCoParticipante() {
        try {
            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.retornarCodFiesPorCPF(cpf);
            String nomeMetodoLog = "######## LISTAR SITUAÇÃO CADASTRO COMPLEMENTAR - CPF CANDIDATO: ";
            log.log(Level.SEVERE,
                    "######## ANALISANDO_LOG_APP - INÍCIO DO MÉTODO: listar situção complementar ==> DADOS USUÁRIO LOGADO: "
                            + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE,
                        "######## RETORNANDO CÓDIGO 401 ######  Método ==> listar situação complementar - CPF: "
                                + nomeMetodoLog + cpf);
                return Response.status(400).entity(retorno400).build();
            }
            DadosComplementaresCoParticipanteTO situacaoComplementar = null;
            situacaoComplementar= contratoServ.listarSituacaoCadastroComplementar(codFies);

            if (situacaoComplementar ==null) {
                return Response.status(400).entity(retorno400D).build();
            }

            log.log(Level.SEVERE, "######## RETORNANDO CÓDIGO 200 ######  Método ==> listar situação complementar: "
                    + nomeMetodoLog + cpf);
            return Response.ok().entity(situacaoComplementar).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API.concat("listarSituacaoCadastroComplementarCoParticipante"));
            if(e instanceof BusinessException) {
                Retorno retorno = new Retorno();
                retorno.setCodigo(-1L);
                retorno.setTipo(ERRO);
                retorno.setEditavel(false);
                retorno.setMensagem(e.getLocalizedMessage());
                return Response.status(200).entity(retorno).build();
            } else {
                return trataExceptionAPI(e);
            }
        }
    }


    //	Prepara as respostas para as requisições do APP
    public Response preparaRetornoAPI(Retorno retorno, String metodo) {
        Gson gson = new Gson();
        try {
            log.log(Level.SEVERE,"######## ENTREI NO preparaRetorno ######  Método ==> "+ metodo);

            retorno.setEditavel(false);

            if(retorno.getCodigo()==null ) {
                retorno.setCodigo(-1L);
                log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 200 com MSG DE NEGOCIO TIPO-404 ###### (preparaRetorno) Método ==> "+ metodo);
                log.log(Level.SEVERE,CODIGO + retorno.getCodigo() +  TIPO + retorno.getTipo() + MSG + retorno.getMensagem());
                return Response.ok().entity(retorno404).build();
            }
            if(retorno.getCodigo()==400L ) {
                retorno.setCodigo(400L);
                log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 400 com MSG DE NEGOCIO TIPO-400 ###### (preparaRetorno) Método ==> "+ metodo);
                log.log(Level.SEVERE,CODIGO + retorno.getCodigo() +  TIPO + retorno.getTipo() + MSG + retorno.getMensagem());
                return Response.status(400).entity(retorno).build();
            }

            if(retorno.getCodigo()== 404L ) {
                log.log(Level.SEVERE, ANALISANDO_LOG_APP_SAIDA + metodo + " ==> OBJETO MSG RETORNADO: " + gson.toJson(retorno));
                log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO CÓDIGO 200 com MSG DE NEGOCIO TIPO-404 COM MENSAGEM NEGOCIAL ###### (preparaRetorno) Método ==> "+ metodo);
                retorno.setTipo(ERRO);
                log.log(Level.SEVERE,CODIGO + retorno.getCodigo() + TIPO + retorno.getTipo() + MSG + retorno.getMensagem());
                return Response.ok().entity(retorno).build();
            }

            if(retorno.getCodigo() == 0L){
                log.log(Level.SEVERE, ANALISANDO_LOG_APP_SAIDA + metodo + " ==> OBJETO RETORNADO: " + gson.toJson(retorno));
                log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 200 ###### (preparaRetorno) Método ==> "+ metodo);
                retorno.setMensagem(mensagemSucesso);
                retorno.setTipo(SUCESSO);
                log.log(Level.SEVERE,CODIGO + retorno.getCodigo() + TIPO + retorno.getTipo() + MSG + retorno.getMensagem());
                return Response.ok().entity(retorno).build();
            }

            log.info("######## RETORNANDO CÓDIGO 200 COM ERRO NEGOCIAL ###### (preparaRetorno) Método ==> "+ metodo);
            retorno.setCodigo(-1L);
            retorno.setTipo(ERRO);
            if (retorno.getMensagem() == null) {
                retorno.setMensagem(ERRO_BACKEND);
            }
            log.log(Level.SEVERE,CODIGO + retorno.getCodigo() + TIPO + retorno.getTipo() + MSG + retorno.getMensagem());
            return Response.ok().entity(retorno).build();

        } catch (Exception e) {
            log.log(Level.SEVERE,ERRO_API + "preparaRetornoAPI");
            return trataExceptionAPI(e);
        }
    }

    //	Monta o Retorno das Exceptions da API
    public Response trataExceptionAPI(Exception e) {

        e.printStackTrace();

        Retorno retornoException = new Retorno();
        retornoException.setMensagem((e.getMessage() == null || e.getMessage().isEmpty()) ? ERRO_BACKEND : e.getMessage());
        retornoException.setCodigo(-1L);
        retornoException.setTipo(ERRO);

        if(e instanceof BusinessException || e instanceof FESException) {
            return Response.ok().entity(retornoException).build();
        } else {
            log.log(Level.SEVERE,"######## RETORNANDO CÓDIGO 500 ########");
            return Response.serverError().entity(retornoException).build();
        }
    }

    @GET
    @Path("/v1/obterDataHora")
    @ApiOperation(value = "Esse endpoint retorna os dados referente ao servidor",
            notes = "Sem parâmetros de entrada" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = DataServidorResponse.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response dadosDoServidor() {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogadoApp());
            String cpf = usuarioExterno.getCpf();
            Long codFies = contratoServ.getCodFiesPorCPFValidacaoContratoEstudante(cpf);

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + DADOS_DO_SERVIDOR_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            if (!validaDadosPertenceUsuarioLogado(codFies, null)) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DADOS_DO_SERVIDOR_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

            Retorno retorno = servidorService.pegarDadosServidor();

            if(retorno ==  null) {
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DADOS_DO_SERVIDOR_LOG + CPF_LOG + cpf);
                return Response.status(404).entity(retorno404).build();
            }

            if(retorno.getCodigo() == -1L) {
                retorno.setTipo(ERRO);
                return Response.status(412).entity(retorno412).build();
            }

            return Response.ok().entity(retorno).build();

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + DADOS_DO_SERVIDOR_LOG + " ==> " + e.getMessage());
            return Response.status(500).entity(retorno500).build();
        }
    }

    @GET
    @Path("/v1/obterResumoContratoEstudante")
    @ApiOperation(value = "Este serviço retorna o resumo do contrato estudante",
            notes = "" )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response obterResumoContratoEstudante() {
        try {

            Gson gson = new Gson();
            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());

            log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + OBTER_RESUMO_CONTRATO_ESTUDANTE_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

            final String CPF = usuarioExterno.getCpf();
            final String PERFIL = usuarioExterno.getRoleFES();

            Retorno retorno = contratoServ.obterResumoContratoEstudante(CPF, PERFIL);

            if (retorno.getCodigo() == 1L) {
                return Response.status(200).entity(retorno).build();
            } else {
                mensagemSucesso = retorno.getMensagem();
                return preparaRetornoAPI(retorno, OBTER_RESUMO_CONTRATO_ESTUDANTE_LOG + CPF_LOG + CPF);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + OBTER_RESUMO_CONTRATO_ESTUDANTE_LOG);
            return trataExceptionAPI(e);
        }
    }

    @GET
    @Path("/v1/detalharContratoExterno")
    @ApiOperation(value = "Este serviço deve permitir uma consulta aos dados do contrato do estudante.",
            notes = "O campo de entrada é o CPF obtido através do token de login.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpResponseMessage.CODE_200, response = Retorno.class),
            @ApiResponse(code = 401, message = HttpResponseMessage.CODE_401 ,response = Retorno.class),
            @ApiResponse(code = 404, message = HttpResponseMessage.CODE_404),
            @ApiResponse(code = 412, message = HttpResponseMessage.CODE_412 ,response = Retorno.class),
            @ApiResponse(code = 500, message = HttpResponseMessage.CODE_500, response = Retorno.class)
    })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response detalharContratoExterno() {
        try {
            Gson gson = new Gson();

            UsuarioExterno usuarioExterno = getUsuarioExterno(getUsuarioLogado());
            String cpf = usuarioExterno.getCpf();
            //FES_REPRESENTANTE_LEGAL  FES_CONJUGE FES_FIADOR
            if(usuarioExterno.getRoles() != null && usuarioExterno.getRoles().contains(Constantes.FES_FIADOR) ||
                    usuarioExterno.getRoles().contains(Constantes.FES_CONJUGE) ||
                    usuarioExterno.getRoles().contains(Constantes.FES_REPRESENTANTE_LEGAL)) {

                DetalharContratoExterno retorno = contratoServ.getCodFiesPorCPFComplementarEstudante(cpf);

                log.log(Level.SEVERE, ANALISANDO_LOG_APP_INICIO + DETALHAR_CONTRATO_EXTERNO_LOG + DADOS_USUARIO_LOGADO + gson.toJson(usuarioExterno));

                if(retorno != null) {
                    if(retorno.getCpfEstudante() == null ) {
                        return Response.status(404).entity(retorno404).build();
                    } else {
                        log.log(Level.INFO, "ANALISANDO_LOG_APP - RETORNO METODO: " + DETALHAR_CONTRATO_EXTERNO_LOG + " - RESPONSE 200 - CPF_ENTRADA: " + cpf + " OBJETO DE RETORNO: " + gson.toJson(retorno));
                        mensagemSucesso = CONSULTA_REALIZADA_COM_SUCESSO;

                        retorno.setCodigo(0L);
                        retorno.setMensagem(mensagemSucesso);
                        return preparaRetornoAPI(retorno, DETALHAR_CONTRATO_EXTERNO_LOG + CPF_LOG + cpf);
                    }
                } else {
                    return Response.status(412).entity(retorno412).build();
                }


            }else{
                log.log(Level.SEVERE, RETORNANDO_CODIGO_401 + DETALHAR_CONTRATO_EXTERNO_LOG + CPF_LOG + cpf);
                return Response.status(401).entity(retorno401).build();
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, ERRO_API + DETALHAR_CONTRATO_EXTERNO_LOG);
            return trataExceptionAPI(e);
        }
    }
}
