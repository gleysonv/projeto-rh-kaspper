var BASE_FIADOR = '../fes-web/servicos/contratofies/manutencaofiador';

window.FiadorModalControle = Backbone.View.extend({
  callback: null,
  modelFiador: null,

  initialize: function () {
    var that = this;
    removerMensagens();

    // clona o fiador recebido
    // (guarda no "this" para o Cancelar funcionar)
    this._cloneFiadorJsonString = JSON.stringify(this.model.clone());
    this.modelFiador = new Fiador(JSON.parse(this._cloneFiadorJsonString));
    
    this.modelAnterior = this.options.modelAnterior || null;
    this.modo = this.options.modo || 'incluir'; 

    $.when(
      $.getScript(BASE_FIADOR + '/controle/FiadorControle.js'),
      $.getScript(BASE_FIADOR + '/controle/FiadorConjugeControle.js'),
      $.getScript(BASE_FIADOR + '/controle/FiadorEnderecoControle.js'),
      $.getScript(BASE_FIADOR + '/controle/FiadorContatoControle.js')
    ).done(function () {

      $.get(BASE_FIADOR + '/visao/FiadorModal.html').done(function (data) {
        that.template = _.template(data);
        that.render();
      });

    }).fail(function () {
      console.error('FiadorModalControle: falha ao carregar controles das abas');
    });
  },
  
  voltarParaConsulta: function (){
    var modelConsulta = this.modelAnterior || new ConsultaFiador();
    
    $('#conteiner').empty();
    
    new ConsultaFiadorControle({
        el: $('#conteiner'),
        model: modelConsulta
    })
  } 

  render: function () {
    $(this.el).html(this.template(this.modelFiador.toJSON()));

    var _this = this;
    var cloneFiador = _this._cloneFiadorJsonString; // snapshot inicial

    $('#modalLabel').html("Dados do Fiador");
    $('#btnSalvar').html("Salvar mudan&ccedil;as");

    //  IMPORTANTE: NÃO chamar .render() manualmente (eles renderizam depois que carregam template)
    $('#tabDadosFiador').empty();
    new FiadorControle({ el: $('#tabDadosFiador'), model: _this.modelFiador });

    $('#tabConjuge').empty();
    new FiadorConjugeControle({ el: $('#tabConjuge'), model: _this.modelFiador });

    $('#tabEndereco').empty();
    new FiadorEnderecoControle({ el: $('#tabEndereco'), model: _this.modelFiador });

    $('#tabContrato').empty();
    new FiadorContatoControle({ el: $('#tabContrato'), model: _this.modelFiador });

    // ajuste visual
    $('#modalBody legend').hide();

    $("#btnSalvar").unbind("click");
    $('#btnSalvar').click(function () {
      if (!_this.modelFiador.isValid()) {
        _this.mostrarErros(_this.modelFiador.validationError);
        $('#modalBody').animate({ scrollTop: 0 }, 500);
        return;
      }

      $("#btnSalvar").attr("disabled", "disabled");
      $('#ajaxStatus').modal('show');
      _this.salvar();
    });


    // evita duplicar bind ao reabrir
    $("#btnSalvarFiadorModal").off("click");
    $("#btnCancelarFiadorModal").off("click");
    $("#btnSairFiadorModal").off("click");

    $("#btnSalvarFiadorModal").on("click", function (e) {
      e.preventDefault();
      removerMensagens();

      if (!_this.modelFiador.isValid()) {
        _this.mostrarErros(_this.modelFiador.validationError);
        $('#modalBody').animate({ scrollTop: 0 }, 500);
        return;
      }

      $("#btnSalvarFiadorModal").attr("disabled", "disabled");
      $('#ajaxStatus').modal('show');
      _this.salvar();
    });

    $("#btnCancelarFiadorModal").on("click", function (e) {
      e.preventDefault();
      // volta o model para o clone inicial (cancelar de verdade)
      _this.modelFiador.set(JSON.parse(cloneFiador || '{}'));
      $('#frmModal').modal('hide');
    });




    return this;
  },

  salvar: function () {
    var _this = this;

    var codigoFies = _this.modelFiador.get("codigoFies");
    if (codigoFies) {
        codigoFies = codigoFies.toString().replace(/\D/g, '');
        _this.modelFiador.set("codigoFies", codigoFies);
    }

    this.modelFiador.salvar().done().success(function (data) {
    debugger;
      $("#btnSalvar").removeAttr("disabled");
      $('#ajaxStatus').modal('hide');

      if (data.codigo > 0) {
        _this.mostrarErros([{ name: data.codigo, message: data.mensagem }]);
        $('#modalBody').animate({ scrollTop: 0 }, 500);
      } else {
        _this.model.set(_this.modelFiador.toJSON());
        mensagemAlerta("Comando realizado com sucesso", "OK", function () {
          $('#modalBody').html("");
          $('#frmModal').modal('hide');
          if (_this.callback) _this.callback(_this.modelFiador);
        });
      }
    }).error(function () {
      $("#btnSalvar").removeAttr("disabled");
      $('#ajaxStatus').modal('hide');
      _this.mostrarErros([{ name: "1", message: "Ocorreu um erro na realização do comando, tente novamente!" }]);
      $('#modalBody').animate({ scrollTop: 0 }, 500);
    });
  },

  mostrarErros: function (errors) {
    var wMsg = '<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">x</button>';
    _.each(errors, function (error) {
      wMsg += error.message + "</BR>";
    });
    wMsg += '</div>';
    $('#msgModal').html(wMsg);
  }
});
