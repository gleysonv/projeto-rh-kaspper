//# sourceURL=ConsultaFiadorControle.js

var BASE_FIADOR = '../fes-web/servicos/contratofies/manutencaofiador';

window.ConsultaFiadorControle = Backbone.View.extend({

  urlTemplate: BASE_FIADOR + '/visao/ConsultaBaseFiador.html',

  events: {
    "click a#btnLimpar": "limpar",
    "click a#btnConsultar": "consultar",
    "click a#btnVoltar": "voltar",
    "click a#btnLocalizarCodFies": "localizarCodFies",
    "click a#btnNovoFiador": "novoFiador",
    "click a#btnSair": "sair",
    "blur #formFiltroConsulta input": "change",
    "change #formFiltroConsulta select": "change",
    "blur #cpf": "validarCpf"
  },

  initialize: function () {
    var that = this;
    removerMensagens();

    // Pré-carrega o básico (não bloqueia, mas ajuda)
    $.getScript(BASE_FIADOR + '/modelo/Fiador.js');
    $.getScript(BASE_FIADOR + '/controle/FiadorModalControle.js');

    $.get(this.urlTemplate).done(function (data) {
      that.template = _.template(data);
      that.render();
      loadMask();
      removeAutoComplete('cpf', 'cpf');
    });
  },

  render: function () {
    $(this.el).html(this.template(this.model.toJSON()));
    $('#divResultado').hide();
    $('#tbResultadoFiadores tbody').empty();
    return this;
  },

  change: function (e) {
    var obj = {};
    obj[e.target.name] = e.target.value;
    this.model.set(obj);
  },

  validarCpf: function (e) {
    removerMensagens();
    var cpf = purificaAtributo(e.target.value);
    if (cpf !== '') {
      var msg = validarCPF(cpf);
      if (msg !== '') {
        mostrarErrors([{ message: msg }]);
        return false;
      }
    }
    return true;
  },

  limpar: function () {
    removerMensagens();
    limparFormulario('#formFiltroConsulta');
    if (this.model && this.model.initialize) this.model.initialize();
    $('#divResultado').hide('slow');
    $('#tbResultadoFiadores tbody').empty();
  },

  validar: function () {
    if (!this.model.isValid()) {
      mostrarErrors(this.model.validationError);
      return false;
    }
    return true;
  },

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

        console.log('[consultarFiadores] retorno =', data);

        // quando backend devolve mensagem de regra
        if (data && data.mensagem) {
          return mostrarErrors([{ message: data.mensagem }]);
        }

        // ✅ NORMALIZA: backend pode devolver ARRAY direto (seu caso)
        var lista = [];
        if ($.isArray(data)) {
          lista = data;
        } else if (data) {
          lista =
            data.listaFiadores ||
            data.fiadores ||
            data.listaRetorno ||
            (data.retorno && data.retorno.listaRetorno) ||
            (data.objeto && data.objeto.listaRetorno) ||
            [];
        }

        that.renderTabelaFiadores(lista);
        $('#divResultado').show('slow');
      })
      .fail(function (xhr) {
        $('#ajaxStatus').modal('hide');
        console.error('[listarFiadores] FAIL', xhr && xhr.status, xhr && xhr.responseText);

        mostrarErrors([{
          message: 'Falha ao consultar fiadores. Verifique Network > Response.'
        }]);
      });
  },

  // ---------- TABELA ----------

  renderTabelaFiadores: function (fiadores) {
    var $tbody = $('#tbResultadoFiadores tbody');
    if ($tbody.length === 0) return;

    $tbody.empty();

    if (!fiadores || fiadores.length === 0) {
      $tbody.append('<tr><td colspan="5">Nenhum fiador encontrado.</td></tr>');
      return;
    }

    var that = this;

    _.each(fiadores, function (f) {

      // ✅ MAPEAMENTO ROBUSTO (para bater com o que o backend devolver)
      var numeroContrato = (f.numeroContrato || f.nuContratoFormatado || f.contrato || f.nuContrato || '');
      var cpfFiador      = (f.cpfFiador || f.coCpfFiador || f.cpf || f.cpfCandidato || '');
      var nomeFiador     = (f.nomeFiador || f.noFiador || f.nome || f.nomeCandidato || '');
      var dtNasc         = (f.dataNascimento || f.dtNascimento || f.dataNasc || f.dtNasc || '');

      var tr =
        '<tr>' +
          '<td>' + (numeroContrato || '') + '</td>' +
          '<td>' + (cpfFiador ? mascararCpf(cpfFiador) : '') + '</td>' +
          '<td>' + (nomeFiador || '') + '</td>' +
          '<td>' + (dtNasc || '') + '</td>' +
          '<td>' +
            '<a href="#" class="btn btn-mini btn-primary btnAlterarFiadorRow">Alterar</a> ' +
            '<a href="#" class="btn btn-mini btn-danger btnExcluirFiadorRow">Excluir</a>' +
          '</td>' +
        '</tr>';

      var $tr = $(tr);

      $tr.find('.btnAlterarFiadorRow').click(function (e) {
        e.preventDefault();
        that.alterarFiador(f);
      });

      $tr.find('.btnExcluirFiadorRow').click(function (e) {
        e.preventDefault();
        that.excluirFiador(f, $tr);
      });

      $tbody.append($tr);
    });
  },

  excluirFiador: function (fiadorRow, $tr) {
    removerMensagens();

    if (!confirm('Confirma a exclusão do fiador selecionado?')) return;

    try {
      // Por enquanto, remove só na tela:
      $tr.remove();
    } catch (err) {
      console.log(err);
      alert('Falha ao excluir.');
    }
  },

  alterarFiador: function (fiadorRow) {
    var model = new Fiador();

    // ✅ pega CPF mesmo quando vem como "cpf"
    var cpf = (fiadorRow.cpfFiador || fiadorRow.coCpfFiador || fiadorRow.cpf || '');
    if (cpf) model.set("cpf", cpf);

    var nome = (fiadorRow.nomeFiador || fiadorRow.noFiador || fiadorRow.nome || '');
    if (nome) model.set("nome", nome);

    var dt = (fiadorRow.dataNascimento || fiadorRow.dtNascimento || fiadorRow.dataNasc || '');
    if (dt) model.set("dataNascimento", dt);

    // reaproveita codigoFiesConsulta
    var cod = this.model.get("codigoFiesConsulta");
    if (cod) model.set("codigoFies", cod);

    this.abrirTelaCadastroFiador(model, "alterar");
  },

  novoFiador: function (e) {
    if (e) e.preventDefault();
    var model = new Fiador();

    var cod = this.model.get("codigoFiesConsulta");
    if (cod) model.set("codigoFies", cod);

    this.abrirTelaCadastroFiador(model, "incluir");
  },

  abrirTelaCadastroFiador: function (modelFiador, modo) {
    removerMensagens();

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
          modo: modo
        }).el
      );
    })
    .fail(function (xhr, status, err) {
      console.log("[abrirTelaCadastroFiador] FAIL", status, err);
      alert('Erro ao carregar a tela de cadastro do fiador.');
    });
  },

  localizarCodFies: function () {
    $('#divResultado').hide();
    this.detalhe = new ConsultaGenericaEstudanteModalControle({
      el: $('#divModalIncluir'),
      model: new Estudante(),
      modelAnterior: this.model
    });
  },

  sair: function () { abrirPagina('../fes-web/fes-index.html'); },
  voltar: function () { abrirPagina('../fes-web/fes-index.html'); }

});






