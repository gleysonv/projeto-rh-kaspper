@Test
public void salvar_quandoCpfVazio_retornaMensagem() throws FESException {
    Fiador f = buildFiadorValido();
    f.setNome("NOME");
    f.setCpf(EMPTY);

    mockConsultarFiadoresSemBloqueio(f);
    doReturn(mockDadosCpfRetorno(1)).when(bean).consultarCpfNoSICPF(anyString());

    Retorno r = bean.salvar(USUARIO, f);
    assertEquals(Long.valueOf(1), r.getCodigo());
    assertEquals("CPF não foi informado!", r.getMensagem());
}
