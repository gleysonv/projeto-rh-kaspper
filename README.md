<div class="control-group">
  <label class="control-label" for="dependentesFiador">
    <span class="obrigatorio">*</span> nº de Dependentes:
  </label>
  <div class="controls">
    <input type="text"
           id="dependentesFiador"
           name="dependentes"
           class="input-small number"
           maxlength="2"
           value="<%= (typeof dependentes !== 'undefined' && dependentes !== null) ? dependentes : '' %>" />
  </div>
</div>
