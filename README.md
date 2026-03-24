	@GET
	@Path("/consultaFiadoresPorCpfCodFiesAgencia")
	@Consumes({ MediaType.APPLICATION_JSON })
	public List<Fiador> consultaFiadoresPorCpfCodFiesAgencia(@QueryParam("codigoFies") Long codigoFies,
						   @QueryParam("cpf") String cpf,
							 @QueryParam("agencia") int agencia) throws BusinessException {

		String usuario = getUsuarioLogado();
		Contrato contrato = contratoServ.consulta(usuario, cpf, codigoFies, agencia);
		return serv.consultarFiadores(usuario, contrato.getCodigo());
	}
