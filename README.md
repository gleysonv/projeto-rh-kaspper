buscar: function () {
  var cpf = purificaAtributo(this.attributes.cpf);
  var codigoFies = purificaAtributo(this.attributes.codigoFiesConsulta); // aqui é o input do form

  var params = {};
  if (cpf) params.cpf = cpf;
  if (codigoFies) params.codigoFies = codigoFies;   // <-- NOME QUE O BACKEND ESPERA

  return this.fetch({
    type: 'GET',
    data: $.param(params),
    cache: false
  });
}
