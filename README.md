DECLARE
    v_cursor SYS_REFCURSOR;
BEGIN
    FES.FESSP943_CONTRATO_CONSULTA(
        v_cursor,
        4,
        'SEU_USUARIO',
        '',
        123456,
        0
    );

    -- Apenas abre o cursor
    -- Depois clique com botão direito > Fetch All
END;
/
