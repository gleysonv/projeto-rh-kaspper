if (retorno && retorno.codigo > 0) {
    mostrarErrors([{
      message: retorno.mensagem || 'Falha ao excluir o fiador.'
    }]);
    return;
}

$tr.remove();

mostrarSucessos([{
  message: (retorno && retorno.mensagem) ? retorno.mensagem : 'Comando realizado com sucesso!'
}]);
