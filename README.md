window.FiadorControle = Backbone.View.extend({
    modelUF: null,
    modelExpedidor: null,
    estadosCivis : [2, 3, 4, 5, 6, 7, 9],

    // ✅ NOVO: adiciona cliques SEM REMOVER o que já existe
    events: {
        'blur input': 'updateModel',
        'change select': 'updateModel',
        "changeDate .data": "changeDate2",

        "click a#btnLocalizarCodFies": "localizarCodFies",
        "click a#btnConsultar": "consultar",
        "click a#btnLimpar": "limparTela",
        "click a#btnVoltar": "voltar"
    },

    serialize: function() {
        return {
            title: this.$(".title").text(),
            start: this.$(".start-page").text(),
            end: this.$(".end-page").text()
        };
    },

    getScriptLog: function (url){
        return $.getScript(url).fail(function (xhr, status, err){
             console.error('[getScript FAIL', url, status, err);
        });
    },

    initialize: function() {
        var that = this;
        console.log("initialize FiadorControle");
        removerMensagens();

        // ✅ NOVO: carrega o modal/Model usados pelo botão "Localizar Cód. Fies"
        // (não interfere no resto; só garante que a função exista)
        that.getScriptLog('../fes-web/servicos/contratofies/consultagenericaestudante/controle/ConsultaGenericaEstudanteModalControle.js');
        that.getScriptLog('../fes-web/servicos/contratofies/consultagenericaestudante/modelo/Estudante.js');

        //Carregar models dependencia
        $.when(
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/EstadoCivil.js'),
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/RegimeBens.js'),
            that.getScriptLog('../fes-web/servicos/cadastro/modelo/Emancipado.js')
        ).done(function() {

            //carregar coleções
            $.when(
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/OrgaoExpedidorColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/EstadoCivilColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/RegimeBensColecao.js'),
                that.getScriptLog('../fes-web/servicos/cadastro/modelo/EmancipadoColecao.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorListControle.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorEnderecoListControle.js'),
                that.getScriptLog('../fes-web/servicos/contratofies/manutencaofiador/controle/FiadorContatoListControle.js')
            ).done(function() {

               $.get('../fes-web/servicos/contratofies/manutencaofiador/visao/Fiador.html').done(function(data) {
                 that.template = _.template(data);
                 that.render();
                 loadMask();

                 setTimeout(function () {
                    $('#ajaxStatus').modal('hide');
                 }, 1000);
               });

            }).fail(function (){
                console.error('Erro ao carregar coleções do Fiador');
            });
        }).fail(function (){
           console.error('Erro ao carregar dependencia do Fiador');
        });

    },

    changeDate2: function (e) {
        console.log("call -> ConsultaGenericaControle -> changeDate2");
        console.log($('input', e.target).attr('name'));

        this.model.set($('input', e.target).attr('name'), $('input', e.target).val());
        console.log(this.model);
    },

    // ✅ NOVO: botão "Localizar Cód. Fies"
    localizarCodFies: function localizarCodFies(e) {
        if (e) e.preventDefault();
        removerMensagens();

        // Se esses divs existirem na sua nova tela, ótimo; se não existirem, não quebra
        try { $('#divResultado').hide(); } catch(ex) {}
        try { $('#divTabelaFiadores').hide(); } catch(ex) {}
        try { $('#divAcoesPosConsulta').hide(); } catch(ex) {}

        if (typeof ConsultaGenericaEstudanteModalControle === "undefined" || typeof Estudante === "undefined") {
            return mostrarErrors([{ message: 'Dependências do modal não carregadas (ConsultaGenericaEstudanteModalControle / Estudante).' }]);
        }

        this.detalhe = new ConsultaGenericaEstudanteModalControle({
            el: $('#divModalIncluir'),
            model: new Estudante(),
            modelAnterior: this.model
        });
    },

    // ✅ NOVO: consulta simples de fiadores por código FIES
    consultar: function consultar(e) {
        if (e) e.preventDefault();
        removerMensagens();

        var codigoFies = purificaAtributo($('#codigoFiesConsulta').val());
        var cpf = purificaAtributo($('#cpf').val());

        if (!codigoFies) {
            return mostrarErrors([{ message: 'Informe o Código Fies.' }]);
        }

        // Se você criou os spans do divResultado, preenche o mínimo
        try {
            $('#codigoFies').text(mascararCodigoFies(codigoFies));
            if ($('#cpfEstudante').length) {
                $('#cpfEstudante').text(cpf ? mascararCpf(cpf) : '-');
            }
            $('#divResultado').show('slow');
        } catch(ex) {}

        $('#ajaxStatus').modal('show');

        // ⚠️ Ajuste aqui se o seu endpoint tiver outro contexto
        var url = '../fes-rest/fiador/consultaFiadores';

        var that = this;
        $.ajax({
            url: url,
            method: 'GET',
            dataType: 'json',
            data: { codigoFies: codigoFies }
        })
        .done(function(fiadores) {
            that.renderTabelaFiadores(fiadores);

            try { $('#divTabelaFiadores').show('slow'); } catch(ex) {}
            try { $('#divAcoesPosConsulta').show('slow'); } catch(ex) {}

            $('#ajaxStatus').modal('hide');
        })
        .fail(function(xhr) {
            console.log('Erro ao consultar fiadores:', xhr);
            $('#ajaxStatus').modal('hide');
            mostrarErrors([{ message: 'Ocorreu um erro ao consultar os fiadores. Verifique o endpoint.' }]);
        });
    },

    // ✅ NOVO: render da tabela (não interfere no resto)
    renderTabelaFiadores: function renderTabelaFiadores(fiadores) {
        var $tbody = $('#tbResultadoFiadores tbody');
        if (!$tbody.length) {
            // se ainda não tem a tabela na página, não quebra
            console.warn('Tabela #tbResultadoFiadores não encontrada no DOM.');
            return;
        }

        $tbody.empty();

        if (!fiadores || fiadores.length === 0) {
            $tbody.append('<tr><td colspan="5" style="text-align:center;">Nenhum fiador encontrado.</td></tr>');
            return;
        }

        for (var i = 0; i < fiadores.length; i++) {
            var f = fiadores[i];

            // ⚠️ Ajuste os nomes conforme o JSON real do seu backend
            var nrContrato = f.numeroContrato || f.nrContrato || f.contrato || '';
            var cpfFiador  = f.cpf || f.cpfFiador || '';
            var nomeFiador = f.nome || f.nomeFiador || '';
            var dtNasc     = f.dataNascimento || f.dtNascimento || '';

            var acoes = '<a href="#" class="btn btn-mini btn-primary">Alterar</a>';

            $tbody.append(
                '<tr>' +
                    '<td>' + nrContrato + '</td>' +
                    '<td>' + cpfFiador + '</td>' +
                    '<td>' + nomeFiador + '</td>' +
                    '<td>' + dtNasc + '</td>' +
                    '<td>' + acoes + '</td>' +
                '</tr>'
            );
        }
    },

    // ✅ NOVO: limpar da tela de consulta (não mexe no seu updateModel)
    limparTela: function limparTela(e) {
        if (e) e.preventDefault();
        removerMensagens();

        try { limparFormulario('#formFiltroConsulta'); } catch(ex) {}

        var $tbody = $('#tbResultadoFiadores tbody');
        if ($tbody.length) $tbody.empty();

        try { $('#divResultado').hide('slow'); } catch(ex) {}
        try { $('#divTabelaFiadores').hide('slow'); } catch(ex) {}
        try { $('#divAcoesPosConsulta').hide('slow'); } catch(ex) {}
    },

    // ✅ NOVO: voltar (igual padrão)
    voltar: function voltar(e) {
        if (e) e.preventDefault();
        abrirPagina('../fes-web/fes-index.html');
    },

    render: function() {
        var _this2;

        try {
            _this2 = this.model.toJSON();
        } catch (e) {
            _this2 = this.model;
        }

        $(this.el).html(this.template(_this2));

        // ... (SEU render INTEIRO continua exatamente como está)
        // (não vou repetir tudo aqui pra não bagunçar o que você já tem)

        // ⚠️ MANTENHA TODO O SEU render original abaixo dessa linha
        // (cole o patch acima no seu arquivo; não precisa reescrever o render)

        window.setTimeout(function() {
            loadMask();
        }, 500);

        return this;
    },

    updateModel: function(el) {
        // ... (SEU updateModel continua exatamente como está)
        // (sem alterações)
    },

    hideErrors: function() {
        $('#msgModal').html("");
    }
});
