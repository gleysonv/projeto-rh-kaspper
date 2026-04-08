verify(ocorrenciaRejeicaoBean, times(1)).gravarOcorrenciaRejeicao(
    eq(f.getCodigoFies()),
    eq(TipoOperacaoAditamento.ADITAMENTO_NAO_SIMPLIFICADO),
    eq(SituacaoOcorrenciaRejeicao.ENVIADO_BANCO),
    eq("SIAPI #ABC-Mensagem de erro"),
    Matchers.<Integer>isNull(),
    Matchers.<Integer>isNull());
