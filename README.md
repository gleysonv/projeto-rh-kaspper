buscar: function () {
  return this.fetch({
    type: 'GET',
    cache: false,
    dataType: 'json',
    data: {
      codigoFies: this.get('codigoFiesConsulta')
    }
  });
}



// MOCK p/ testar visual
if (!data.fiadores && !data.listaFiadores) { ... }


that.renderTabelaFiadores(data || []);


public Fiador consulta(@QueryParam("codigoFies") Long codigoFies,
                       @QueryParam("cpf") String cpf)
