# DoaAção — Planejamento da AEP

## 1. Identificação e proposta

Projeto: DoaAção — Sistema de Gestão de Doações. AEP do 4º semestre, 2026.2.

Instituição: [PREENCHER] | Curso: [ADS ou Engenharia de Software] | Turma: [PREENCHER] | Professor: [PREENCHER].

Integrantes e RAs: [Integrante 1 / RA]; [Integrante 2 / RA]; [Integrante 3 / RA, se houver].

Organizações e grupos solidários podem receber doações por diferentes canais e depender de anotações dispersas. Isso dificulta localizar o que foi recebido, identificar a instituição destinatária e acompanhar o que já foi entregue. Esta é uma hipótese de problema para o projeto acadêmico; não se apresenta entrevista ou pesquisa de campo como realizada.

O sistema proposto centraliza o registro de doações de alimentos e produtos de higiene, seus itens e sua situação. O usuário principal é o voluntário ou responsável pela organização das doações.

A proposta se relaciona à ODS 2 — Fome Zero e Agricultura Sustentável — pela organização da arrecadação e destinação de alimentos. Produtos de higiene complementam a ação social. O sistema apoia a organização; não se afirma que, por si só, elimina a insegurança alimentar.

Objetivo: permitir que um operador registre, consulte, atualize e exclua doações, acompanhe recebimentos e entregas e preserve essas informações em banco de dados local.

## 2. Escopo e requisitos

RF01 — O sistema deve permitir cadastrar uma doação com identificação do doador ou grupo, instituição destinatária e situação inicial RECEBIDA.

RF02 — O sistema deve permitir incluir um ou mais itens em cada doação, informando tipo (alimento ou higiene), descrição, quantidade inteira positiva e unidade. Para alimentos, deve registrar a validade.

RF03 — O sistema deve listar as doações e permitir consultar os dados e todos os itens de uma doação selecionada.

RF04 — O sistema deve permitir alterar doador, instituição, situação e os itens de uma doação existente.

RF05 — O sistema deve permitir excluir uma doação após confirmação, removendo também seus itens.

RF06 — O sistema deve permitir registrar a situação RECEBIDA ou ENTREGUE e exibi-la na listagem.

RF07 — O sistema deve apresentar a contagem total de doações e os totais por situação.

Regras: toda doação deve conter ao menos um item. Os campos obrigatórios não podem estar vazios. Cada item pertence exclusivamente a uma doação. Não há somatório de quantidades com unidades diferentes. Alimentos exigem data válida no formato AAAA-MM-DD; a conferência de vencimento e aptidão é manual.

Fora do escopo: login, beneficiários individuais, estoque, sincronização entre computadores, emissão de comprovantes e relatórios exportados. Doador e instituição são campos textuais, sem cadastro independente.

Requisitos não funcionais: uso local com interface gráfica; persistência em SQLite; integridade por chave estrangeira e transações; mensagens de validação; Java 17 ou superior; instruções de execução no README.

## 3. Planejamento e viabilidade

Cronograma proposto para oito semanas do 2º bimestre. As datas abaixo são campos a preencher após consultar o calendário oficial. Os responsáveis A, B e C devem ser substituídos pelos integrantes reais; em dupla, redistribuir as atividades.

Semana 1 | Datas: __/__/__ a __/__/__ | Validar problema, escopo e critérios de aceitação | A + equipe.

Semana 2 | Datas: __/__/__ a __/__/__ | Revisar UML, DER e script do banco | B.

Semana 3 | Datas: __/__/__ a __/__/__ | Implementar classes e validações | A.

Semana 4 | Datas: __/__/__ a __/__/__ | Implementar cadastro e consulta no banco | B.

Semana 5 | Datas: __/__/__ a __/__/__ | Implementar edição, exclusão e transações | B + A.

Semana 6 | Datas: __/__/__ a __/__/__ | Implementar telas e navegação | C.

Semana 7 | Datas: __/__/__ a __/__/__ | Integrar, testar CRUD e persistência | Todos.

Semana 8 | Datas: __/__/__ a __/__/__ | Corrigir, documentar e preparar apresentação | Todos.

Backlog: priorizar RF01–RF05 para obter o núcleo funcional; depois RF06–RF07. Critério de conclusão: funcionalidade executável, persistência verificada e instruções atualizadas. Usar tarefas A fazer, Em andamento e Concluído no controle da equipe.

Viabilidade: aplicação local com bibliotecas disponíveis sem cobrança de licença para este uso e sem servidor contratado. Estimativa inicial de esforço: 36 a 48 horas de equipe, sujeita à experiência dos integrantes.

Riscos e respostas: dificuldade com JDBC — integrar banco cedo; divergência entre código e diagramas — revisar semanalmente; perda de dados — copiar o banco com o programa fechado; atraso — preservar o CRUD antes de ampliar o escopo. Mudanças devem ser justificadas no README.

