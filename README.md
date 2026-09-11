# DoaAção — AEP 4º semestre 2026.2

Sistema desktop local para organizar doações de alimentos e produtos de higiene, associado à ODS 2 (Fome Zero e Agricultura Sustentável). O registro de higiene complementa o escopo social; a contribuição à ODS 2 ocorre pela organização das doações de alimentos.

Repositório público: https://github.com/Jppjorgensen/-AEP-DoaAcao

Planejamento: [PDF da AEP](docs/AEP_DoaAcao_Planejamento.pdf).

## Antes de entregar
- Preencher instituição, curso, turma, professor, nomes e RAs no documento-fonte em docs/Planejamento.md e atualizar o PDF correspondente.
- Ajustar o cronograma proposto às datas oficiais e atribuir responsáveis reais.
- Conferir o acesso público ao repositório e o link no PDF.
- Conferir as orientações oficiais: os requisitos foram extraídos do guia extraoficial anexado.
- Estudar o código, personalizar o trabalho e registrar a contribuição real de cada integrante. Não fabricar commits, entrevistas ou histórico de desenvolvimento.

## Execução rápida
Requisito: Java 17 ou superior instalado e ambiente gráfico de desktop. O pacote inclui classes compiladas e os drivers em lib; não exige servidor nem internet para executar. Extraia o ZIP inteiro, mantendo as pastas.

Se baixou pelo GitHub (Code → Download ZIP) ou clonou o repositório, compile primeiro conforme a seção abaixo: as classes compiladas não são versionadas.

Windows: dê dois cliques em run.bat ou execute `java -cp "bin;lib/*" App` na pasta do projeto.
Linux/macOS: execute `bash run.sh` na pasta do projeto, ou `java -cp 'bin:lib/*' App`.

O SQLite cria automaticamente `data/doaacao.db` a partir de `database/schema.sql`. Para preservar registros, mantenha esse arquivo. O aplicativo foi concebido para um único operador local, sem acesso simultâneo e sem autenticação.

## Compilar a partir do código
Instale um JDK 17 ou superior que inclua javac. No Windows use compile.bat; no Linux/macOS use `bash compile.sh`. Em seguida execute o programa. O repositório ignora bin/ e data/: o professor deverá compilar após clonar. O ZIP inclui bin/ para facilitar a experimentação.

## Utilização
1. Clique em Nova doação e informe doador (ou grupo) e instituição destinatária.
2. Clique em Adicionar item. Escolha ALIMENTO ou HIGIENE, descrição, quantidade inteira positiva e unidade (pacote, caixa, unidade etc.).
3. Para alimentos, informe a validade no formato AAAA-MM-DD; para higiene, deixe a validade vazia. O protótipo não bloqueia alimentos vencidos: a conferência da aptidão é manual.
4. Inclua outros itens e confirme. Não há soma de quantidades com unidades diferentes.
5. Selecione uma doação e use Visualizar / editar para consultar seus itens, corrigir dados ou mudar o status de RECEBIDA para ENTREGUE.
6. Para excluir, selecione a doação, clique em Excluir e confirme. Os itens são removidos junto com ela.
7. O rodapé mostra o total de doações e a contagem por situação.

## Requisitos implementados
RF01 cadastrar doação; RF02 incluir múltiplos itens tipados; RF03 listar e consultar; RF04 editar dados e itens; RF05 excluir com confirmação; RF06 acompanhar recebimento/entrega; RF07 resumo quantitativo por situação.

## Arquitetura e POO
`src/App.java` agrupa classes estáticas aninhadas para simplificar a compilação didática. São classes independentes conceitualmente, com responsabilidades separadas:
- ItemDoacao: classe abstrata com atributos privados e métodos abstratos.
- Alimento e Higiene: herdam ItemDoacao e sobrescrevem getTipo, getValidade e resumo com @Override. A confirmação usa resumo via referência ItemDoacao, exercitando despacho polimórfico.
- Doacao: composição 1:N de ItemDoacao, lista imutável exposta somente para leitura. Um item persistido pertence a uma única doação.
- DoacaoRepository: SQL preparado, transações e reconstrução dos subtipos.
- App: interface Swing, validações e coordenação das ações.

O banco usa tabela única para os subtipos de item com discriminador tipo. As chaves estrangeiras estão ativadas e a exclusão é em cascata. Criar/editar cabeçalho e itens ocorre em uma transação; erro SQL provoca rollback.

## Testes e limites da validação
Execute `java -cp "bin;lib/*" App --test` no Windows ou `java -cp 'bin:lib/*' App --test` no Linux/macOS, a partir da raiz do projeto. O teste usa um banco temporário separado, que é apagado ao final; não altera as doações do aplicativo.

Validado: compilação Java 17; cadastro com dois subtipos; leitura após fechar/reabrir a conexão; atualização; exclusão em cascata; despacho polimórfico; rejeição de quantidade zero, data inválida e doação sem itens.
A interface Swing não foi aberta neste ambiente sem servidor gráfico. Antes da entrega, executar manualmente os passos de uso no computador da equipe e do avaliador. Não se afirma teste visual concluído.

## Escopo e limites
Sem login, cadastro de beneficiários, envio de mensagens, comprovantes fiscais, sincronização, controle de estoque ou geração de relatórios externos. Doador e instituição são textos do registro, sem cadastros independentes. Datas de recebimento e entrega não são armazenadas nesta versão. A unidade é livre e a quantidade é inteira. O registro não comprova entrega física ou segurança sanitária.

## Dependências distribuídas
- sqlite-jdbc 3.46.1.0 (org.xerial), licença Apache-2.0/BSD conforme os avisos incluídos no JAR.
- slf4j-api e slf4j-nop 2.0.13, licença MIT conforme os avisos incluídos nos JARs.
- Java/Swing/JDBC: bibliotecas padrão do Java. Os JARs originais e seus metadados foram preservados.
Fontes dos binários: https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/ e https://repo.maven.apache.org/maven2/org/slf4j/

## Atualizar o repositório
O repositório público é https://github.com/Jppjorgensen/-AEP-DoaAcao. Cada integrante deve enviar suas alterações reais pelo Git, preservando o histórico existente. Não versionar o banco com dados nem a pasta bin. O primeiro envio reúne o material preparado; os commits seguintes devem registrar o trabalho efetivo da equipe.
