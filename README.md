# projeto-rh-kaspper
Bem-vindo ao Sis RH da Kaspper - API 🧑‍💼
O Projeto consiste em um CRUD para salvar candidatos a vagas de emprego e vagas e uma consulta listando os candidatos que tem o perfil para a vaga.

fontes : https://github.com/gleysonv/projeto-rh-kaspper/tree/master/sistema-rh-kaspper
 Backend
 Frontend
Ambiente de desenvolvimento
Existem alguns passos para execução do projeto em ambiente local, necessário que alguns programas estejam corretamente instalados.

Framework/lib	Versão Recomendada	S.O Utilizado
Maven	3.8.3	Windows
JDK	8	Windows
Docker	20.10.8	Windows
MySql 8.0.27
Intellij	-	Windows
Execução do projeto
Passo 1
Faça o clone do projeto

$ git clone https://github.com/gleysonv/projeto-rh-kaspper/tree/master/sistema-rh-kaspper sistema-rh-kaspper
Passo 2
Entre na pasta raiz do projeto

$ cd desafio-cliente
Passo 3

$ cd backend
Passo 4
Executar o comando do maven para gerar o artefato que será publicado no docker
existe na raiz do projeto o arquivo do postman - sistemaRH.postman_collection.json para testes do backend

$ mvn clean package -DskipTests
Passo 5
Voltar para pasta raiz

$ cd ..
Passo 6
Execute o comando para levantar toda infraestrutura necessária

$ docker-compose up --build
Acompanhar logs:

$ docker-compose logs -f
Passo 7
Caso haja problemas com a criação das tabelas pela imagem Docker (Mysql):

Há um arquivo chamado init.sql na raiz do projeto, com um script de DDL e INSERT para auxiliar na subida do projeto.
