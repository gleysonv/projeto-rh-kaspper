@RequestScoped
@Path("/fiador")
public class FiadorRest extends AbstractSecurityRest {

    @Inject
    private FiadorService serv;

    @GET
    @Path("/consultaFiadores")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Fiador> consultaFiadores(@QueryParam("codigoFies") Long codigoFies) {
        String usuario = getUsuarioLogado();
        return serv.consultarFiadores(usuario, codigoFies);
    }

    @GET
    @Path("/consulta")
    @Produces(MediaType.APPLICATION_JSON)
    public Fiador consulta(@QueryParam("codigoFies") Long codigoFies,
                           @QueryParam("cpf") String cpf) {
        String usuario = getUsuarioLogado();
        return serv.consultar(usuario, codigoFies, cpf);
    }

    @POST
    @Path("/salva")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Validation
    public Retorno salva(Fiador fiador) {
        String usuario = getUsuarioLogado();
        return serv.salvar(usuario, fiador);
    }

    @POST
    @Path("/excluir")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Validation
    public Retorno excluir(Fiador fiador) {
        String usuario = getUsuarioLogado();
        return serv.excluir(usuario, fiador);
    }

    @POST
    @Path("/validarRenegociacao")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Validation
    public Retorno validar(SolicitacaoRenegociacaoContratoVO renegociacao) {
        String usuario = getUsuarioLogado();

        Fiador fiador = new Fiador();
        fiador.setCpf(renegociacao.getDadosEstudante().getCpf());
        fiador.setCodigoFies(Long.valueOf(renegociacao.getDadosEstudante().getCodFies()));

        return serv.validarRenegociacao(usuario, fiador, renegociacao.getCodigo());
    }
}
