console.log("bind cancelar delegado");
$(document).off("click", "#btnCancelarFiadorModal").on("click", "#btnCancelarFiadorModal", function (e) {
    console.log("clicou cancelar");
    e.preventDefault();

    _this.modelFiador.set(JSON.parse(_this._cloneFiadorJsonString || '{}'));
    _this.voltarParaConsulta();
});