//# sourceURL=ConsultaFiador.js

window.ConsultaFiador = Backbone.Model.extend({

  // ✅ endpoint do FiadorRest
  urlRoot: '../fes-web/emprest/fiador/consultaFiadores',

  defaults: {
    cpf: '',
    codigoFiesConsulta: '',
    agenciaConsulta: ''
  },

  initialize: function () {
    this.set('cpf', '');
    this.set('codigoFiesConsulta', '');
    this.set('agenciaConsulta', '');
  },

  validate: function (attributes) {
    if (attributes === undefined) return true;

    var errors = [];

    var cpf = purificaAtributo(attributes.cpf || '');
    var codigoFies = purificaAtributo(attributes.codigoFiesConsulta || '');

    // Pelo fluxo novo: pelo menos CPF OU Código FIES
    if (!cpf && (!codigoFies || codigoFies === '0.000')) {
      errors.push({ message: 'Informe o código FIES ou o CPF do estudante.' });
    } else if (cpf) {
      var msg = validarCPF(cpf);
      if (msg !== '') errors.push({ message: msg });
    }

    return errors.length > 0 ? errors : false;
  },

  buscar: function () {
    var cpf = purificaAtributo(this.attributes.cpf || '');
    var codigoFies = purificaAtributo(this.attributes.codigoFiesConsulta || '');

    var params = {};
    // ✅ o FiadorRest.consultaFiadores espera QueryParam("codigoFies")
    if (codigoFies) params.codigoFies = codigoFies;

    // (se você quiser permitir filtro por cpf na lista, só se o backend aceitar esse param)
    // if (cpf) params.cpf = cpf;

    return this.fetch({
      type: 'GET',
      data: $.param(params),
      cache: false
    });
  }

});
