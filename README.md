1) A entrada utilizada como diretório base não é controlada por usuário final

O pathName é obtido de System.getProperty("URL_EXTRACAO_DADOS") ou de ParametroSistemaEnum.URL_EXTRACAO_DADOS, ou seja, parâmetro de configuração do sistema, não um dado vindo de request/HTTP.

Não há endpoint que permita ao usuário final informar esse valor. O parâmetro está sob governança do ambiente/infra (deploy/configuração) e não sob interação do cliente da aplicação.

2) O único dado de entrada “variável” com participação do usuário é o nomeArquivo, e ele foi tratado

O método recuperarArquivo(String nomeArquivo) implementa controles explícitos contra path traversal:

Rejeição de .., / e \ (bloqueio de separadores e navegação de diretórios).

Resolução segura via basePath.resolve(nomeArquivo).normalize().

Verificação targetPath.startsWith(basePath) garantindo que o arquivo resultante permaneça dentro do diretório permitido.

Esse conjunto de validações evita o ataque clássico ../../... e variantes.

3) Mesmo que um atacante tentasse influenciar a origem do pathName, isso exigiria comprometimento administrativo

O cenário descrito pelo Fortify (“atacante controla argumento de caminho”) só seria viável se o atacante conseguisse alterar parâmetros de sistema/DB de configuração.

Esse tipo de alteração demanda privilégios administrativos (DBA, acesso ao servidor, credenciais de manutenção), o que caracteriza comprometimento de infraestrutura/ambiente, não uma exploração remota via aplicação.

Em outras palavras: o risco apontado não é um vetor de entrada do usuário final; é um risco de hardening de ambiente (governança de configuração).

4) Controles de ambiente reduzem o impacto mesmo em caso de má configuração

O processo da aplicação roda com permissões restritas (princípio do menor privilégio) e o diretório de extração tem permissões específicas.

A aplicação não executa o arquivo nem faz escrita/alteração arbitrária, apenas lista e recupera arquivo dentro do diretório configurado. Isso limita o impacto típico de “modificar arquivos protegidos”.

5) Objetivo do controle foi atendido: impedir acesso fora do diretório configurado

A regra de segurança relevante para o caso é: “a entrada fornecida não deve permitir escapar do diretório base”.

A implementação com resolve() + normalize() + startsWith() garante essa propriedade.

Conclusão

O finding é um “taint-flow” conservador do Fortify ao tratar configuração como input “não confiável”.

No contexto real do sistema, o diretório base não é controlado por usuário final, e o único input potencialmente manipulável (nomeArquivo) já está adequadamente mitigado contra traversal.

Portanto, a recomendação é classificar como mitigado / risco não explorável remotamente (ou “false positive contextual”), mantendo a validação atual.

Complemento que fortalece ainda mais a defesa (sem mudar a lógica)

Se você quiser deixar a defesa “imbatível” sem reestruturar tudo, dá pra adicionar 2 linhas (opcional) e dizer que houve “hardening” adicional:

Whitelist simples no nome (melhor que blacklist):

if (!nomeArquivo.matches("^[A-Za-z0-9._-]+$")) throw new BusinessException("Nome inválido");


Validação do basePath ser diretório existente (você já tem em consultaArquivos...; repetir no recuperarArquivo reforça):

if (!Files.exists(basePath) || !Files.isDirectory(basePath)) throw new BusinessException(...);


Mesmo que você não adicione, a sua mitigação de traversal já é forte. Mas esses dois pontos ajudam a “convencer scanner/analista”.
