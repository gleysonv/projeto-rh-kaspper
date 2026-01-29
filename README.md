mantemFiador: function mantemFiador(e) {
  try { console.log('[MENU] mantemFiador acionado'); } catch (ex) {}

  $.when(
    $.get('../fes-web/servicos/contratofies/manutencaofiador/modelo/ConsultaFiador.js'),
    $.get('../fes-web/servicos/contratofies/manutencaofiador/modelo/FiadorConsulta.js'),
    $.get('../fes-web/servicos/contratofies/manutencaofiador/controle/ConsultaFiadorControle.js') // <-- FALTAVA
  )
  .done(function () {
    try {
      console.log('[MENU] deps carregadas',
        typeof window.ConsultaFiador,
        typeof window.ConsultaFiadorControle
      );

      $('#container').html(
        new ConsultaFiadorControle({
          model: new ConsultaFiador()
        }).el
      );
    } catch (err) {
      console.error('[MENU] erro ao montar tela mantemFiador:', err);
      alert('Erro ao abrir Manutenção de Fiador. Veja o console.');
    }
  })
  .fail(function (xhr, status, err) {
    console.error('[MENU] falha ao carregar dependências:', status, err, xhr);
    alert('Falha ao carregar arquivos JS/HTML da Manutenção de Fiador (ver console).');
  });
},
