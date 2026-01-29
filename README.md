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

    // Dependências do fluxo de "Novo Fiador" (modal de abas)
    $.get(BASE_FIADOR + '/modelo/Fiador.js');
    $.get(BASE_FIADOR + '/controle/FiadorModalControle.js');

    $.get(this.urlTemplate).done(function (data) {
      that.template = _.template(data);
      that.render();
      loadMask();
      removeAutoComplete('cpf', 'cpf');
    });
  },

    novoFiador: function () {
      if ($('#frmModal').length === 0) {
        $('#divModalIncluir').html(
          '<div id="frmModal" class="modal hide fade" tabindex="-1">' +
          ' <div class="modal-header">' +
          '  <button type="button" class="close" data-dismiss="modal">×</button>' +
          '  <h3 id="modalLabel"></h3>' +
          ' </div>' +
          ' <div class="modal-body" id="modalBody"></div>' +
          ' <div class="modal-footer">' +
          '  <a href="#" class="btn" data-dismiss="modal">Fechar</a>' +
          '  <a href="#" class="btn btn-primary" id="btnSalvar">Salvar</a>' +
          ' </div>' +
          '</div>'
        );
      }

      var ctrl = new FiadorModalControle({
        el: $('#modalBody'),
        model: new Fiador()
      });

      ctrl.callback = function (fiadorSalvo) {
        // aqui você decide o que faz depois (ex: recarregar lista, etc.)
      };

      $('#frmModal').modal('show');
    }


  validarCpf: function (e) {
    removerMensagens();
    var cpf = purificaAtributo(e.target.value);

    if (cpf !== '') {
      var msg = validarCPF(cpf);
      if (msg !== '') {
        return mostrarErrors([{ message: msg }]);
      }
    }
    return true;
  },

  change: function (e) {
    var change = {};
    change[e.target.name] = e.target.value;
    this.model.set(change);
  },

  limpar: function () {
    removerMensagens();
    limparFormulario('#formFiltroConsulta');
    if (this.model && this.model.initialize) this.model.initialize();

    $('#divResultado').hide('slow');
    $('#tbResultadoFiadores tbody').empty();
  },

  localizarCodFies: function () {
    $('#divResultado').hide();
    this.detalhe = new ConsultaGenericaEstudanteModalControle({
      el: $('#divModalIncluir'),
      model: new Estudante(),
      modelAnterior: this.model
    });
  },

  consultar: function () {
    $('#divResultado').hide('slow');
    removerMensagens();
    this.model.set('cpf', purificaAtributo(this.model.get('cpf')));

    var that = this;

    if (!this.validar()) {
      $('#ajaxStatus').modal('hide');
      return;
    }

    $('#ajaxStatus').modal('show');

    this.model.buscar()
      .done(function (data) {
        if (data && data.mensagem) {
          $('#ajaxStatus').modal('hide');
          return mostrarErrors([{ message: data.mensagem }]);
        }

        // MOCK para permitir testar o visual mesmo sem backend
        if (!data) data = {};
        if (!data.fiadores && !data.listaFiadores) {
          data.fiadores = [
            { numeroContrato: '24.0340.185.0000061/47', cpfFiador: '', nomeFiador: 'CARLOS GAR', dataNascimento: '04/11/1999' },
            { numeroContrato: '19.0970.185.0003669/05', cpfFiador: '', nomeFiador: 'SIMONE FERREIRA', dataNascimento: '19/04/2011' }
          ];
        }

        that.renderResult(data);
        that.renderTabelaFiadores(data.fiadores || data.listaFiadores || []);
        $('#divResultado').show('slow');
      })
      .error(function (e) {
        console.log(e);

        // Mesmo com erro, deixa o front testável
        var mock = {
          fiadores: [
            { numeroContrato: '24.0340.185.0000061/47', cpfFiador: '', nomeFiador: 'CARLOS GAR', dataNascimento: '04/11/1999' }
          ]
        };
        that.renderResult(mock);
        that.renderTabelaFiadores(mock.fiadores);
        $('#divResultado').show('slow');
        $('#ajaxStatus').modal('hide');
      });
  },

  renderResult: function (data) {
    if (data && data.dadosCandidato) {
      $("#codigoFies").text(mascararCodigoFies(data.dadosCandidato.nuCandidato));
    } else if (this.model && this.model.get('codigoFiesConsulta')) {
      $("#codigoFies").text(this.model.get('codigoFiesConsulta'));
    }

    if (data && data.consultaPreAdit) {
      $("#cpfEstudade").text(mascararCpf(data.consultaPreAdit.coCpf || this.model.get('cpf')));
      $("#nomeEstudade").text(data.consultaPreAdit.noCandidato || '');
      $("#codigoIESUF").text(((data.cursoHabilitacao && data.cursoHabilitacao.noIes) ? data.cursoHabilitacao.noIes : '') + '/' + (data.consultaPreAdit.ufIEs || ''));
      $('#ultimoSemestre').val(data.consultaPreAdit.nuMinSemestre || '');
    } else {
      $("#cpfEstudade").text(this.model.get('cpf'));
    }

    if (data && data.cursoHabilitacao) {
      $("#unidadeAdmCampus").text(data.cursoHabilitacao.noCampus || '');
      $("#areaEspecifica").text(data.cursoHabilitacao.noAreaEspecifica || '');
      $("#contratoSIAPI").text(data.nuContratoFormatado || '');
      $("#cursoTurno").text((data.cursoHabilitacao.noCurso || '') + ' ' + (data.cursoHabilitacao.deTurno || ''));
      $("#areGeral").text(data.cursoHabilitacao.noAreaGeral || '');
    }

    $('#ajaxStatus').modal('hide');
  },

  renderTabelaFiadores: function (fiadores) {
    var $tbody = $('#tbResultadoFiadores tbody');
    if ($tbody.length === 0) return;

    $tbody.empty();

    if (!fiadores || fiadores.length === 0) {
      $tbody.append('<tr><td colspan="5">Nenhum fiador encontrado.</td></tr>');
      return;
    }

    _.each(fiadores, function (f) {
      var tr = '<tr>' +
        '<td>' + (f.numeroContrato || f.nuContratoFormatado || '') + '</td>' +
        '<td>' + (f.cpfFiador ? mascararCpf(f.cpfFiador) : '') + '</td>' +
        '<td>' + (f.nomeFiador || f.noFiador || '') + '</td>' +
        '<td>' + (f.dataNascimento || f.dtNascimento || '') + '</td>' +
        '<td><a href="#" class="btn btn-mini btn-primary btnNovoFiadorRow">Novo Fiador</a></td>' +
      '</tr>';

      var $tr = $(tr);
      $tr.find('.btnNovoFiadorRow').click(function (e) {
        e.preventDefault();
        $(document).trigger('novoFiador');
      });

      $tbody.append($tr);
    });

    var that = this;
    $(document).off('novoFiador.consultaFiador').on('novoFiador.consultaFiador', function () {
      that.novoFiador();
    });
  },

  novoFiador: function () {
    // Modal básico pra renderizar as abas (sem depender do template global)
    if ($('#frmModal').length === 0) {
      var modalHtml = ''
        + '<div id="frmModal" class="modal hide fade" tabindex="-1" role="dialog" aria-hidden="true">'
        + '  <div class="modal-header">'
        + '    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'
        + '    <h3 id="modalLabel"></h3>'
        + '  </div>'
        + '  <div class="modal-body" id="modalBody"></div>'
        + '  <div class="modal-footer">'
        + '    <a href="#" class="btn" data-dismiss="modal">Fechar</a>'
        + '    <a href="#" class="btn btn-primary" id="btnSalvar">Salvar</a>'
        + '  </div>'
        + '</div>';
      $('#divModalIncluir').html(modalHtml);
    }

    var modelFiador = new Fiador();
    var ctrl = new FiadorModalControle({
      el: $('#modalBody'),
      model: modelFiador
    });

    ctrl.callback = function () {};
    $('#frmModal').modal('show');
  },

  sair: function () {
    abrirPagina('../fes-web/fes-index.html');
  },

  validar: function () {
    if (!this.model.isValid()) {
      mostrarErrors(this.model.validationError);
      return false;
    }
    return true;
  },

  voltar: function () {
    abrirPagina('../fes-web/fes-index.html');
  },

  render: function () {
    $(this.el).html(this.template(this.model.toJSON()));
    $('#divResultado').hide();
    if ($('#tbResultadoFiadores tbody').length) {
      $('#tbResultadoFiadores tbody').empty();
    }
    return this;
  }

});

//# sourceURL=ConsultaFiadorControle.js
