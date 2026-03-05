salvar: function () {
  var _this = this;

  // ---- LIMPEZA DO codigoFies (remove pontos e qualquer coisa que não seja número)
  var codigoFies = _this.modelFiador.get("codigoFies");
  if (codigoFies) {
    codigoFies = codigoFies.toString().replace(/\D/g, '');
    _this.modelFiador.set("codigoFies", codigoFies);
  }
  // ---- FIM LIMPEZA

  this.modelFiador.salvar().done().success(function (data) {