## 4. Justificativa técnica e interface

Java foi escolhido pela tipagem estática, suporte direto à orientação a objetos e disponibilidade de bibliotecas padrão para interface desktop e conexão com banco. A versão de referência é Java 17.

SQLite é um banco relacional real, acessado por JDBC. Permite persistir as informações em disco sem configurar um servidor externo, reduzindo a preparação do ambiente acadêmico. Um arquivo SQLite é um banco de dados, não um arquivo de texto usado para simular persistência.

As relações entre doação e itens são representadas por chave estrangeira. A aplicação ativa a verificação dessa chave em cada conexão. A exclusão em cascata e as transações preservam a consistência dos registros.

A interface usa Java Swing. A tela principal reúne listagem, situação e ações; o formulário identifica os campos e permite adicionar ou remover itens. Erros são exibidos em mensagens e exclusões exigem confirmação. O status aparece em texto, sem depender apenas de cores.

Jornada: abrir o programa → escolher Nova doação → informar origem e destino → incluir itens → salvar → consultar → atualizar para ENTREGUE. Antes da apresentação, a equipe deve testar a jornada em um computador com ambiente gráfico.

Para ADS, essa solução contempla a interface navegável e explicita o planejamento e a usabilidade. Para Engenharia de Software, as mesmas telas podem ser mantidas, com foco avaliativo em requisitos, arquitetura e orientação a objetos.

A implementação separa conceitualmente domínio, persistência e interface. As classes estão aninhadas em App.java para simplificar a compilação; os atributos do domínio são privados e acessados por métodos. Uma evolução possível é separar essas classes em arquivos próprios.

Não são exigidos dados sensíveis: o doador pode ser identificado pelo nome do grupo. A aplicação é local, sem login e sem sincronização; por isso, foi delimitada ao uso por um operador.

## 5. Modelagem de classes (UML)

ItemDoacao é abstrata. Alimento e Higiene herdam seus atributos e implementam getTipo(), getValidade() e resumo() com @Override. Ao salvar, a interface chama resumo() por uma referência do tipo ItemDoacao; o resultado depende do subtipo concreto.

Doacao possui de um a vários itens. O item não é compartilhado com outra doação e sua vida no banco termina quando a doação é excluída. Essa relação expressa composição 1:N.

DoacaoRepository salva, lista e exclui agregados Doacao; App coordena a interface e utiliza o repositório. Os nomes mostrados no diagrama correspondem às classes do código.

## 6. Modelo de dados (DER)

A tabela doacoes armazena o cabeçalho de cada registro. A tabela itens_doacao armazena todos os itens e o discriminador tipo. A herança do domínio é persistida em uma única tabela de itens.

Cardinalidade lógica: uma doação tem 1..N itens; cada item pertence a exatamente uma doação. A chave estrangeira garante que não exista item órfão; a exigência de ao menos um item é validada pelo domínio, dentro do fluxo transacional.

Na tabela itens_doacao, validade é obrigatória para ALIMENTO e nula para HIGIENE. Quantidade é positiva; status aceita apenas RECEBIDA e ENTREGUE. A exclusão da doação remove seus itens via ON DELETE CASCADE.

## 7. Entrega, rastreabilidade e validação

Repositório público: https://github.com/Jppjorgensen/-AEP-DoaAcao.

Estrutura: /src — código Java; /database — schema.sql; /docs — PDF e documentação editável; /lib — dependências; README.md — execução e limites; scripts de compilação e execução.

RF01–RF02: formulário de doação, classes de domínio e método salvar. RF03: método listar e seleção na interface. RF04: edição com substituição transacional dos itens. RF05: excluir com confirmação e cascata. RF06: campo status. RF07: contadores na tela principal.

Validação realizada nesta preparação: compilação com nível Java 17; teste automatizado de cadastro com alimento e higiene, leitura após reconexão, atualização, exclusão em cascata, polimorfismo e rejeição de entradas inválidas. Os testes usam banco temporário separado.

Limite da validação: a interface gráfica não foi executada visualmente no ambiente de preparação, que não possui servidor gráfico. A equipe precisa validar as telas e os scripts no computador em que apresentará o trabalho.

Pendências antes de entregar: preencher identificação e RAs; confirmar curso e regras oficiais; definir datas e responsáveis; revisar e compreender o código; executar a interface; conferir o acesso público ao GitHub e o link neste PDF.

Os commits devem refletir a participação efetiva dos integrantes. Não foram inventados histórico colaborativo, entrevistas, pesquisa de campo ou resultados de uso por instituições.

Base documental: Guia_Extraoficial_AEP_4Semestre_2026.pdf, fornecido pelo solicitante; Exemplo_AEP_ChamadaSmart.pdf, utilizado somente como referência de estrutura. O tema e a implementação deste pacote são distintos do exemplo.

Os diagramas completos estão no PDF; ao alterar o modelo, atualizar ambos.
