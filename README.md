abrirTelaCadastroFiador: function (modelFiador, modo) {
    removerMensagens();

    var modelConsultaAnterior = this.model;

    $.when(
        $.getScript(BASE_FIADOR + '/modelo/Fiador.js'),
        $.getScript(BASE_FIADOR + '/controle/FiadorModalControle.js'),
        $.getScript(BASE_FIADOR + '/controle/FiadorControle.js'),
        $.getScript(BASE_FIADOR + '/controle/FiadorConjugeControle.js'),
        $.getScript(BASE_FIADOR + '/controle/FiadorEnderecoControle.js'),
        $.getScript(BASE_FIADOR + '/controle/FiadorContatoControle.js')
    )
    .done(function () {
        $('#container').html(
            new FiadorModalControle({
                el: $('#container'),
                model: modelFiador,
                modo: modo,
                modelAnterior: modelConsultaAnterior
            }).el
        );
    })
    .fail(function (xhr, status, err) {
        console.log("[abrirTelaCadastroFiador] FAIL", status, err);
        alert('Erro ao carregar a tela de cadastro do fiador.');
    });
},
