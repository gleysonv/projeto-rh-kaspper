var _self = this;

// 1) Se já existe codigoProfissao, carrega a descrição para o input
var codigoProf = _this2.codigoProfissao;
if (codigoProf) {
  var comboInit = new Combo();
  comboInit.set('dominioCombo', 56);
  comboInit.set('filtroNumerico', codigoProf);

  $fes.post('../fes-web/emprest/consultas/carregarCombo', comboInit, function sucesso(data) {
    var lista = (data && data.listaRetorno) ? data.listaRetorno : [];
    if (lista.length > 0) {
      // aqui sim pode usar [0], pois veio filtrado por filtroNumerico
      $("#profissao").val(lista[0].descricao);
    }
  });
}

// 2) Autocomplete funcional
var $prof = $("#profissao");
var $container = $("#exportOrder");
if ($container.length === 0) {
  $container = $("body");
}

$prof.autocomplete({
  minLength: 3,
  delay: 300,
  appendTo: $container,

  source: function(request, response) {
    var term = (request.term || "").trim();
    if (term.length < 3) {
      return response([]);
    }

    var combo = new Combo();
    combo.set('dominioCombo', 56);
    combo.set('filtroTextual', term);

    $fes.post('../fes-web/emprest/consultas/carregarCombo', combo, function sucesso(data) {
      var lista = (data && data.listaRetorno) ? data.listaRetorno : [];

      response($.map(lista, function(item) {
        return {
          id: item.identificadorNumerico,
          label: item.descricao,
          value: item.descricao
        };
      }));
    });
  },

  select: function(event, ui) {
    // mostra descrição no input
    $prof.val(ui.item.value);

    // grava o código no model (validação)
    _self.model.set("codigoProfissao", ui.item.id);

    return false;
  },

  change: function(event, ui) {
    // se digitou e não selecionou, invalida
    if (!ui.item) {
      _self.model.set("codigoProfissao", "");
    }
  }
});

// ao digitar, limpa o código para forçar seleção válida
$prof.on("input", function() {
  _self.model.set("codigoProfissao", "");
});
