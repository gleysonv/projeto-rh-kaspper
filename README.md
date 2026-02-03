novoFiador: function (e) {
  if (e) e.preventDefault();
  removerMensagens();

  var that = this;

  $.when(
    $.getScript(BASE_FIADOR + '/modelo/Fiador.js'),
    $.getScript(BASE_FIADOR + '/controle/FiadorControle.js'),
    $.getScript(BASE_FIADOR + '/controle/FiadorEnderecoListControle.js'),
    $.getScript(BASE_FIADOR + '/controle/FiadorContatoListControle.js'),
    $.getScript(BASE_FIADOR + '/controle/FiadorConjugeListControle.js')
  ).done(function () {

    var modelFiador = new Fiador();

    $('#container').html(
      new FiadorControle({
        model: modelFiador,
        modo: 'incluir',      // opcional
        origem: 'consulta'   // opcional
      }).el
    );

  }).fail(function () {
    alert('Erro ao carregar a tela de cadastro do fiador.');
  });
},
