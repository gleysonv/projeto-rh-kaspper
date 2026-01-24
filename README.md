window.FiadorLoader = {
  carregar: function (opts) {
    opts = opts || {};

    // 1) lista de scripts que PRECISAM existir antes do render
    var scripts = [
      // libs/base (se já estiverem carregadas globalmente, pode remover)
      // '../fes-web/js/underscore.js',
      // '../fes-web/js/backbone.js',

      // models/coleções que o render usa
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/modelo/OrgaoExpedidor.js',
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/colecao/OrgaoExpedidorColecao.js',

      '../fes-web/servicos/administracao/cadastrofiadorcontrato/modelo/UF.js',
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/colecao/UFColecao.js',

      // views/controllers das abas (ajuste para os nomes reais no seu projeto)
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorControle.js',
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorConjugeControle.js',
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorEnderecoControle.js',
      '../fes-web/servicos/administracao/cadastrofiadorcontrato/controle/FiadorContatoControle.js'
    ];

    // 2) carrega em sequência (ordem importa)
    function carregarSequencial(i) {
      if (i >= scripts.length) {
        // 3) aqui já está tudo carregado -> typeof deve ser "function"
        // console.log('typeof OrgaoExpedidorColecao', typeof OrgaoExpedidorColecao);

        // instancia a tela
        return new FiadorControle(opts).render();
      }

      return $.getScript(scripts[i])
        .done(function () { carregarSequencial(i + 1); })
        .fail(function () {
          console.error('Falha ao carregar:', scripts[i]);
        });
    }

    carregarSequencial(0);
  }
};
