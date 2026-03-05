alterarFiador: function (fiadorRow) {
    var that = this;

    // 1) cria o model vazio
    var model = new Fiador();

    // 2) pega cpf da grid (você já faz isso)
    var cpf = (fiadorRow.cpfFiador || fiadorRow.coCpfFiador || fiadorRow.cpf || '');
    cpf = (cpf || '').toString().replace(/\D/g, ''); // <<< limpa máscara

    // 3) pega codigoFies (prefiro do model principal, como você já faz)
    var codigoFies = this.model.get('codigoFiesConsulta') || this.model.get('codigoFies') || '';
    codigoFies = (codigoFies || '').toString().replace(/\D/g, ''); // <<< remove pontos etc.

    if (!cpf || !codigoFies) {
        mostrarErrors([{ message: 'Não foi possível alterar: CPF ou Código FIES não informado.' }], '#msgModal');
        return;
    }

    // opcional: seta já no model
    model.set('cpf', cpf);
    model.set('codigoFies', codigoFies);

    // 4) carrega o fiador completo antes de abrir a tela
    try { $('#ajaxStatus').modal('show'); } catch (e) {}

    $.ajax({
        url: '../fes-web/emprest/fiador/consulta',
        type: 'GET',
        dataType: 'json',
        data: {
            codigoFies: codigoFies,
            cpf: cpf
        }
    }).done(function (data) {
        try { $('#ajaxStatus').modal('hide'); } catch (e) {}

        if (!data) {
            mostrarErrors([{ message: 'Consulta não retornou dados do fiador.' }], '#msgModal');
            return;
        }

        // garante que codigoFies/cpf estejam consistentes no model
        data.codigoFies = codigoFies;
        data.cpf = cpf;

        // popula o model com o retorno COMPLETO (identidade, endereco, contato, etc.)
        model.set(data);

        // 5) agora sim abre a tela de alteração com tudo carregado
        that.abrirTelaCadastroFiador(model, 'alterar');

    }).fail(function (xhr) {
        try { $('#ajaxStatus').modal('hide'); } catch (e) {}

        var msg = 'Erro ao consultar fiador completo para alteração.';
        try {
            if (xhr && xhr.responseText) msg += ' ' + xhr.responseText;
        } catch (e) {}

        mostrarErrors([{ message: msg }], '#msgModal');
    });
},
