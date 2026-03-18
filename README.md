excluirFiador: function (fiadorRow, $tr) {
    removerMensagens();

    if (!confirm('Confirma a exclusão do fiador selecionado?')) {
        return;
    }

    var model = new Fiador();

    var cpf = fiadorRow.cpfFiador || fiadorRow.coCpfFiador || fiadorRow.cpf || "";
    var codigoFies = this.model.get("codigoFiesConsulta") || fiadorRow.codigoFies || 0;
    var dependenteCPF = fiadorRow.dependenteCPF || fiadorRow.dependenteCpf || 0;
    var codigo = fiadorRow.codigo || fiadorRow.codigoFiador || 0;

    model.set("cpf", cpf);
    model.set("codigoFies", codigoFies);
    model.set("dependenteCPF", dependenteCPF);
    model.set("codigo", codigo);

    $('#ajaxStatus').modal('show');

    model.excluir()
        .done(function (retorno) {
            $('#ajaxStatus').modal('hide');

            if (retorno && retorno.mensagem) {
                mostrarErrors([{ message: retorno.mensagem }]);
                return;
            }

            $tr.remove();

            if ($('#tbResultadoFiadores tbody tr').length === 0) {
                $('#tbResultadoFiadores tbody').append('<tr><td colspan="5">Nenhum fiador encontrado.</td></tr>');
            }
        })
        .fail(function (xhr) {
            $('#ajaxStatus').modal('hide');
            console.error('[excluirFiador] FAIL', xhr);
            mostrarErrors([{ message: 'Falha ao excluir o fiador.' }]);
        });
},

No fiador. Js
excluir: function () {
    console.log("call -> Fiador -> excluir");

    this.url = '../fes-web/emprest/fiador/excluir';

    return this.save(null, {
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(this.toJSON())
    });
},


No fiador. Js
excluir: function () {
    console.log("call -> Fiador -> excluir");

    this.url = '../fes-web/emprest/fiador/excluir';

    return this.save(null, {
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(this.toJSON())
    });
},
