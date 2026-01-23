window.FiadorControle = Backbone.View.extend({
    modelUF: null,
	modelExpedidor: null,
	estadosCivis : [2, 3, 4, 5, 6, 7, 9],
	events: {
		'blur input': 'updateModel',
		'change select': 'updateModel',
		"changeDate .data": "changeDate2"
	},

	serialize: function() {
		return {
			title: this.$(".title").text(),
			start: this.$(".start-page").text(),
			end: this.$(".end-page").text()
		};
	},

	initialize: function() {
		var that = this;
		console.log("initialize FiadorControle");
		$.get('../fes-web/servicos/administracao/cadastrofiadorcontrato/visao/Fiador.html').done(function(data) {
			that.template = _.template(data);
			that.render();
		});
	},

	changeDate2: function (e) {
		console.log("call -> ConsultaGenericaControle -> changeDate2");
		console.log($('input', e.target).attr('name'));

		this.model.set($('input', e.target).attr('name'), $('input', e.target).val());
		console.log(this.model);
	},

	render: function() {
		var _this2;

		try {
			_this2 = this.model.toJSON();
		} catch (e) {
			_this2 = this.model;
		}

		$(this.el).html(this.template(_this2));

		this.modelUF = new UFColecao();
		this.modelUF.buscar().done(function(collection) {
			var wSelecionado = _this2.identidade.uf.sigla;
			gMontaSelect("#ufIdentidadeFiador", "sigla", "sigla", collection, wSelecionado);
		});
		
		if(_this2.cpf){
			$('#cpfFiador').attr('readonly', true);
			$('#cpfFiador').prop('readonly', true);
		}
		
		$(".cpfmodal").mask("999.999.999-99").off('blur.mask').blur(function (e) {
			var src = e.currentTarget;
			if (src.value != '') {
				var wRet = validarCPF(src.value); 
				if (wRet != ''){
					src.value = '';
					mostrarErrors([ {
						message : wRet
					} ], '#msgModal');					
				} else {
					removerMensagens();
				}
			}
		});

		this.modelExpedidor = new OrgaoExpedidorColecao();
		this.modelExpedidor.buscar().done(function(collection) {
			var wSelecionado = _this2.identidade.orgaoExpedidor.codigo;
			gMontaSelect("#orgaoExpedidorFiador", "codigo", "nome", collection, wSelecionado);
		});

		this.modelRegimeBens = new RegimeBensColecao();

		this.modelRegimeBens.buscar().done(function(collection) {
			var wSelecionado = _this2.regimeBens.codigo;
			gMontaSelect("#regimeBensFiador", "codigo", "nome", collection, wSelecionado);
		});

		this.modelEstadoCivil = new EstadoCivilColecao();
		this.modelEstadoCivil.buscar().done(function(collection) {
			var wSelecionado = _this2.estadoCivil.codigo;
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

		gCarregarPaises("#nacionalidadeFiador", _this2.nacionalidade.codigo);

		if (_this2.dataNascimento == null || _this2.dataNascimento == "") {
			_this2.dataNascimento = "01/01/1901";
		}

		var wdataNascimento = $caixa.data.converteStrToData(_this2.dataNascimento);
		var wIdade = moment().diff(wdataNascimento, 'years');

		this.modelEmancipado = new EmancipadoColecao();

		this.modelEmancipado.buscar().done(function(collection) {
			console.log("call -> ConsultaGenericaControle -> modelEmancipado");

			var wSelecionado = _this2.emancipado.codigo;

			gMontaSelect("#emancipadoMotivoFiador", "codigo", "nome", collection, wSelecionado);

			if (wSelecionado != "") {
				_this2.emancipado.descricao = "S";
				$("#emancipadoFiador").prop("selectedIndex", 1);
			} else {
				_this2.emancipado.descricao = "N";
				$('#emancipadoMotivoFiador').attr('disabled', 'disabled');
			}

			if ($.inArray(parseInt(_this2.estadoCivil.codigo), _this2.estadosCivis) > -1) {
				_this2.emancipado.codigo = "CAS";
				try {
					desabilitarCampo("#emancipadoFiador", 'disabled');
					desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
				} catch (exception) {}
			} else {
				if (wIdade >= 18) {
					$("#emancipadoFiador").val("S");

					_this2.emancipado.codigo = "MAI";

					desabilitarCampo("#emancipadoFiador", 'disabled');
					desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
				}
			}

			$('#emancipadoMotivoFiador').val(_this2.emancipado.codigo);

		});

		this.combo = new Combo();
		this.combo.set('dominioCombo', 56);
		this.combo.set('filtroNumerico', _this2.codigoProfissao);

		$fes.post('../fes-web/emprest/consultas/carregarCombo', this.combo, function sucesso(data) {
			if (data.listaRetorno.length > 0) {
				$("#profissao").val(data.listaRetorno[0].descricao);
			}
		});

		var _self = this;
		$("#profissao").autocomplete({
			matchContains: true,
			max: 50,
			minLength: 3,
			appendTo: $("#exportOrder"),
			source: function(request, response) {
				this.combo = new Combo();
				this.combo.set('dominioCombo', 56);
				this.combo.set('filtroTextual', request.term);

				$fes.post('../fes-web/emprest/consultas/carregarCombo', this.combo, function sucesso(data) {

					response($.map(data.listaRetorno, function(item) {
						return {
							value: item.identificadorNumerico,
							label: item.descricao
						}
					}));
				});
			},
			messages: {
				noResults: '',
				results: function() {}
			},

			select: function(event, ui) {
				console.log("call -> ConsultaGenericaControle -> select");
				event.preventDefault();
				_self.model.set("codigoProfissao", ui.item.value);
				$(event.target).val(ui.item.label);
			},
			focus: function(event, ui) {
				console.log("call -> ConsultaGenericaControle -> focus");
				event.preventDefault();
				$(event.target).val(ui.item.label);
			},

			open: function() {
				$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
			},
			close: function() {
				$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
			},
			change: function(event, ui) {
				console.log("call -> ConsultaGenericaControle -> change");
				if (!ui.item) {
					$(event.target).val("");
					_self.model.set("codigoProfissao", "");
				}
			}
		});

		window.setTimeout(function() {
			loadMask();
		}, 500);

		return this;
	},

	updateModel: function(el) {
		// funcao de chamada dos inputs
		console.log("call -> updateModel");

		var $el = $(el.target);
		var name = $el.attr('name');

		if (name != undefined) {
			console.log("call -> updateModel -> undefined");
			this.model.set(name, $el.val());
		}

		this.model.set("cpf", purificaAtributo($("#cpfFiador").val()));

		// ----------------------------------------------------------------------------
		var wEc = this.model.get("estadoCivil");
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

		wNas.codigo = $("#nacionalidadeFiador").val();
		wNas.nome = $("#nacionalidadeFiador option:selected").text();

		var wRenda = $("#rendaMensalFiador").val();
		this.model.set("valorRendaMensal", purificaMoeda(wRenda));

		if(el.currentTarget.id != "emancipadoFiador" && el.currentTarget.id != "emancipadoMotivoFiador"){
			var wIdade = 1;
			if (!(this.model.get("dataNascimento") == null || this.model.get("dataNascimento") == "")) {
				var wdataNascimento = $caixa.data.converteStrToData(this.model.get("dataNascimento"));
				wIdade = (new Date()).differenceInYears(wdataNascimento);
			}
	
			if (($.inArray(parseInt(wEc.codigo), this.estadosCivis) > -1) && wEc.codigo != 9) {
				console.log("call -> updateModel -> casado2");
				wEm.codigo = "CAS";
				$("#emancipadoFiador").val("S");
				$('#emancipadoMotivoFiador').val(wEm.codigo);
			} else if(wEc.codigo == 9){
				console.log("call -> updateModel ->  uniao estavel");
				wEm.codigo = "MAI";
				$("#emancipadoFiador").val("S");
				$('#emancipadoMotivoFiador').val("MAI");
			} else {
				if (wIdade >= 18) {
					console.log("call -> updateModel ->  18");
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
				if ($("#emancipadoFiador").val() != "N")
					$('#emancipadoMotivoFiador').removeAttr('disabled');
			} else {
				desabilitarCampo("#emancipadoFiador", 'disabled');
				desabilitarCampo("#emancipadoMotivoFiador", 'disabled');
			}
		}

	},

	hideErrors: function() {
		$('#msgModal').html("");
	}
});
