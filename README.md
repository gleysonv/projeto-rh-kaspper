var CadastroAlteraSemestreControle = Backbone.View.extend({

	urlTemplate : '../fes-web/servicos/contratofies/alterasemestre/visao/CadastroAlteraSemestre.html',
	
    initialize : function initialize () {
    	$.get('../fes-web/servicos/contratofies/consultagenericaestudante/controle/ConsultaGenericaEstudanteModalControle.js');
    	$.get('../fes-web/servicos/contratofies/consultagenericaestudante/modelo/Estudante.js');
    	var that = this;
    	removerMensagens();
    	
    	$.get(this.urlTemplate)
		.done(function(data) {
			that.template = _.template(data);
			that.render();
			loadMask();
			removeAutoComplete('cpf', 'cpf');
		});
    	
    },
	
    events : {
    	"click a#btnSalvar" : "salvar",
		"click a#btnIncluir" : "incluir",
		"click a#btnLimpar" : "limpar",
		"click a#btnConsultar" : "consultar",
		"click a#btnVoltar" : "voltar",
		"click a#btnLocalizarCodFies" : "localizarCodFies",
		"blur #formFiltroConsulta input" : "change",
		"change #formFiltroConsulta select" : "change",
		'blur #cpf' : 'validarCpf'
		
	},

	validarCpf : function validarCpf(e) {
		removerMensagens();
		
		var cpf = purificaAtributo(e.target.value);
		
		if(cpf != '') {
			var msg =  validarCPF(cpf);
			
			if (msg != '') {
				return mostrarErrors([{message: msg}]);
			}
		}
		return true;
	},  
	
	change : function change(e) {
		var change = {};
		change[e.target.name] = e.target.value;
		this.model.set(change);
	},
	
    limpar : function limpar() {
    	removerMensagens();
    	limparFormulario('#formFiltroConsulta');
    	this.model.initialize();
    	$('#divResultado').hide('slow');
	},
    
    localizarCodFies : function localizarCodFies() {
    	$('#divResultado').hide();
    	this.detalhe = new ConsultaGenericaEstudanteModalControle({el : $('#divModalIncluir'), model : new Estudante(), modelAnterior : this.model });
    },
	
    consultar : function consultar() {
    	$('#divResultado').hide('slow');
    	removerMensagens();
    	this.model.set('cpf', purificaAtributo(this.model.get('cpf')));
    	
		var that = this;
		
		if (this.validar()) {
			$('#ajaxStatus').modal('show');
			this.model.buscar()
			.done(function sucesso(data) {
				if (data.mensagem != "" && data.mensagem != null) {
					$('#ajaxStatus').modal('hide');	
					return mostrarErrors([{message: data.mensagem}]);
				}
				that.renderResult(data);
				$('#divResultado').show('slow');
			}).error(function erro(e) {
				console.log(e);
				mostrarErrors([{message: 'Ocorreu um erro na consulta, tente novamente!'}]);
				$('#ajaxStatus').modal('hide');
			});
		}else
		
		$('#ajaxStatus').modal('hide');	
	},
	
	renderResult : function renderResult(_data) {
		if (_data != null && _data.dadosCandidato != null) {
			$("#codigoFies").text(mascararCodigoFies(_data.dadosCandidato.nuCandidato));
			$("#cpfEstudade").text(mascararCpf(_data.consultaPreAdit.coCpf));
			$("#unidadeAdmCampus").text(_data.cursoHabilitacao.noCampus);
			$("#areaEspecifica").text(_data.cursoHabilitacao.noAreaEspecifica);
			$("#contratoSIAPI").text(_data.nuContratoFormatado);
			$("#codigoIESUF").text(_data.cursoHabilitacao.noIes +'/'+_data.consultaPreAdit.ufIEs);
			$("#cursoTurno").text(_data.cursoHabilitacao.noCurso +''+_data.cursoHabilitacao.deTurno+'');
			$("#nomeEstudade").text(_data.consultaPreAdit.noCandidato);
			$("#areGeral").text(_data.cursoHabilitacao.noAreaGeral);
			$('#ultimoSemestre').val(_data.consultaPreAdit.nuMinSemestre);
		}
			
    	$('#ajaxStatus').modal('hide');
	},

	validar : function validar() {
		if (!this.model.isValid()){
			mostrarErrors(this.model.validationError);
			return false;
		}
		
		return true;
	},
	voltar : function voltar() {
    	abrirPagina('../fes-web/fes-index.html');
    },
    
    salvar : function salvar () {
    	  var that = this;
    	  if($("#novoSemestre").val() ==''||$("#novoSemestre").val()== null){
    			mostrarErrors([{message: 'Não foi informado um dos parâmetros obrigatórios, por favor informar o semestre atual!'}]);
    			return false;
    	  }else{
    		  if($("#novoSemestre").val() > $("#ultimoSemestre").val()){
    			 mostrarErrors([{message: 'Prazo informado maior que o previsto para conclusão do curso.'}]);
      			return false;
    		  }  
    		  if($("#novoSemestre").val() < 1){
     			 mostrarErrors([{message: 'Este Campo não pode ser MENOR ou IGUAL a 0.'}]);
       			 return false;
     		  }  
    	  }
    	  
    	  var semestre = new Semestre();
		  semestre.set('novoSemestre', $("#novoSemestre").val());
		  semestre.set('ano',  that.model.attributes.ano);
		  semestre.set('semestre', that.model.attributes.semestre);
		  semestre.set('codigoFies', purificaAtributo(that.model.attributes.dadosCandidato.nuCandidato));
		
		 
		  mensagemConfirmacao('Tem certeza da alteração?\n\nCaso seja confirmada a quantidade errada, uma nova correção'+
    			              ' somente será possível até o término do prazo para ADITAMENTO e desde que o contrato não tenha'+
    			              ' sido confirmado pela IES e a CAIXA, e a RM ou Termo de Anuência não tenham sido impressos.\n\nDeseja realmente alterar? '+
    			              ' Se sim clique no botão Confirmar.','Cancelar','Confirmar',
    				function() {
    					$fes.post('../fes-web/emprest/contrato/confirmaAlteracaoSemestre', semestre, function sucesso(data){
    						if (data.tipo == 'alert-error') {
        		    			mostrarErrors([{message: data.mensagem}], '#messageContainerModal', data.tipo);
        		    		} else {
        		    			mostrarErrors([{message: data.mensagem}],  '#messageContainer','alert-success');
        		    			$('#divResultado').hide();
        		    		}
    					}).error(function(data) {
    						errors.push({message : 'Ocorreu um erro na consulta, tente novamente!'});
    					}); 

    						
    			});
    },
	
    render : function render () {
    	$(this.el).html(this.template(this.model.toJSON()));
    	//console.log('altera semestre RENDER');
    	
    	gCarregarComboSemestre('#selectSemestre', '');
    	gCarregarComboAnos('#selectAno', '');
    	
    	this.model.set('ano', this.model.get('ano') == 0 ? obterAnoAtual() : this.model.get('ano'));
    	this.model.set('semestre', this.model.get('semestre') == 0 ? obterMesAtual() : this.model.get('semestre'));
    	$('#divResultado').hide();

    	return this;

    }
    
});

//# sourceURL=CadastroAlteraSemestreControle.js
