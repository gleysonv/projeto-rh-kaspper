window.FiadorBoxControle = Backbone.View
		.extend({
			_self : null,
			events : {
				'click a#btn-AlterarFiador' : 'alterarFiador',
				'click [class="accordion-toggle"][title="toggle"]' : 'visualizar',
			},

			initialize : function() {
				console.log("call -> FiadorBoxControle -> AlterarFiador");
				var that = this;
				$.get('../fes-web/servicos/cadastro/fiador/visao/FiadorBox.html').done(
						function(data) {
							that.template = _.template(data);

							$.when($.get('../fes-web/servicos/cadastro/fiador/modelo/Fiador.js'),
									$.get('../fes-web/servicos/cadastro/fiador/controle/FiadorListControle.js'),
									$.get('../fes-web/servicos/cadastro/fiador/controle/FiadorEnderecoListControle.js'),
									$.get('../fes-web/servicos/cadastro/fiador/controle/FiadorContatoListControle.js'),
									$.get('../fes-web/servicos/cadastro/fiador/controle/FiadorModalControle.js')).done(function() {
								that.render();
							});
						});
			},

			render : function() {
				console.log("call -> FiadorBoxControle -> render");

				$(this.el).html(this.template(this.model.toJSON()));

				_self = this;

				window.setTimeout(function() {
					// console.log ("call -> FiadorBoxControle -> setTimeout");

					$(".column").sortable({
						connectWith : '.column',
						revert : true,
						iframeFix : false,
						items : 'div.box',
						opacity : 0.8,
						helper : 'original',
						forceHelperSize : true,
						placeholder : 'box-placeholder round-all',
						forcePlaceholderSize : true,
						tolerance : 'pointer'
					});

					// console.log ("call -> FiadorBoxControle -> setTimeout
					// fim");

					$(".column").disableSelection();

				}, 100);

				// console.log('fim render!');
				return this;
			},

			visualizar : function(e1) {
				console.log("call -> FiadorBoxControle -> visualizar");

				var wCodigo = (e1.target.nodeName === 'A') ? $(e1.target).attr('data-index') : $(e1.target).parent().attr('data-index');
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
					model : _self.model
				}).el);

				_self.$('#objetoListaBox' + wCodigo).append(new FiadorEnderecoListControle({
					model : _self.model
				}).el);

				_self.$('#objetoListaBox' + wCodigo).append(new FiadorContatoListControle({
					model : _self.model
				}).el);

				if (_self.options.alterar || _self.options.alterar == undefined) {

					//RTC_24491053
					if (!(($.inArray("FES_GESTOR", usuario.get("grupoUsuarioList")) == -1) && $.inArray("FES_MANUT", usuario.get("grupoUsuarioList")) == -1 && $.inArray("FES_CECOQGO", usuario.get("grupoUsuarioList")) == -1 && $.inArray("FES_MANUTJUR", usuario.get("grupoUsuarioList")) == -1 && $.inArray("FES_AGENCIA", usuario.get("grupoUsuarioList")) == -1)) {

						_self
								.$('#objetoListaBox' + wCodigo)
								.append(
										'<a href="#frmModal" id="btn-AlterarFiador" role="button" class="btn" data-toggle="modal">Alterar &raquo;</a> &nbsp;&nbsp; <a href="javascript:void(0)" id="btn-RemoverFiador" role="button" class="btn btn-warning">Remover</a>');

						$("#btn-RemoverFiador").click(function(evt) {
							console.log("call -> FiadorBoxControle -> visualizar -> RemoverFiador");
							_self.removerFiador(evt);
						});
					}
				}
			},

			alterarFiador : function() {
				console.log("call -> FiadorBoxControle -> alterarFiador");
				var that = this;
				// return;
				var wFiador = new FiadorModalControle({
					model : this.model
				});

				$('#modalBody').html(wFiador.el);
				$('#frmModal').css('left', '15%');	

				wFiador.callback = function(e) {
					console.log("call -> FiadorBoxControle -> callback ");
					$("#frmModal").modal('hide');
					$('#modalBody').html("");
					// that.render();
					that.callback(that.model);
				}
			},

			removerFiador : function(e) {
				console.log("call -> FiadorBoxControle -> removerFiador");
				e.preventDefault();

				var that = this;
				mensagemConfirmacao("Você deseja realmente excluir o fiador?", "Cancelar", "OK", function() {

					that.model.excluir().done().success(function(data) {
						if (data.codigo > 0) {
							var errors = [];
							errors.push({
								name : data.codigo,
								message : data.mensagem
							});
							mostrarErrors(errors);
						} else {
							mensagemAlerta("Comando realizado com sucesso", "OK", function() {
								that.model.set("removido", true);
								that.callback(that.model);
							});
						}
					}).error(function(data) {
						console.log("erro 3");
						console.log(data);
						var errors = [];
						errors.push({
							name : "1",
							message : "Ocorreu um erro na realização do comando, tente novamente!"
						});
						mostrarErrors(errors);
					});

				}, "Confirmação");
			},

			retorno : function() {
				console.log("call -> FiadorBoxControle -> retorno");
				$('#modalBody').modal('hide');
				$('#modalBody').html("");
				this.setControls(this);
			},

			// Toggle button widget
			widgetToggle : function(e) {
				// Make sure the bottom of the box has rounded corners
				e.parent().parent().toggleClass("round-all");
				e.parent().parent().toggleClass("round-top");

				// replace plus for minus icon or the other way around
				if (e.html() == "<i class=\"icon-plus\"></i>") {
					e.html("<i class=\"icon-minus\"></i>");
				} else {
					e.html("<i class=\"icon-plus\"></i>");
				}

				// close or open box
				e.parent().parent().next(".box-content").toggleClass("box-content-closed");

				// Prevent the browser jump to the link anchor
				return false;
			}
		});
//# sourceURL=FiadorBoxControle.js			
