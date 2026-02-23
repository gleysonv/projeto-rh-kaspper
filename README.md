consultar: function () {
  var that = this;

  $('#divResultado').hide('slow');
  removerMensagens();

  this.model.set('cpf', purificaAtributo(this.model.get('cpf')));

  if (!this.validar()) return;

  $('#ajaxStatus').modal('show');

  this.model.buscar()
    .done(function (data) {
      $('#ajaxStatus').modal('hide');

      console.log('[listarFiadores] retorno =', data);

      // se o backend devolve "mensagem" quando dá erro de regra
      if (data && data.mensagem) {
        return mostrarErrors([{ message: data.mensagem }]);
      }

      // tenta achar a lista no payload (robusto)
      var lista =
        (data && data.listaFiadores) ||
        (data && data.fiadores) ||
        (data && data.listaRetorno) ||
        (data && data.retorno && data.retorno.listaRetorno) ||
        (data && data.objeto && data.objeto.listaRetorno) ||
        [];

      that.renderTabelaFiadores(lista);
      $('#divResultado').show('slow');
    })
    .fail(function (xhr) {
      $('#ajaxStatus').modal('hide');
      console.error('[listarFiadores] FAIL', xhr && xhr.status, xhr && xhr.responseText);

      mostrarErrors([{
        message: 'Falha ao consultar fiadores. Verifique Network > Response (endpoint /v1/listarFiadores).'
      }]);
    });
},
