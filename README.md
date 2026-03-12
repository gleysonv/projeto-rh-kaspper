this.combo = new Combo();
this.combo.set('dominioCombo', 56);
this.combo.set('filtroNumerico', _this2.codigoProfissao);

var _self = this;
var $prof = $("#profissao");

// destrói autocomplete anterior, se já existir
try {
    $prof.autocomplete("destroy");
} catch (e) {}

$prof.autocomplete({
    minLength: 3,
    delay: 300,
    appendTo: "body",
    source: function (request, response) {
        var term = $.trim(request.term || "");

        if (term.length < 3) {
            return response([]);
        }

        try {
            $('#ajaxStatus').modal('show');
        } catch (e) {}

        var combo = new Combo();
        combo.set('dominioCombo', 56);
        combo.set('filtroTextual', term);

        $fes.post('../fes-web/emprest/consultas/carregarCombo', combo, function sucesso(data) {
            var lista = (data && data.listaRetorno) ? data.listaRetorno : [];

            try {
                $('#ajaxStatus').modal('hide');
            } catch (e) {}

            response($.map(lista, function (item) {
                return {
                    id: item.identificadorNumerico,
                    label: item.descricao,
                    value: item.descricao
                };
            }));
        });
    },

    select: function (event, ui) {
        event.preventDefault();
        $prof.val(ui.item.label);
        _self.model.set("codigoProfissao", ui.item.id);
        return false;
    },

    focus: function (event, ui) {
        event.preventDefault();
        $prof.val(ui.item.label);
        return false;
    },

    change: function (event, ui) {
        if (!ui.item) {
            _self.model.set("codigoProfissao", "");
        }
    },

    open: function () {
        $(".ui-autocomplete").css("z-index", 99999);
    }
});

$prof.on("input", function () {
    _self.model.set("codigoProfissao", "");
});
