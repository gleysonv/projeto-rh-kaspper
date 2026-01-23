window.Fiador = Backbone.Model.extend({
	urlRoot: '../fes-web/servicos/administracao/cadastrofiadorcontrato/visao/Fiador.html',


	defaults: {
		codigo: 0,
		mensagem: "",
		codigoFies: 0.0,
		conjuge: new Conjuge().toJSON(),
		cpf: "",
		nome: "",
		dependenteCPF: 0,
		dataNascimento: "",
		ric: '',
		nacionalidade: new Nacionalidade().toJSON(),
		identidade: new Identidade().toJSON(),
		estadoCivil: new EstadoCivil().toJSON(),
		regimeBens: new RegimeBens().toJSON(),
		endereco: {
			endereco: "",
			numero: "",
			bairro: "",
			cep: "",
			uf: new UF(),
			cidade: {
				codigoCidade: 0,
				codigoIBGE: 0,
				nome: "",
				uf: new UF().toJSON()
			}
		},
		contato: new Contato().toJSON(),
		emancipado: new Emancipado().toJSON(),
		vinculacao: '',
		valorRendaMensal: 0,
		codigoProfissao: ''
	},

	initialize: function() {
		console.log("call -> Fiador - > initialize ");
	},

	validate: function(attributes, options) {
		console.log("call -> FiadorModel -> validate");

		if (attributes == undefined)
			return true;
		
		if(this.url == "../fes-web/emprest/fiador/excluir"){
			return false;
		}

		var datas = true;
		var isMenor18 = true;
		var errors = [];

		if (!attributes.nome) {
			errors.push({
				name: 'nome',
				message: 'Não foi informado o nome.'
			});
		}

		if (!attributes.cpf || validarCPF(attributes.cpf) != '') {
			errors.push({
				name: 'cpf',
				message: 'Não foi informado o cpf.'
			});
		}

		if (!attributes.dataNascimento) {
			errors.push({
				name: 'dataNascimento',
				message: 'A data de nascimento não informada.'
			});
			datas = false;
		} else {
			var dataMenos16 = new Date(obterAnoAtual() - 16, obterMesAtual(), obterDiaAtual());
			var dataMenos18 = new Date(obterAnoAtual() - 18, obterMesAtual(), obterDiaAtual());
			var dataNascimento = $caixa.data.converteStrToData(attributes.dataNascimento);
			isMenor18 = dataNascimento > dataMenos18;
			if (dataNascimento > dataMenos16) {
				errors.push({
					name: 'dataNascimento',
					message: 'A data de nascimento não pode ser maior ' + formatDate(dataMenos16, "dd/MM/yyyy")
				});
			}
		}

		if (attributes.nacionalidade == null || !attributes.nacionalidade.nome || !attributes.nacionalidade.codigo) {
			errors.push({
				name: 'nacionalidade',
				message: 'Não foi informado a nacionalidade'
			});
		}

		if (!attributes.identidade.identidade) {
			errors.push({
				name: 'identidade.identidade',
				message: 'Não foi informado a identidade.'
			});
		}

		if (!attributes.identidade.dataExpedicaoIdentidade || attributes.identidade.dataExpedicaoIdentidade == "__/__/____") {
			errors.push({
				name: 'identidade.identidade',
				message: 'Não foi informado a data de expedição identidade.'
			});
			datas = false;
		} else if(moment().isBefore(moment($("#dataExpedicaoIdentidadeFiador").val(), 'DD/MM/YYYY'))) {
			errors.push({name: 'identidade.identidade', message: 'Data Expedição não pode ser superior à data atual.'});
			datas = false;
		}
		
		if(datas){
			if(moment(attributes.identidade.dataExpedicaoIdentidade, "DD/MM/YYYY").isBefore(moment(attributes.dataNascimento, "DD/MM/YYYY"))){
				errors.push({name: 'identidade.dataExpedicaoIdentidade', message: 'Data Expedição não pode ser inferior à Data Nascimento.'});
			}
		}

		if (attributes.identidade.orgaoExpedidor == null || !attributes.identidade.orgaoExpedidor.codigo) {
			errors.push({
				name: 'identidade.identidade',
				message: 'Não foi informado o orgão expedidor.'
			});
		}

		if (attributes.identidade.uf == null || !attributes.identidade.uf.sigla) {
			errors.push({
				name: 'identidade.identidade',
				message: 'Não foi informado a uf do orgão expedidor.'
			});
		}
		
		if (attributes.emancipado != null && attributes.emancipado.descricao == "S" && (!attributes.emancipado.codigo)) {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o Motivo Emancipação!'
			});
		}

		if (attributes.estadoCivil == null || !attributes.estadoCivil.codigo) {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o Estado Civil!'
			});
		} 
		else if (attributes.estadoCivil.codigo == 1 && isMenor18 && 
				(!(attributes.emancipado == null || !attributes.emancipado.descricao) && attributes.emancipado.descricao == "N")) {
			errors.push({
				message: 'Menor não emancipado!'
			});
		}
		else if (attributes.estadoCivil.codigo == 2 || attributes.estadoCivil.codigo == 9) {
			if (attributes.regimeBens.codigo == "") {
				errors.push({
					message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o Regime Bens!'
				});
			}

			if (!attributes.conjuge.nome || attributes.conjuge.nome == "") {
				errors.push({
					message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o nome do conjuge!'
				});
			}

			if (!attributes.conjuge.cpf || validarCPF(attributes.conjuge.cpf) != '') {
				errors.push({
					message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o CPF do conjuge!'
				});
			}

			if (!attributes.conjuge.dataNascimento || attributes.conjuge.dataNascimento == "") {
				errors.push({
					message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a data Nascimento do conjuge!'
				});
			} else {
				var dataMenos16Conjuge = new Date(obterAnoAtual() - 16, obterMesAtual(), obterDiaAtual());
				var dataNascimentoConjuge = $caixa.data.converteStrToData(attributes.conjuge.dataNascimento);
				if (dataNascimentoConjuge > dataMenos16Conjuge) {
					errors.push({
						name: 'dataNascimento',
						message: 'A data de nascimento do conjuge não pode ser maior ' + formatDate(dataMenos16Conjuge, "dd/MM/yyyy")
					});
				}
			}

			if (!attributes.conjuge.identidade.identidade) {
				errors.push({
					name: 'identidade.identidade',
					message: 'Não foi informado a identidade do conjuge.'
				});
			}

			if (!attributes.conjuge.identidade.dataExpedicaoIdentidade || attributes.conjuge.identidade.dataExpedicaoIdentidade == "__/__/____") {
				errors.push({
					name: 'identidade.identidade',
					message: 'Não foi informado a data de expedição identidade do conjuge.'
				});
			} else if(moment().isBefore(moment(attributes.conjuge.identidade.dataExpedicaoIdentidade, 'DD/MM/YYYY'))) {
				errors.push({name: 'identidade.identidade', message: 'Data Expedição não pode ser superior à data atual.'});
			}

			if (attributes.conjuge.identidade.orgaoExpedidor == null || !attributes.conjuge.identidade.orgaoExpedidor.codigo) {
				errors.push({
					name: 'identidade.identidade',
					message: 'Não foi informado o orgão expedidor do conjuge.'
				});
			}

			if (attributes.conjuge.identidade.uf == null || !attributes.conjuge.identidade.uf.sigla) {
				errors.push({
					name: 'identidade.identidade',
					message: 'Não foi informado a uf do orgão expedidor do conjuge.'
				});
			}
		}
		attributes.valorRendaMensal = mascaraMoeda(Number(attributes.valorRendaMensal.toString()));

		if (parseFloat(attributes.valorRendaMensal) <= 0) {
			errors.push({
				name: 'valorRendaMensal',
				message: 'Não foi informado o valor da renda mensal'
			});

		}

		if (!attributes.emancipado.descricao) {
			if (attributes.emancipado.codigo == "")
				attributes.emancipado.descricao = "N";
		} else {
			if (attributes.emancipado.descricao == "S" && attributes.emancipado.codigo == "") {
				errors.push({
					message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o tipo da emancipação!'
				});
			}
		}

		if (!attributes.codigoProfissao || $("#profissao").val() == "") {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a profissão!'
			});
		}

		if (!attributes.endereco.endereco) {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o endereço!'
			});
		}

		if (!attributes.endereco.bairro) {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o bairro!'
			});
		}

		if (!attributes.endereco.cep || attributes.endereco.cep == "" || attributes.endereco.cep == "00000000") {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o CEP!'
			});
		}

		if (!attributes.endereco.cidade.codigoCidade || attributes.endereco.cidade.codigoCidade == "") {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a cidade!'
			});
		}

		if (!attributes.endereco.cidade.uf.sigla) {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a UF!'
			});
		}

		if (!attributes.contato.telefoneResidencial.ddd || attributes.contato.telefoneResidencial.ddd == ""
			 || attributes.contato.telefoneResidencial.ddd == "0" || attributes.contato.telefoneResidencial.ddd == "00") {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a ddd do telefone residencial!'
			});
		}

		if (!attributes.contato.telefoneResidencial.numero || attributes.contato.telefoneResidencial.numero == ""
			 || attributes.contato.telefoneResidencial.numero == "00000000") {
			errors.push({
				message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar a número do telefone residencial!'
			});
		}

		return errors.length > 0 ? errors : false;
	},

	salvar: function() {
		this.url = "../fes-web/emprest/fiador/salva";
		console.log("call -> Fiador -> salvar");
		return this.save(null);
	},

	excluir: function() {
		this.url = "../fes-web/emprest/fiador/excluir";
		console.log("call -> Fiador -> excluir");
		return this.save(null);
	}

});
//# sourceURL=Fiador.js	
