var BASE_FIADOR = '../fes-web/servicos/contratofies/manutencaofiador';

window.FiadorBoxControle = Backbone.View.extend({
	_self: null,

	events: {
		'click a#btn-AlterarFiador': 'alterarFiador',
		'click [class="accordion-toggle"][title="toggle"]': 'visualizar'
	},

	initialize: function () {
		console.log("call -> FiadorBoxControle -> AlterarFiador");
		var that = this;

		$.get(BASE_FIADOR + '/visao/FiadorBox.html').done(function (data) {
			that.template = _.template(data);

			$.when(
				$.get(BASE_FIADOR + '/modelo/Fiador.js'),
				$.get(BASE_FIADOR + '/controle/FiadorListControle.js'),
				$.get(BASE_FIADOR + '/controle/FiadorEnderecoListControle.js'),
				$.get(BASE_FIADOR + '/controle/FiadorContatoListControle.js'),
				$.get(BASE_FIADOR + '/controle/FiadorModalControle.js')
			).done(function () {
				that.render();
			});
		});
	},

	render: function () {
		console.log("call -> FiadorBoxControle -> render");

		$(this.el).html(this.template(this.model.toJSON()));

		_self = this;

		window.setTimeout(function () {
			$(".column").sortable({
				connectWith: '.column',
				revert: true,
				iframeFix: false,
				items: 'div.box',
				opacity: 0.8,
				helper: 'original',
				forceHelperSize: true,
				placeholder: 'box-placeholder round-all',
				forcePlaceholderSize: true,
				tolerance: 'pointer'
			});

			$(".column").disableSelection();

		}, 100);

		return this;
	},

	visualizar: function (e1) {
		console.log("call -> FiadorBoxControle -> visualizar");

		var wCodigo = (e1.target.nodeName === 'A')
			? $(e1.target).attr('data-index')
			: $(e1.target).parent().attr('data-index');

		$('#messageContainer').html("");

		if (!($('#objetoListaBox' + wCodigo).hasClass('collapse'))) {
			$('#objetoListaBox' + wCodigo).addClass('collapse').removeClass('expanded');
			console.log("call -> FiadorBoxControle -> visualizar -> collapse");
			return;
		}

		$('#objetoListaBox' + wCodigo).addClass('expanded').removeClass('collapse');

		var _self = this;

		console.log(_self.model);

		_self.$('#objetoListaBox' + wCodigo).html(new FiadorListControle({
			model: _self.model
		}).el);

		_self.$('#objetoListaBox' + wCodigo).append(new FiadorEnderecoListControle({
			model: _self.model
		}).el);

		_self.$('#objetoListaBox' + wCodigo).append(new FiadorContatoListControle({
			model: _self.model
		}).el);

		if (_self.options.alterar || _self.options.alterar === undefined) {
			if (!(($.inArray("FES_GESTOR", usuario.get("grupoUsuarioList")) == -1) &&
				$.inArray("FES_MANUT", usuario.get("grupoUsuarioList")) == -1 &&
				$.inArray("FES_MANUTJUR", usuario.get("grupoUsuarioList")) == -1 &&
				$.inArray("FES_AGENCIA", usuario.get("grupoUsuarioList")) == -1)) {

				_self.$('#objetoListaBox' + wCodigo).append(
					'<a href="#frmModal" id="btn-AlterarFiador" role="button" class="btn" data-toggle="modal">Alterar &raquo;</a> &nbsp;&nbsp; ' +
					'<a href="javascript:void(0)" id="btn-RemoverFiador" role="button" class="btn btn-warning">Remover</a>'
				);

				$("#btn-RemoverFiador").click(function (evt) {
					console.log("call -> FiadorBoxControle -> visualizar -> RemoverFiador");
					_self.removerFiador(evt);
				});
			}
		}
	},

	alterarFiador: function () {
		console.log("call -> FiadorBoxControle -> alterarFiador");
		var that = this;

		var wFiador = new FiadorModalControle({
			model: this.model
		});

		$('#modalBody').html(wFiador.el);
		$('#frmModal').css('left', '15%');

		wFiador.callback = function () {
			console.log("call -> FiadorBoxControle -> callback ");
			$("#frmModal").modal('hide');
			$('#modalBody').html("");
			that.callback(that.model);
		};
	},

	removerFiador: function (e) {
		console.log("call -> FiadorBoxControle -> removerFiador");
		e.preventDefault();

		var that = this;

		mensagemConfirmacao(
			"Você deseja realmente deseja excluir o fiador?",
			"Cancelar",
			"OK",
			function () {
				that.model.excluir()
					.done(function (data) {
						if (data && data.codigo > 0) {
							var errors = [];
							errors.push({
								name: data.codigo,
								message: data.mensagem
							});
							mostrarErrors(errors);
						} else {
							mostrarSucessos([{
								message: "Comando de exclusão realizado com sucesso!"
							}]);

							that.model.set("removido", true);

							if (that.$el && that.$el.length) {
								that.$el.remove();
							}

							if (typeof that.callback === 'function') {
								that.callback(that.model);
							}
						}
					})
					.fail(function (data) {
						console.log("erro 3");
						console.log(data);

						var errors = [];
						errors.push({
							name: "1",
							message: "Ocorreu um erro na realização do comando, tente novamente!"
						});
						mostrarErrors(errors);
					});
			},
			"Confirmação"
		);
	},

	retorno: function () {
		console.log("call -> FiadorBoxControle -> retorno");
		$('#modalBody').modal('hide');
		$('#modalBody').html("");
		this.setControls(this);
	},

	// Toggle button widget
	widgetToggle: function (e) {
		e.parent().parent().toggleClass("round-all");
		e.parent().parent().toggleClass("round-top");

		if (e.html() == "<i class=\"icon-plus\"></i>") {
			e.html("<i class=\"icon-minus\"></i>");
		} else {
			e.html("<i class=\"icon-plus\"></i>");
		}

		e.parent().parent().next(".box-content").toggleClass("box-content-closed");

		return false;
	}
});

//# sourceURL=FiadorBoxControle.js
