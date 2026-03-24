window.ConsultaFiador = Backbone.Model.extend({

    // novo endpoint do backend
    urlRoot: '../fes-web/emprest/fiador/consultaFiadoresPorCpfCodFiesAgencia',

    defaults: {
        cpf: '',
        codigoFiesConsulta: '',
        agenciaConsulta: ''
    },

    initialize: function () {
        this.set('cpf', '');
        this.set('codigoFiesConsulta', '');
        this.set('agenciaConsulta', '');
    },

    validate: function (attributes) {
        if (attributes === undefined) {
            return true;
        }

        var errors = [];

        var cpf = purificaAtributo(attributes.cpf || '');
        var codigoFies = purificaAtributo(attributes.codigoFiesConsulta || '');
        var agencia = purificaAtributo(attributes.agenciaConsulta || '');

        // agência obrigatória
        if (!agencia) {
            errors.push({ message: 'Informe a agência.' });
        }

        // exige CPF ou Código FIES
        if (!cpf && !codigoFies) {
            errors.push({ message: 'Informe o CPF ou o Código FIES.' });
        }

        // valida CPF somente se ele tiver sido informado
        if (cpf) {
            var msg = validarCPF(cpf);
            if (msg !== '') {
                errors.push({ message: msg });
            }
        }

        return errors.length > 0 ? errors : false;
    },

    buscar: function () {
        var cpf = purificaAtributo(this.attributes.cpf || '');
        var codigoFies = purificaAtributo(this.attributes.codigoFiesConsulta || '');
        var agencia = purificaAtributo(this.attributes.agenciaConsulta || '');

        var params = {};

        if (codigoFies) {
            params.codigoFies = codigoFies;
        }

        if (cpf) {
            params.cpf = cpf;
        }

        if (agencia) {
            params.agencia = agencia;
        }

        return this.fetch({
            type: 'GET',
            data: $.param(params),
            cache: false
        });
    }

});
