🛡️ O que esse código faz, na prática
basePath.resolve(nomeArquivo).normalize()


Ele faz duas coisas de segurança, em sequência:

1️⃣ resolve(nomeArquivo) – “cola o arquivo no diretório certo”

Imagine que o sistema só pode pegar arquivos dentro da pasta:

/dados/extracao/


Se o arquivo pedido for:

relatorio.csv


O sistema monta automaticamente:

/dados/extracao/relatorio.csv


Ou seja:

O sistema não aceita caminhos completos

Ele sempre começa a partir da pasta permitida

2️⃣ normalize() – “limpa tentativas de enganar o caminho”

Agora imagine que alguém tenta pedir:

../../etc/senha.txt


O normalize():

Remove truques como ../

Resolve o caminho “real” final

Por exemplo:

/dados/extracao/../../etc/senha.txt
↓
/etc/senha.txt


Isso permite que o sistema perceba a tentativa de sair da pasta permitida.

3️⃣ A verificação final (a trava de segurança)

Depois disso, o sistema confere:

if (!targetPath.startsWith(basePath)) {
    bloqueia o acesso
}


Em termos simples:

“Se o arquivo final não estiver dentro da pasta autorizada, o acesso é negado.”
