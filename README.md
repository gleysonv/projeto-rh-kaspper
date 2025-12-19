Os invasores podem controlar o argumento de caminho do sistema de arquivos para get() em ExtracaoDadosBean.java, linha 43, o que permite que eles acessem ou modifiquem arquivos que, de outra forma, estariam protegidos.
Details
Erros de manipulação de caminho ocorrem quando as duas condições a seguir são atendidas:
1. Um invasor pode especificar um caminho usado em uma operação no sistema de arquivos.
2. Ao especificar o recurso, o invasor adquire uma capacidade que, de outra forma, não seria permitida.
Por exemplo, o programa pode dar ao invasor a capacidade de substituir o arquivo especificado ou executá-lo com uma configuração controlada pelo invasor.
Nesse caso, o invasor pode especificar o valor que entra no programa em executeQuery() em ParametroSistemaDAO.java, na linha 58, e esse valor é utilizado para acessar um recurso do sistema de arquivos em get() em ExtracaoDadosBean.java, na linha 43.

Exemplo 1: O código a seguir usa a entrada de uma solicitação HTTP para criar um nome de arquivo. O programador não levou em consideração a possibilidade de que um invasor pudesse fornecer um nome de arquivo, como "../../tomcat/conf/server.xml", que faz com que o aplicativo exclua um dos seus próprios arquivos de configuração.

String rName = request.getParameter("reportName");
File rFile = new File("/usr/local/apfr/reports/" + rName);
...
rFile.delete();

Exemplo 2: O código a seguir usa a entrada de um arquivo de configuração para determinar qual arquivo deve ser aberto e ecoado de volta para o usuário. Se o programa for executado com privilégios adequados, e usuários mal-intencionados puderem alterar o arquivo de configuração, eles poderão usar esse programa para ler qualquer arquivo no sistema que termine com a extensão .txt.

fis = new FileInputStream(cfg.getProperty("sub")+".txt");
amt = fis.read(arr);
out.println(arr);

Algumas pessoas acham que, no ambiente móvel, vulnerabilidades clássicas, como a manipulação de caminhos, não fazem sentido — por que o usuário atacaria a si mesmo? No entanto, lembre-se de que a essência das plataformas móveis são aplicativos baixados de várias fontes e executados lado a lado no mesmo dispositivo. A probabilidade de execução de um malware junto com um aplicativo de banco é alta, o que exige a expansão da superfície de ataque de aplicativos móveis de forma a incluir comunicações entre processos.
Exemplo 3: O código a seguir adapta o Example 1 à plataforma Android.

...
String rName = this.getIntent().getExtras().getString("reportName");
File rFile = getBaseContext().getFileStreamPath(rName);
...
rFile.delete();
...

A melhor maneira de impedir a manipulação de caminho é com um nível de desvio: crie uma lista de valores legítimos dentre os quais o usuário deve selecionar. Com essa abordagem, a entrada fornecida pelo usuário nunca é usada diretamente para especificar o nome do recurso.
Em algumas situações, essa abordagem é impraticável, pois o conjunto de nomes de recursos legítimos é muito grande ou difícil de manter. Nessas situações, os programadores muitas vezes recorrem à aplicação de uma lista de bloqueios. Uma lista de bloqueios é aplicada para fazer a rejeição ou o escape seletivo de caracteres potencialmente perigosos antes de usar a entrada. No entanto, qualquer lista de caracteres não seguros desse tipo tem grandes chances de ser incompleta e quase certamente se tornará desatualizada. Uma abordagem melhor é criar uma lista com os caracteres que podem aparecer no nome do recurso e aceitar entradas formadas exclusivamente com caracteres do conjunto aprovado.
 
"a entrada fornecida pelo usuário nunca é usada diretamente para especificar o nome do recurso."
 



package br.gov.caixa.fes.negocio;

