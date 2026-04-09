@Test
public void salvarRenegociacao_quandoSucesso_retornaSucesso() {
    Fiador f = buildFiadorValido();
    when(query.setParameter(anyString(), anyObject())).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");
    Padrao retornoApi = mock(Padrao.class);
    when(retornoApi.getCodigoRetorno()).thenReturn("0");
    when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

    Retorno r = bean.salvarRenegociacao(USUARIO, f, 10L);
    assertEquals(Long.valueOf(0), r.getCodigo());
    assertEquals(MSG_COMANDO_SUCESSO, r.getMensagem());
}


@Test
public void excluirRenegociacao_quandoSucesso_retornaSucesso() {
    Fiador f = buildFiadorValido();
    when(query.setParameter(anyString(), anyObject())).thenReturn(query);
    when(query.executeUpdate()).thenReturn(1);

    when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");
    Padrao retornoApi = mock(Padrao.class);
    when(retornoApi.getCodigoRetorno()).thenReturn("0");
    when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

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

    when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");
    Padrao retornoApi = mock(Padrao.class);
    when(retornoApi.getCodigoRetorno()).thenReturn("0");
    when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

    Retorno r = bean.salvar(USUARIO, f);
    assertEquals(Long.valueOf(1), r.getCodigo());
    assertEquals("valor duplicado", r.getMensagem());
}

@Test
public void salvar_quandoExceptionGenerica_retornaMensagemPadrao() {
    Fiador f = buildFiadorValido();
    when(query.setParameter(anyString(), anyObject())).thenReturn(query);
    when(query.executeUpdate()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE_X));

    when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");
    Padrao retornoApi = mock(Padrao.class);
    when(retornoApi.getCodigoRetorno()).thenReturn("0");
    when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

    Retorno r = bean.salvar(USUARIO, f);
    assertEquals(Long.valueOf(1), r.getCodigo());
    assertEquals("Ocorreu um erro na realização do comando, tente novamente!", r.getMensagem());
}
