package br.gov.caixa.fes.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import br.gov.caixa.arqrefcore.validacao.Validation;
import br.gov.caixa.fes.dominio.Fiador;
import br.gov.caixa.fes.dominio.Retorno;
import br.gov.caixa.fes.dominio.renegociacaocontrato.vo.SolicitacaoRenegociacaoContratoVO;
import br.gov.caixa.fes.negocio.FiadorService;

@RequestScoped
@Path("/fiador")
public class FiadorRest extends AbstractSecurityRest {

	@Inject
	private FiadorService serv;

	@GET
	@Path("/consultaFiadores")
	@Consumes({ MediaType.APPLICATION_JSON })
	public List<Fiador> consultaFiadores(@QueryParam("codigoFies") Long codigoFies) {

		String usuario = getUsuarioLogado();

		return serv.consultarFiadores(usuario, codigoFies);
	}

	@GET
	@Path("/consulta")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Fiador consulta(@QueryParam("codigoFies") Long codigoFies, String cpf) {

		String usuario = getUsuarioLogado();

		return serv.consultar(usuario, codigoFies, cpf);
	}

	@POST
	@Path("/salva")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Validation
	public Retorno salva(Fiador fiador) {
		String usuario = getUsuarioLogado();

		return serv.salvar(usuario, fiador);
	}

	@POST
	@Path("/excluir")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Validation
	public Retorno excluir(Fiador fiador) {
		String usuario = getUsuarioLogado();

		return serv.excluir(usuario, fiador);
	}

	@POST
	@Path("/validarRenegociacao")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Validation
	public Retorno validar(SolicitacaoRenegociacaoContratoVO renegociacao) {
		String usuario = getUsuarioLogado();

		Fiador fiador = new Fiador();
		fiador.setCpf(renegociacao.getDadosEstudante().getCpf());
		fiador.setCodigoFies(Long.valueOf(renegociacao.getDadosEstudante().getCodFies()));

		return serv.validarRenegociacao(usuario, fiador, renegociacao.getCodigo());
	}
}