import br.gov.caixa.arqrefcore.excecao.BusinessException;
import br.gov.caixa.arqrefcore.log.Logging;
import br.gov.caixa.fes.dao.BasicDAOOracle;
import br.gov.caixa.fes.dominio.ArquivoExtracaoDados;
import br.gov.caixa.fes.dominio.enumerador.ParametroSistemaEnum;
import br.gov.caixa.fes.interceptor.LoggingInterceptor;
import br.gov.caixa.fes.util.DataUtil;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Stateless
@LocalBean
@Logging
@Interceptors(LoggingInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ExtracaoDadosBean extends BasicDAOOracle implements ExtracaoDadosService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 679316431727340774L;
	@Inject
	private ParametroSistemaService parametro;
	
	@Override
	public List<ArquivoExtracaoDados> consultaArquivosExtracaoDados(Date dataInicial, Date dataFinal) throws BusinessException {
		List<ArquivoExtracaoDados> arquivosExtracaoDados = new ArrayList<>();
		String pathName = buscarPathName();

		Path basePath = Paths.get(pathName).toAbsolutePath().normalize();

		if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
			throw new BusinessException("Pasta não encontrada! Diretório: " + basePath.toString());
		}

		File[] arquivos = basePath.toFile().listFiles();

		if (arquivos == null) {
			return arquivosExtracaoDados;
		}

		File path = new File(pathName);

		for (File arquivo : arquivos) {
			Date dataArquivo = DataUtil.zerarHora(new Date(arquivo.lastModified()));
			
			if(arquivo.isDirectory() || dataArquivo.before(dataInicial) || dataArquivo.after(dataFinal)) {
				continue;
			}			
			
			ArquivoExtracaoDados arquivoExtracaoDados = new ArquivoExtracaoDados();
			arquivoExtracaoDados.setNome(arquivo.getName());
			arquivoExtracaoDados.setDataModificacao(DataUtil.formatar(new Date(arquivo.lastModified()), DataUtil.PADRAO_DATA_HORA_COMPLETA));
			arquivoExtracaoDados.setTotalBytes(arquivo.length());

			arquivosExtracaoDados.add(arquivoExtracaoDados);
		}
		
		Collections.sort(arquivosExtracaoDados, new Comparator<ArquivoExtracaoDados>() {
			
	        @Override
	        public int compare(ArquivoExtracaoDados  arquivo1, ArquivoExtracaoDados arquivo2){
	            return  arquivo1.getDataModificacao().compareTo(arquivo2.getDataModificacao());
	        }
			
		});		

		return arquivosExtracaoDados;
	}
	
	@Override
	public File recuperarArquivo(String nomeArquivo) throws BusinessException {
		if (StringUtils.isBlank(nomeArquivo)) {
			throw new BusinessException("Nome do arquivo não informado.");
		}
		// Sanitizar: não aceitar separadores nem '..'
		if (nomeArquivo.contains("..") || nomeArquivo.contains("/") || nomeArquivo.contains("\\") ) {
			throw new BusinessException("Nome de arquivo inválido.");
		}

		String pathName = buscarPathName(); // diretório base vindo de config/parametro
		Path basePath = Paths.get(pathName).toAbsolutePath().normalize();
		Path targetPath = basePath.resolve(nomeArquivo).normalize();

		// proteção contra path traversal
		if (!targetPath.startsWith(basePath)) {
			throw new BusinessException("Acesso ao arquivo não autorizado.");
		}

		File arquivo = targetPath.toFile();

		if (!arquivo.exists() || !arquivo.isFile()) {
			throw new BusinessException("Arquivo não encontrado!");
		}
		
		return arquivo;
		
	}

	protected String buscarPathName() throws BusinessException {
		String pathName = System.getProperty("URL_EXTRACAO_DADOS", StringUtils.EMPTY);

		if(StringUtils.isEmpty(pathName)) {
			pathName = parametro
					.consultar(ParametroSistemaEnum.URL_EXTRACAO_DADOS.getCodigoParametro())
					.get(0)
					.getValor();
		}
		if(!pathName.endsWith(File.separator)){
			pathName = pathName + File.separator;
		}
		return pathName;
	}
}
