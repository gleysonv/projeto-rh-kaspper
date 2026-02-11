var _self = this; // controle backbone
var $prof = $("#profissao");

// importante: se você tem esse div, ok:
var $container = $("#exportOrder");
if ($container.length === 0) {
  // fallback: anexa no body se não existir
  $container = $("body");
}

$prof.autocomplete({
  minLength: 3,
  delay: 300,                 // evita flood no backend
  appendTo: $container,
  source: function(request, response) {
    // segurança: se tiver menos de 3 letras, não chama backend
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
    // deixa o input com a descrição
    $prof.val(ui.item.value);

    // grava o codigo no model (isso resolve a validação)
    // ajuste aqui pro seu model real:
    _self.model.set("codigoProfissao", ui.item.id);

    return false;
  },

  change: function(event, ui) {
    // se o usuário digitou e não escolheu item válido, invalida o código
    if (!ui.item) {
      _self.model.set("codigoProfissao", "");
    }
  }
});

// bônus: ao digitar manualmente, limpa o codigoProfissao
$prof.on("input", function() {
  _self.model.set("codigoProfissao", "");
});
