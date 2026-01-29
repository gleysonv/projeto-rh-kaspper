        mantemFiador: function mantemFiador(e) {
            $
                .when(
                    $
                    .get('../fes-web/servicos/contratofies/manutencaofiador/modelo/ConsultaFiador.js'),
                    $
                    .get('../fes-web/servicos/contratofies/manutencaofiador/modelo/FiadorConsulta.js')

                )
                .done(
                    function() {
                        $('#container')
                            .html(
                                new ConsultaFiadorControle({
                                    model: new ConsultaFiador()
                                }).el);
                    });
        },
		
		
		            {
                "nome": "Altera&ccedil;&atilde;o e substitui&ccedil;&atilde;o de Fiador",
                "grupo": "Manuten&ccedil;&atilde;o",
                "subgrupo": "",
                "subgrupo2": "",
                "arquivos": "",
                "controle": "",
                "modelo": "",
                "evento": "mantemFiador",
                "perfil": ["FES_GESTOR", "FES_MANUT", "FES_MANUTJUR"]
            },
			
			
			
			
ConsultaFiador			
			/ Model da tela: Consulta de dados do fiador por estudante (CPF / Código FIES / Agência)
// Mantém a URL do serviço já existente.
window.ConsultaFiador = Backbone.Model.extend({

  // endpoint existente no sistema
  urlRoot: '../fes-web/emprest/contrato/consultaDadosFiadorPorEstudante',

  defaults: {
    cpf: '',
    codigoFiesConsulta: '',
    agenciaConsulta: ''
  },

  initialize: function () {
    // garante reset quando o controle chamar model.initialize()
    this.set('cpf', '');
    this.set('codigoFiesConsulta', '');
    this.set('agenciaConsulta', '');
  },

  validate: function (attributes) {
    if (attributes === undefined) return true;

    var errors = [];

    // Pelo fluxo novo: pelo menos CPF OU Código FIES
    if (attributes.cpf === '' && (attributes.codigoFiesConsulta === '' || attributes.codigoFiesConsulta === '0.000')) {
      errors.push({ message: 'Informe o código FIES ou o CPF do estudante.' });
    } else if (purificaAtributo(attributes.cpf) !== '') {
      var msg = validarCPF(attributes.cpf);
      if (msg !== '') errors.push({ message: msg });
    }

    return errors.length > 0 ? errors : false;
  },

  buscar: function () {
    return this.fetch({
      type: 'GET',
      contentType: 'application/json',
      data: $.param({
        cpf: this.attributes.cpf,
        codigoFiesConsulta: purificaAtributo(this.attributes.codigoFiesConsulta),
        agenciaConsulta: purificaAtributo(this.attributes.agenciaConsulta)
      })
    });
  }
});

//# sourceURL=ConsultaFiador.js



FiadorConsulta.js


// Model auxiliar (linha do resultado)
window.FiadorConsulta = Backbone.Model.extend({
  defaults: {
    numeroContrato: '',
    cpfFiador: '',
    nomeFiador: '',
    dataNascimento: ''
  }
});

//# sourceURL=FiadorConsulta.js
