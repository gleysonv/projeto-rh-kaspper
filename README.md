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
        if (data && data.mensagem) {
          $('#ajaxStatus').modal('hide');
          return mostrarErrors([{ message: data.mensagem }]);
        }

        // MOCK p/ testar visual
        if (!data) data = {};
        if (!data.fiadores && !data.listaFiadores) {
          data.fiadores = [
            { numeroContrato: '24.0340.185.0000061/47', cpfFiador: '00000000000', nomeFiador: 'CARLOS GAR', dataNascimento: '04/11/1999' },
            { numeroContrato: '19.0970.185.0003669/05', cpfFiador: '11111111111', nomeFiador: 'SIMONE FERREIRA', dataNascimento: '19/04/2011' }
          ];
        }

        that.renderTabelaFiadores(data.fiadores || data.listaFiadores || []);
        $('#divResultado').show('slow');
        $('#ajaxStatus').modal('hide');
      })
      .fail(function () {
        var mock = {
          fiadores: [
            { numeroContrato: '24.0340.185.0000061/47', cpfFiador: '00000000000', nomeFiador: 'CARLOS GAR', dataNascimento: '04/11/1999' }
          ]
        };
        that.renderTabelaFiadores(mock.fiadores);
        $('#divResultado').show('slow');
        $('#ajaxStatus').modal('hide');
      });
  },

  // ---------- PASSO 2 (Ações) ----------

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
      var numeroContrato = (f.numeroContrato || f.nuContratoFormatado || '');
      var cpfFiador = (f.cpfFiador || f.coCpfFiador || '');
      var nomeFiador = (f.nomeFiador || f.noFiador || '');
      var dtNasc = (f.dataNascimento || f.dtNascimento || '');

      var tr =
        '<tr>' +
          '<td>' + numeroContrato + '</td>' +
          '<td>' + (cpfFiador ? mascararCpf(cpfFiador) : '') + '</td>' +
          '<td>' + nomeFiador + '</td>' +
          '<td>' + dtNasc + '</td>' +
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

    // 1) Visual (sempre funciona)
    // 2) Backend (liga quando tiver endpoint certo)
    //    Exemplo: criar Fiador, setar url excluir, setar cpf/codigoFies e chamar salvar()

    try {
      // Se você tiver cpf e codigoFies, dá pra tentar chamar o backend:
      // var m = new Fiador();
      // m.url = "../fes-web/emprest/fiador/excluir";
      // m.set("cpf", fiadorRow.cpfFiador || "");
      // m.set("codigoFies", this.model.get("codigoFiesConsulta") || 0);
      // $('#ajaxStatus').modal('show');
      // m.salvar()
      //   .done(function(){ $tr.remove(); $('#ajaxStatus').modal('hide'); })
      //   .fail(function(){ $('#ajaxStatus').modal('hide'); alert('Falha ao excluir no backend.'); });

      // Por enquanto, remove só na tela:
      $tr.remove();
    } catch (err) {
      console.log(err);
      alert('Falha ao excluir.');
    }
  },

  alterarFiador: function (fiadorRow) {
    // abre a tela de cadastro com abas e tenta preencher o que tiver
    var model = new Fiador();

    // Preenche o que vier no resultado (se existir)
    if (fiadorRow.cpfFiador) model.set("cpf", fiadorRow.cpfFiador);
    if (fiadorRow.nomeFiador) model.set("nome", fiadorRow.nomeFiador);
    if (fiadorRow.dataNascimento) model.set("dataNascimento", fiadorRow.dataNascimento);

    // ajuda: se sua consulta tem codigoFiesConsulta, reaproveita
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

  // ---------- PASSO 3 (abre a tela de abas corretamente) ----------
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
      // Renderiza o "container com abas" (FiadorModalControle),
      // e ele mesmo injeta os formulários dentro de cada aba
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

//# sourceURL=ConsultaFiadorControle.js

FiadorListControle.js
var BASE_FIADOR = '../fes-web/servicos/contratofies/manutencaofiador';
window.FiadorListControle = Backbone.View.extend({

	events: {},

	initialize: function() {
		console.log("call -> FiadorListControle");
		var that = this;

		$.get(BASE_FIADOR + '/visao/FiadorList.html').done(function(data) {
			that.template = _.template(data);
			that.render();
		});
	},

	render: function() {
		console.log("call -> FiadorListControle -> render");
		console.log(this.model);
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});

