   // ================== NOVOS TESTES DE COBERTURA DA COMUNICAÇÃO SIAPI ==================

    @Test
    public void salvar_quandoInclusao_deveComunicarSiapi() throws Exception {
        Fiador f = buildFiadorValido();

        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");

        Padrao retornoApi = mock(Padrao.class);
        when(retornoApi.getCodigoRetorno()).thenReturn("0");
        when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

        Retorno r = bean.salvar(USUARIO, f);

        assertNotNull(r);
        assertEquals(Long.valueOf(0), r.getCodigo());
        verify(contratoBean, times(1)).exportaOnline120(f.getCodigoFies());
        verify(api, times(1)).executeAPIPB699Codigo(any(Padrao.class));
        verify(ocorrenciaRejeicaoBean, times(1)).gravarOcorrenciaRejeicao(
                eq(f.getCodigoFies()),
                eq(TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO),
                eq(SituacaoOcorrenciaRejeicao.ENVIADO_BANCO),
                eq("SIAPI #ABC-Mensagem de erro"),
                1,
                2026);
    }

    @Test
    public void excluirRenegociacao_quandoSucesso_deveComunicarSiapi() throws BusinessException {
        Fiador f = buildFiadorValido();

        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");

        Padrao retornoApi = mock(Padrao.class);
        when(retornoApi.getCodigoRetorno()).thenReturn("0");
        when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

        Retorno r = bean.excluirRenegociacao(USUARIO, f, 10L);

        assertNotNull(r);
        assertEquals(Long.valueOf(0), r.getCodigo());
        verify(contratoBean, times(1)).exportaOnline120(f.getCodigoFies());
        verify(api, times(1)).executeAPIPB699Codigo(any(Padrao.class));
    }

    @Test
    public void validarRenegociacao_quandoSucesso_naoDeveComunicarSiapi() throws BusinessException {
        Fiador f = buildFiadorValido();

        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        Retorno r = bean.validarRenegociacao(USUARIO, f, 10L);

        assertNotNull(r);
        assertEquals(Long.valueOf(0), r.getCodigo());
        verify(contratoBean, never()).exportaOnline120(anyLong());
        verify(api, never()).executeAPIPB699Codigo(any(Padrao.class));
        verify(ocorrenciaRejeicaoBean, never()).gravarOcorrenciaRejeicao(
                eq(f.getCodigoFies()),
                eq(TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO),
                eq(SituacaoOcorrenciaRejeicao.ENVIADO_BANCO),
                eq("SIAPI #ABC-Mensagem de erro"),
                1,
                2026);
    }

    @Test
    public void salvar_quandoInclusaoESiapiRetornarErro_deveGravarOcorrencia() throws BusinessException {
        Fiador f = buildFiadorValido();

        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(contratoBean.exportaOnline120(anyLong())).thenReturn("W120");

        Padrao retornoApi = mock(Padrao.class);
        when(retornoApi.getCodigoRetorno()).thenReturn("5");
        when(retornoApi.getCodigoErroTransacaoSiapi()).thenReturn("ABC");
        when(retornoApi.getMensagem()).thenReturn("Mensagem de erro");
        when(api.executeAPIPB699Codigo(any(Padrao.class))).thenReturn(retornoApi);

        Retorno r = bean.salvar(USUARIO, f);

        assertNotNull(r);
        assertEquals(Long.valueOf(0), r.getCodigo());

        verify(ocorrenciaRejeicaoBean, times(1)).gravarOcorrenciaRejeicao(
                eq(f.getCodigoFies()),
                eq(TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO),
                eq(SituacaoOcorrenciaRejeicao.ENVIADO_BANCO),
                eq("SIAPI #ABC-Mensagem de erro"),
                1,
                2026);
    }

    @Test
    public void salvar_quandoInclusaoEExportaOnline120Vazio_naoDeveChamarApi() throws BusinessException {
        Fiador f = buildFiadorValido();

        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
        when(contratoBean.exportaOnline120(anyLong())).thenReturn(EMPTY);

        Retorno r = bean.salvar(USUARIO, f);

        assertNotNull(r);
        assertEquals(Long.valueOf(0), r.getCodigo());
        verify(contratoBean, times(1)).exportaOnline120(f.getCodigoFies());
        verify(api, never()).executeAPIPB699Codigo(any(Padrao.class));
        verify(ocorrenciaRejeicaoBean, never()).gravarOcorrenciaRejeicao(
                eq(f.getCodigoFies()),
                eq(TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO),
                eq(SituacaoOcorrenciaRejeicao.ENVIADO_BANCO),
                eq("SIAPI #ABC-Mensagem de erro"),
                1,
                2026);
    }
