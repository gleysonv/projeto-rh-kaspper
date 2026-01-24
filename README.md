

carregarDependencias: function (callbackOk) {
  var scripts = [
    '../fes-web/servicos/administracao/cadastrofiadorcontrato/modelo/OrgaoExpedidor.js',
    '../fes-web/servicos/administracao/cadastrofiadorcontrato/colecao/OrgaoExpedidorColecao.js',

    '../fes-web/servicos/administracao/cadastrofiadorcontrato/modelo/UF.js',
    '../fes-web/servicos/administracao/cadastrofiadorcontrato/colecao/UFColecao.js'

    // se precisar, adiciona aqui:
    // '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorConjugeControle.js',
    // '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorEnderecoControle.js',
    // '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorContatoControle.js'
  ];

  function carregarSeq(i) {
    if (i >= scripts.length) {
      if (callbackOk) callbackOk();
      return;
    }
    $.getScript(scripts[i])
      .done(function () { carregarSeq(i + 1); })
      .fail(function () {
        console.error('Falha ao carregar script:', scripts[i]);
      });
  }

  carregarSeq(0);
},








initialize: function() {
  var that = this;
  console.log("initialize FiadorControle");

  this.carregarDependencias(function () {
    $.get('../fes-web/servicos/administracao/cadastrofiadorcontrato/visao/FiadorModal.html')
      .done(function(data) {
        that.template = _.template(data);
        that.render();
      });
  });
},
