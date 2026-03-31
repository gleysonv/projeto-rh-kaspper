<ul class="breadcrumb">
	<li><a href="index.html">In&iacute;cio</a> <span class="divider">&gt;</span></li>
	<li>
		Contrato FIES
		<span class="divider">&gt;</span>
	</li>
	<li>
		Manuten&ccedil;&atilde;o
		<span class="divider">&gt;</span>
	</li>
	<li class="active">Altera&ccedil;&atilde;o/Substitui&ccedil;&atilde;o de Fiador</li>
</ul>

<h1>Informa&ccedil;&otilde;es do Estudante</h1>

<form class="form-horizontal" id="formFiltroConsulta">

	<div class="row-fluid noprint" id="divConsultar">
		<div class="span6">
			<div class="control-group">
				<label class="control-label" for="cpf">CPF:</label>
				<div class="controls">
					<input type="text" id="cpf" name="cpf" class="input-medium cpf" maxlength="14">
				</div>
			</div>

			<div class="control-group">
				<label class="control-label" for="codigoFiesConsulta">C&oacute;digo Fies:</label>
				<div class="controls">
					<input type="text" id="codigoFiesConsulta" name="codigoFiesConsulta" class="input-medium fies" maxlength="11">

					<a href="#" class="btn btn-primary" title="Localizar" id="btnLocalizarCodFies">
						<i class="icon-search icon-white"></i>
						Localizar C&oacute;d. Fies
					</a>
				</div>
			</div>

			<div class="control-group">
				<label class="control-label" for="agenciaConsulta">
					<span class="obrigatorio">*</span> Ag&ecirc;ncia:
				</label>
				<div class="controls">
					<input type="text" id="agenciaConsulta" name="agenciaConsulta" class="input-medium" maxlength="11">
				</div>
			</div>
		</div>
	</div>

	<br>

	<div class="form-actions noprint">
		<a href="#" class="btn btn-primary" title="Consultar" id="btnConsultar">
			<i class="icon-search icon-white"></i>
			Consultar
		</a>
		<a href="#" class="btn btn-limpar" title="Limpar" id="btnLimpar">Limpar</a>
		<a href="#" class="btn btn-primary" title="Voltar" id="btnVoltar">Voltar</a>
	</div>

	<div class="span12" style="margin-top:10px;">
		<table class="table table-bordered table-striped" id="tbResultadoFiadores">
			<caption>Lista de fiadores vinculados ao contrato FIES consultado</caption>
			<thead>
				<tr>
					<th>N&ordm; Contrato</th>
					<th>CPF Fiador</th>
					<th>Nome Fiador</th>
					<th>Data Nasc.</th>
					<th>A&ccedil;&otilde;es</th>
				</tr>
			</thead>
			<tbody></tbody>
		</table>
	</div>

	<div class="form-actions" id="divAcoesPosConsulta" style="display:none;">
		<a href="#" class="btn btn-primary" title="Novo Fiador" id="btnNovoFiador">Novo Fiador</a>
		<a href="#" class="btn" title="Sair" id="btnSair">Sair</a>
	</div>
</form>

<div id="divModalIncluir"></div>
