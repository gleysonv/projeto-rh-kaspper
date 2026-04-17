@Test
public void salvar_quandoJaExistiremDoisFiadores_retornaErroLimiteFiadores() throws FESException {
    Fiador fiador = buildFiadorValido();

    List<Fiador> fiadoresCadastrados = Arrays.asList(new Fiador(), new Fiador());

    doReturn(fiadoresCadastrados).when(bean).consultarFiadores(USUARIO, fiador.getCodigoFies());

    Retorno retorno = bean.salvar(USUARIO, fiador);

    assertNotNull(retorno);
    assertEquals(Long.valueOf(-1L), retorno.getCodigo());
    assertEquals("Não é permitido cadastrar mais de 2 fiadores.", retorno.getMensagem());

    verify(bean, never()).consultarCpfNoSICPF(anyString());
}


@Test
public void salvar_quandoCpfComRestricao_retornaErroCpfRestrito() throws FESException {
    Fiador fiador = buildFiadorValido();

    DadosCpfRetorno dadosCpfRetorno = mock(DadosCpfRetorno.class);
    when(dadosCpfRetorno.getSituacaoCPF()).thenReturn(0);

    doReturn(Collections.singletonList(new Fiador())).when(bean).consultarFiadores(USUARIO, fiador.getCodigoFies());
    doReturn(dadosCpfRetorno).when(bean).consultarCpfNoSICPF(fiador.getCpf());

    Retorno retorno = bean.salvar(USUARIO, fiador);

    assertNotNull(retorno);
    assertEquals(Long.valueOf(-1L), retorno.getCodigo());
    assertEquals("CPF informado com restrição. Favor regularizar o CPF para continuar a contratação!", retorno.getMensagem());
}


@Test
public void salvar_quandoCpfRegular_deveDelegarParaSalvarInclusao() throws FESException {
    Fiador fiador = buildFiadorValido();

    DadosCpfRetorno dadosCpfRetorno = mock(DadosCpfRetorno.class);
    when(dadosCpfRetorno.getSituacaoCPF()).thenReturn(1);

    Retorno retornoEsperado = new Retorno();
    retornoEsperado.setCodigo(0L);
    retornoEsperado.setMensagem(MSG_COMANDO_SUCESSO);

    doReturn(Collections.singletonList(new Fiador())).when(bean).consultarFiadores(USUARIO, fiador.getCodigoFies());
    doReturn(dadosCpfRetorno).when(bean).consultarCpfNoSICPF(fiador.getCpf());
    doReturn(retornoEsperado).when(bean).salvar("I", USUARIO, fiador);

    Retorno retorno = bean.salvar(USUARIO, fiador);

    assertNotNull(retorno);
    assertEquals(Long.valueOf(0L), retorno.getCodigo());
    assertEquals(MSG_COMANDO_SUCESSO, retorno.getMensagem());

    verify(bean, times(1)).salvar("I", USUARIO, fiador);
}
