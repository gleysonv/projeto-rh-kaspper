// === AJUSTE AQUI ===
// Se sua aplicação já tem um helper de URL, use ele.
// Caso contrário, deixe '' se o REST estiver no mesmo contexto.
var BASE_URL = ''; // ex: '/fes' ou '/api' ou ''

(function ($) {

  function getCodigoFiesDigitado() {
    return $.trim($('#codigoFiesConsulta').val() || '');
  }

  function getCpfDigitado() {
    return $.trim($('#cpf').val() || '');
  }

  function limparResultado() {
    $('#divResultado').hide();
    $('#divTabelaFiadores').hide();
    $('#divAcoesPosConsulta').hide();
    $('#tbResultadoFiadores tbody').empty();

    $('#codigoFies').text('');
    $('#cpfEstudante').text('');
    $('#contratoSIAPI').text('');
    $('#nomeEstudante').text('');
    $('#cursoTurno').text('');
  }

  // Preenche o “Resultado” do estudante (mínimo pra tela fazer sentido)
  // Se você tiver um REST específico do contrato/estudante, substitua aqui.
  function preencherCabecalhoMinimo(codigoFies, cpf) {
    $('#codigoFies').text(codigoFies);
    $('#cpfEstudante').text(cpf || '-');

    // placeholders (troque quando tiver endpoint real)
    $('#contratoSIAPI').text('-');
    $('#nomeEstudante').text('-');
    $('#cursoTurno').text('-');

    $('#divResultado').show();
  }

  function renderFiadores(fiadores) {
    var $tbody = $('#tbResultadoFiadores tbody');
    $tbody.empty();

    if (!fiadores || !fiadores.length) {
      $tbody.append(
        '<tr><td colspan="5" style="text-align:center;">Nenhum fiador encontrado.</td></tr>'
      );
      return;
    }

    fiadores.forEach(function (f, idx) {
      // Ajuste os nomes conforme o JSON real do seu backend
      var contrato = f.numeroContrato || f.contrato || f.nrContrato || '';
      var cpfFiador = f.cpf || f.cpfFiador || '';
      var nome = f.nome || f.nomeFiador || '';
      var dtNasc = f.dataNascimento || f.dtNascimento || '';

      var btnAlterar = '<a href="#" class="btn btn-mini btn-primary btnAlterarFiador" data-index="' + idx + '">Alterar</a>';

      var tr = ''
        + '<tr>'
        +   '<td>' + contrato + '</td>'
        +   '<td>' + cpfFiador + '</td>'
        +   '<td>' + nome + '</td>'
        +   '<td>' + dtNasc + '</td>'
        +   '<td>' + btnAlterar + '</td>'
        + '</tr>';

      $tbody.append(tr);
    });

    $('#divTabelaFiadores').show();
    $('#divAcoesPosConsulta').show();
  }

  function consultarFiadoresPorCodigoFies(codigoFies) {
    // Endpoint conforme seu exemplo: @Path("/fiador") + @Path("/consultaFiadores")
    var url = BASE_URL + '/fiador/consultaFiadores';

    return $.ajax({
      url: url,
      method: 'GET',
      dataType: 'json',
      data: { codigoFies: codigoFies }
    });
  }

  function acaoConsultar() {
    var codigoFies = getCodigoFiesDigitado();
    var cpf = getCpfDigitado();

    if (!codigoFies) {
      alert('Informe o C\u00F3digo Fies.');
      $('#codigoFiesConsulta').focus();
      return;
    }

    preencherCabecalhoMinimo(codigoFies, cpf);

    // Carrega fiadores
    consultarFiadoresPorCodigoFies(codigoFies)
      .done(function (data) {
        renderFiadores(data);
      })
      .fail(function (xhr) {
        console.error('Erro ao consultar fiadores', xhr);
        alert('N\u00E3o foi poss\u00EDvel consultar os fiadores. Verifique o endpoint e o console.');
      });
  }

  $(document).ready(function () {

    // Garantia de bind (o seu problema atual costuma ser aqui)
    $(document).off('click', '#btnLocalizarCodFies');
    $(document).on('click', '#btnLocalizarCodFies', function (e) {
      e.preventDefault();
      acaoConsultar();
    });

    $(document).off('click', '#btnConsultar');
    $(document).on('click', '#btnConsultar', function (e) {
      e.preventDefault();
      acaoConsultar();
    });

    $(document).off('click', '#btnLimpar');
    $(document).on('click', '#btnLimpar', function (e) {
      e.preventDefault();
      $('#cpf').val('');
      $('#codigoFiesConsulta').val('');
      $('#agenciaConsulta').val('');
      limparResultado();
    });

    // Exemplo de ação no botão Alterar (só pra não ficar morto)
    $(document).off('click', '.btnAlterarFiador');
    $(document).on('click', '.btnAlterarFiador', function (e) {
      e.preventDefault();
      alert('Clique em Alterar detectado (implementar abertura da tela/modal de altera\u00E7\u00E3o aqui).');
    });

    // Estado inicial
    limparResultado();
  });

})(jQuery);
