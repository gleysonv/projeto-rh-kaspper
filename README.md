window.FiadorControle = Backbone.View.extend({
    modelUF: null,
    modelExpedidor: null,
    estadosCivis: [2, 3, 4, 5, 6, 7, 9],

    events: {
        'blur input': 'updateModel',
        'change select': 'updateModel',
        "changeDate .data": "changeDate2"
    },

    serialize: function () {
        return {
            title: this.$(".title").text(),
            start: this.$(".start-page").text(),
            end: this.$(".end-page").text()
        };
    },

    getScriptLog: function (url) {
        return $.getScript(url).fail(function (xhr, status, err) {
            console.error('[getScript FAIL', url, status, err);
        });
    },

    initialize: function () {
        var that = this;
        console.log("initialize FiadorControle");
        removerMensagens();

        $.when(
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/EstadoCivil.js'),
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/RegimeBens.js'),
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/Emancipado.js')
        ).done(function () {

            $.when(
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/OrgaoExpedidorColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/EstadoCivilColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/RegimeBensColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/EmancipadoColecao.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorListControle.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorEnderecoListControle.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorContatoListControle.js')
            ).done(function () {

                $.get('../fes-web/servicos/contratofies/manutencaofiador/visao/Fiador.html').done(function (data) {
                    that.template = _.template(data);
                    that.render();
                    loadMask();

                    setTimeout(function () {
                        $('#ajaxStatus').modal('hide');
                    }, 1000);
                });

            }).fail(function () {
                console.error('Erro ao carregar coleções do Fiador');
            });

        }).fail(function () {
            console.error('Erro ao carregar dependencia do Fiador');
        });
    },

    changeDate2: function (e) {
        console.log("call -> ConsultaGenericaControle -> changeDate2");
        console.log($('input', e.target).attr('name'));

        this.model.set($('input', e.target).attr('name'), $('input', e.target).val());
        console.log(this.model);
    },

    render: function () {
        var _this2;
        var _self = this;

        try {
            _this2 = this.model.toJSON();
        } catch (e) {
            _this2 = this.model;
        }

        $(this.el).html(this.template(_this2));

        this.modelUF = new UFColecao();
        this.modelUF.buscar().done(function (collection) {
            var wSelecionado = (_this2.identidade && _this2.identidade.uf) ? _this2.identidade.uf.sigla : "";
            gMontaSelect("#ufIdentidadeFiador", "sigla", "sigla", collection, wSelecionado);
        });

        if (_this2.cpf) {
            $('#cpfFiador').attr('readonly', true);
            $('#cpfFiador').prop('readonly', true);
        }

        $(".cpfmodal").mask("999.999.999-99").off('blur.mask').blur(function (e) {
            var src = e.currentTarget;
            if (src.value !== '') {
                var wRet = validarCPF(src.value);
                if (wRet !== '') {
                    src.value = '';
                    mostrarErrors([{
                        message: wRet
                    }], '#msgModal');
                } else {
                    removerMensagens();
                }
            }
        });

        $(".number").off('blur.mask').blur(function (e) {
            var src = e.currentTarget;
            src.value = src.value.replace(/\D/g, '').slice(0, 2);
        });

        this.modelExpedidor = new OrgaoExpedidorColecao();
        this.modelExpedidor.buscar().done(function (collection) {
            var wSelecionado = (_this2.identidade && _this2.identidade.orgaoExpedidor) ? _this2.identidade.orgaoExpedidor.codigo : "";
            gMontaSelect("#orgaoExpedidorFiador", "codigo", "nome", collection, wSelecionado);
        });

        this.modelRegimeBens = new RegimeBensColecao();
        this.modelRegimeBens.buscar().done(function (collection) {
            var wSelecionado = (_this2.regimeBens) ? _this2.regimeBens.codigo : "";
            gMontaSelect("#regimeBensFiador", "codigo", "nome", collection, wSelecionado);
        });

        this.modelEstadoCivil = new EstadoCivilColecao();
        this.modelEstadoCivil.buscar().done(function (collection) {
            var wSelecionado = (_this2.estadoCivil) ? _this2.estadoCivil.codigo : "";
            gMontaSelect("#estadoCivilFiador", "codigo", "nome", collection, wSelecionado);

            if (wSelecionado == 2 || wSelecionado == 9) {
                $('#litab2').show();
                $('#regimeBensFiador').removeAttr('disabled');
            } else {
                $('#litab2').hide();
                $("#regimeBensFiador").prop("selectedIndex", 0);
                $('#regimeBensFiador').attr('disabled', 'disabled');
            }
        });

        if (_this2.nacionalidade && _this2.nacionalidade.codigo) {
            gCarregarPaises("#nacionalidadeFiador", _this2.nacionalidade.codigo);
        } else {
            gCarregarPaises("#nacionalidadeFiador", "");
        }

        if (_this2.dataNascimento == null || _this2.dataNascimento === "") {
            _this2.dataNascimento = "01/01/1901";
        }

        var wdataNascimento = $caixa.data.converteStrToData(_this2.dataNascimento);
        var wIdade = moment().diff(wdataNascimento, 'years');

        this.modelEmancipado = new EmancipadoColecao();
        this.modelEmancipado.buscar().done(function (collection) {
            var wSelecionado = (_this2.emancipado) ? _this2.emancipado.codigo : "";

            gMontaSelect("#emancipadoMotivoFiador", "codigo", "nome", collection, wSelecionado);

            if (wSelecionado !== "") {
                _this2.emancipado.descricao = "S";
                $("#emancipadoFiador").prop("selectedIndex", 1);
            } else {
                if (_this2.emancipado) {
                    _this2.emancipado.descricao = "N";
                }
                $('#emancipadoMotivoFiador').attr('disabled', 'disabled');
            }

            if ($.inArray(parseInt((_this2.estadoCivil || {}).codigo), [2, 3, 4, 5, 6, 7, 9]) > -1) {
                if (_this2.emancipado) {
                    _this2.emancipado.codigo = "CAS";
                }
                try {
                    desabilitarCampo("#emancipadoFiador", 'disabled');
                    desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
                } catch (exception) {}
            } else {
                if (wIdade >= 18) {
                    $("#emancipadoFiador").val("S");

                    if (_this2.emancipado) {
                        _this2.emancipado.codigo = "MAI";
                    }

                    desabilitarCampo("#emancipadoFiador", 'disabled');
                    desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
                }
            }

            $('#emancipadoMotivoFiador').val((_this2.emancipado || {}).codigo);
        });

        this.combo = new Combo();
        this.combo.set('dominioCombo', 56);
        this.combo.set('filtroNumerico', _this2.codigoProfissao);

        var $prof = $("#profissao");
        var $container = $("#exportOrder");

        if ($container.length === 0) {
            $container = $("body");
        }

        $prof.autocomplete({
            minLength: 3,
            delay: 300,
            appendTo: $container,
            source: function (request, response) {
                var term = (request.term || "").trim();
                if (term.length < 3) {
                    return response([]);
                }

                try {
                    $('#ajaxStatus').modal('show');
                    $prof.prop('disabled', true);
                } catch (e) {}

                var combo = new Combo();
                combo.set('dominioCombo', 56);
                combo.set('filtroTextual', term);

                $fes.post('../fes-web/emprest/consultas/carregarCombo', combo, function sucesso(data) {
                    var lista = (data && data.listaRetorno) ? data.listaRetorno : [];

                    try {
                        $('#ajaxStatus').modal('hide');
                        $prof.prop('disabled', false);
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
                $prof.val(ui.item.value);
                _self.model.set("codigoProfissao", ui.item.id);
                return false;
            },

            change: function (event, ui) {
                if (!ui.item) {
                    _self.model.set("codigoProfissao", "");
                }
            }
        });

        $prof.on("input", function () {
            _self.model.set("codigoProfissao", "");
        });

        window.setTimeout(function () {
            loadMask();
        }, 500);

        return this;
    },

    updateModel: function (el) {
        console.log("call -> updateModel");

        var $el = $(el.target);
        var name = $el.attr('name');

        if (name != undefined) {
            this.model.set(name, $el.val());
        }

        this.model.set("cpf", purificaAtributo($("#cpfFiador").val()));

        var depCpf = $("#dependenteCPF").val();
        depCpf = depCpf ? depCpf.replace(/\D/g, '').slice(0, 2) : "";
        this.model.set("dependenteCPF", depCpf === "" ? 0 : parseInt(depCpf, 10));

        var wEc = this.model.get("estadoCivil");
        if (!wEc) {
            wEc = {};
            this.model.set("estadoCivil", wEc);
        }
        wEc.codigo = $("#estadoCivilFiador").val();
        wEc.nome = $("#estadoCivilFiador option:selected").text();

        if (wEc.codigo == 2 || wEc.codigo == 9) {
            $('#litab2').show();
            $('#regimeBensFiador').removeAttr('disabled');
        } else {
            $('#litab2').hide();
            $("#regimeBensFiador").prop("selectedIndex", 0);
            $('#regimeBensFiador').attr('disabled', 'disabled');
        }

        var wRb = this.model.get("regimeBens");
        if (!wRb) {
            wRb = {};
            this.model.set("regimeBens", wRb);
        }
        wRb.codigo = $("#regimeBensFiador").val();
        wRb.nome = $("#regimeBensFiador option:selected").text();

        var wId = {
            identidade: $("#identidadeFiador").val(),
            orgaoExpedidor: {
                codigo: $("#orgaoExpedidorFiador").val(),
                nome: $("#orgaoExpedidorFiador option:selected").text()
            },
            dataExpedicaoIdentidade: $("#dataExpedicaoIdentidadeFiador").val(),
            uf: {
                sigla: $("#ufIdentidadeFiador").val(),
                descricao: $("#ufIdentidadeFiador").val()
            }
        };

        this.model.set("identidade", wId);

        var wEm = this.model.get("emancipado");
        if (!wEm) {
            wEm = {};
            this.model.set("emancipado", wEm);
        }

        if ($("#emancipadoFiador").val() == "N") {
            wEm.codigo = "";
            wEm.descricao = "N";
            $('#emancipadoMotivoFiador').attr('disabled', 'disabled');
            $("#emancipadoMotivoFiador").prop("selectedIndex", 0);
        } else {
            wEm.codigo = $('#emancipadoMotivoFiador').val();
            wEm.descricao = "S";
            $('#emancipadoMotivoFiador').removeAttr('disabled');
        }

        var wNas = this.model.get("nacionalidade");
        if (!wNas) {
            wNas = {};
            this.model.set("nacionalidade", wNas);
        }
        wNas.codigo = $("#nacionalidadeFiador").val();
        wNas.nome = $("#nacionalidadeFiador option:selected").text();

        var wRenda = $("#rendaMensalFiador").val();
        this.model.set("valorRendaMensal", purificaMoeda(wRenda));

        if (el.currentTarget.id != "emancipadoFiador" && el.currentTarget.id != "emancipadoMotivoFiador") {
            var wIdade = 1;
            if (!(this.model.get("dataNascimento") == null || this.model.get("dataNascimento") === "")) {
                var wdataNascimento = $caixa.data.converteStrToData(this.model.get("dataNascimento"));
                wIdade = (new Date()).differenceInYears(wdataNascimento);
            }

            if (($.inArray(parseInt(wEc.codigo), this.estadosCivis) > -1) && wEc.codigo != 9) {
                wEm.codigo = "CAS";
                $("#emancipadoFiador").val("S");
                $('#emancipadoMotivoFiador').val(wEm.codigo);
            } else if (wEc.codigo == 9) {
                wEm.codigo = "MAI";
                $("#emancipadoFiador").val("S");
                $('#emancipadoMotivoFiador').val("MAI");
            } else {
                if (wIdade >= 18) {
                    wEm.codigo = "MAI";
                    $("#emancipadoFiador").val("S");
                    $('#emancipadoMotivoFiador').val("MAI");
                }
            }

            if ($.inArray(parseInt(wEc.codigo), [1, 9]) > -1 && wIdade < 18) {
                $('#emancipadoFiador').removeAttr('disabled');
                $('#emancipadoFiador').val("N");
                $('#emancipadoMotivoFiador').removeAttr('disabled');
                wEm.codigo = "";
                wEm.descricao = "N";
                $('#emancipadoMotivoFiador').attr('disabled', 'disabled');
                $('#emancipadoMotivoFiador').val("");
                if ($("#emancipadoFiador").val() != "N") {
                    $('#emancipadoMotivoFiador').removeAttr('disabled');
                }
            } else {
                desabilitarCampo("#emancipadoFiador", 'disabled');
                desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
            }
        }
    },

    hideErrors: function () {
        $('#msgModal').html("");
    }
});
