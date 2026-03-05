a minha ficou assim 

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
	<li class="active">Alteração/Subistituicão de Fiador .</li>
</ul>

<h1>Informa&ccedil;&otilde;es  do Estudante</h1>


<form class="form-horizontal" id="formFiltroConsulta">

	<div class="row-fluid noprint" id="divConsultar">
		<div class="span6">
				<div class="control-group">
					<label class="control-label" for="cpf"> CPF: </label>
					<div class="controls">												
						<input type="text" id="cpf" name="cpf" class="input-medium cpf" maxlength="14">
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="codigoFiesConsulta">C&oacute;digo Fies: </label>
					<div class="controls">												
						<input type="text" id="codigoFiesConsulta" name="codigoFiesConsulta" class="input-medium fies" maxlength="11">
						
						<a href="#" class="btn btn-primary" title="Localizar" id="btnLocalizarCodFies">
						<i class="icon-search icon-white"></i>
							Localizar C&oacute;d. Fies
					</a>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="codigoFiesConsulta">Agência: </label>
					<div class="controls">
						<input type="text" id="agenciaConsulta" name="agenciaConsulta" class="input-medium fies" maxlength="11">
					</div>
				</div>


		  </div>
	</div>	
		<br>
		<div class="form-actions noprint" >
			<a href="#" class="btn btn-primary" title="Consultar" id="btnConsultar">
				<i class="icon-search icon-white"></i>
				Consultar
			</a>
			<a href="#" class="btn btn-limpar" title="Limpar" id="btnLimpar">Limpar</a>
			<a href="#" class="btn btn-primary" title="Voltar" id="btnVoltar">Voltar</a>
		</div>

	<!-- Resultado da pesquisa de fiadores -->
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

	<!-- Ações só fazem sentido após existir um contrato/estudante consultado -->
	<div class="form-actions" id="divAcoesPosConsulta" style="display:none;">
		<a href="#" class="btn btn-primary" title="Novo Fiador" id="btnNovoFiador">Novo Fiador</a>
		<a href="#" class="btn" title="Sair" id="btnSair">Sair</a>
	</div>
		
</form>


<!-- Modal Incluir -->
<div id="divModalIncluir"></div>
	

copiei dessa aqui.>>

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
	<li class="active">Altera Semestre</li>
</ul>

<h1>Informa&ccedil;&otilde;es  do Estudante</h1>


<form class="form-horizontal" id="formFiltroConsulta">

	<div class="row-fluid noprint" id="divConsultar">
		<div class="span6">
				<div class="control-group">
					<label class="control-label" for="selectSemestre"><span class="obrigatorio">*</span>Semestre/Ano: </label>
					<div class="controls">												
						<select id="selectSemestre" name="semestre" style="width: 100px"></select>
						<span> / </span> 
						<select id="selectAno" name="ano" validators="required" style="width: 100px"></select>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label" for="cpf"> CPF: </label>
					<div class="controls">												
						<input type="text" id="cpf" name="cpf" class="input-medium cpf" maxlength="14">
					</div>
				</div>
				
				<div class="control-group">
					<label class="control-label" for="codigoFiesConsulta">C&oacute;digo Fies: </label>
					<div class="controls">												
						<input type="text" id="codigoFiesConsulta" name="codigoFiesConsulta" class="input-medium fies" maxlength="11">
						
						<a href="#" class="btn btn-primary" title="Localizar" id="btnLocalizarCodFies">
						<i class="icon-search icon-white"></i>
							Localizar C&oacute;d. Fies
					</a>
					</div>
				</div>
		  </div>
	</div>	
		<br>
		<div class="form-actions noprint" >
			<a href="#" class="btn btn-primary" title="Consultar" id="btnConsultar">
				<i class="icon-search icon-white"></i>
				Consultar
			</a>
			<a href="#" class="btn btn-limpar" title="Limpar" id="btnLimpar">Limpar</a>
			<a href="#" class="btn btn-primary" title="Voltar" id="btnVoltar">Voltar</a>
		</div>
	
	<div class="row-fluid" id="divResultado">
		<div class="span6">
				<div class="control-group">
	                    <label class="control-label" for="codigoFies">C&oacute;digo Fies:</label>
	                	<div class="controls resultado">
							<span id="codigoFies"></span>
					    </div>
				</div>
				
				<div class="control-group">
                    <label class="control-label" for="cpf"> CPF:</label>
                     <div class="controls resultado">
							<span id="cpfEstudade"></span>
					</div>
                </div>
                
                <div class="control-group">
	            	    <label class="control-label" for="unidadeAdmCampus">Unidadade Admistrativa/Campus:</label>
	                   	     <div class="controls resultado">
								<span id="unidadeAdmCampus"></span>
						    </div>
	             </div>
	             
	             <div class="control-group">
						<label for="areaEspecifica" class="control-label">&Aacute;rea Espec&iacute;fica:</label>
						   <div class="controls resultado">
								<span id="areaEspecifica"></span>
						   </div>
				</div>
				
				<div class="control-group">
		                    <label class="control-label" for="contratoSIAPI">Contrato SIAPI:</label>
		                  	<div class="controls resultado">
									<span id="contratoSIAPI"></span>
							</div>
		         </div>
		
		</div>
		
		<div class="span6 pull-right">
				<div class="control-group">
	            	    <label class="control-label" for="cpf"> Nome do Estudante:</label>
	                    <div class="controls resultado">
							<span id="nomeEstudade"></span>
						</div>
	            </div>
	            
	            <div class="control-group">
            	    <label class="control-label" for="codigoIESUF">I.E.S/UF:</label>
                   	 <div class="controls resultado">
							<span id="codigoIESUF"></span>
						</div>
                </div>
                
                <div class="control-group">
						<label for="areGeral" class="control-label">&Aacute;rea Geral:</label>
							<div class="controls resultado">
								<span id="areGeral"></span>
						    </div>
				</div>
				
				
				<div class="control-group">
	                    <label class="control-label" for="cursoTurno">Curso/Turno:</label>
							<div class="controls resultado">
								<span id="cursoTurno"></span>
							</div>
				 </div>
				 
				  <div class="control-group">
		                    <label class="control-label obrigatorio" for="novoSemestre">Informe em qual semestre o estudante est&aacute; matriculado <br/>no curso
Portugu&ecirc;s/Ingl&ecirc;s e Respectivas Literaturas <br/>(Matutino), em 2&#186;/2014. </label>
								<div class="input-prepend" style="padding-left: 15px;">
								<input type="text" id="novoSemestre"  class="input-small text-left numero" maxlength="4" value=""/>
								</div>/
								<div class="input-prepend">
								<input type="text" id="ultimoSemestre" disabled="disabled"	class="input-small text-left numero" maxlength="4" />
								</div>
				</div>
				 
	          </div>
	         <div class="form-actions" >
					<a href="#" class="btn btn-primary" title="Continuar" id="btnSalvar">Continuar</a>
			  </div> 		
				
		</div>
		
</form>


<!-- Modal Incluir -->
<div id="divModalIncluir"></div>
	
