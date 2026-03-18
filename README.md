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
      $.get('../fes-web/servicos/contratofies/consultagenericaestudante/controle/ConsultaGenericaEstudanteModalControle.js');
      $.get('../fes-web/servicos/contratofies/consultagenericaestudante/modelo/Estudante.js');
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
    // Ações (Novo Fiador / Sair) só após consulta
    $('#divAcoesPosConsulta').hide();
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
    $('#divAcoesPosConsulta').hide();
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
    $('#divAcoesPosConsulta').hide();
    removerMensagens();

    this.model.set('cpf', purificaAtributo(this.model.get('cpf')));

    if (!this.validar()) return;

    // 🔒 Importante: o endpoint atual (/fiador/consultaFiadores) exige codigoFies.
    // Se vier vazio, o servidor estoura NumberFormatException (codigoFies="").
    var cod = purificaAtributo(this.model.get('codigoFiesConsulta') || '');
    if (!cod) {
      return mostrarErrors([{ message: 'Informe o Código FIES (pode usar "Localizar Cód. Fies").' }]);
    }

    $('#ajaxStatus').modal('show');

    this.model.buscar()
      .done(function (data) {
        $('#ajaxStatus').modal('hide');

        console.log('[consultarFiadores] retorno =', data);

        // quando backend devolve mensagem de regra
        if (data && data.mensagem) {
          return mostrarErrors([{ message: data.mensagem }]);
        }

        //  NORMALIZA: backend pode devolver ARRAY direto (seu caso)
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
        $('#divAcoesPosConsulta').show('slow');
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

    function pick(obj, paths) {
      for (var i = 0; i < paths.length; i++) {
        var parts = paths[i].split('.');
        var cur = obj;
        for (var j = 0; j < parts.length; j++) {
          if (cur == null) break;
          cur = cur[parts[j]];
        }
        if (cur !== undefined && cur !== null && cur !== '' && typeof cur !== 'object') {
          return cur;
        }
      }
      return '';
    }

    _.each(fiadores, function (f) {

      //  MAPEAMENTO ROBUSTO (para bater com o que o backend devolver)
      var numeroContrato = pick(f, [
        'numeroContrato',
        'nuContratoFormatado',
        'nuContrato',
        'contrato.numeroContrato',
        'contrato.nuContratoFormatado',
        'contrato.nuContrato'
      ]);
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


    alterarFiador: function (fiadorRow) { var that = this;
        var model = new Fiador();

        var cpf = (fiadorRow.cpfFiador || fiadorRow.coCpfFiador || fiadorRow.cpf || '');
        cpf = (cpf || '').toString().replace(/\D/g, ''); // <<< limpa máscara

        var codigoFies = this.model.get('codigoFiesConsulta') || this.model.get('codigoFies') || '';
        codigoFies = (codigoFies || '').toString().replace(/\D/g, ''); // <<< remove pontos etc.

        if (!cpf || !codigoFies) {
            mostrarErrors([{ message: 'Não foi possível alterar: CPF ou Código FIES não informado.' }], '#msgModal');
            return;
        }

        model.set('cpf', cpf);
        model.set('codigoFies', codigoFies);

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

            data.codigoFies = codigoFies;
            data.cpf = cpf;

            model.set(data);

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
