# DoaAção

**Unicesumar · Engenharia de Software · Turma esoft4sm**  
AEP — 4º semestre — 2026.2

Aplicação desktop para organizar doações de alimentos e produtos de higiene, registrar os itens recebidos e acompanhar sua entrega às instituições. O projeto se relaciona à **ODS 2 — Fome Zero e Agricultura Sustentável** pela organização das doações de alimentos.

[Documento da AEP em PDF](docs/AEP_DoaAcao_Planejamento.pdf) · [Requisitos e planejamento](docs/Planejamento.md)

## Funcionalidades

- Cadastrar doações com identificação do doador ou grupo e da instituição destinatária.
- Incluir vários itens por doação, com tipo, descrição, quantidade e unidade.
- Registrar a validade dos alimentos.
- Consultar, editar e excluir doações com seus itens.
- Acompanhar as situações **RECEBIDA** e **ENTREGUE**.
- Exibir a quantidade de doações por situação.

## Tecnologias

Java 17, Java Swing, JDBC e SQLite. As bibliotecas necessárias para conexão com o banco estão na pasta `lib`.

## Como executar

É necessário ter **JDK 17 ou superior**, com os comandos `java` e `javac` disponíveis, e um computador com interface gráfica.

Baixe o repositório em **Code → Download ZIP** e extraia todos os arquivos, ou clone o projeto. Abra o terminal na pasta que contém o arquivo `README.md`.

**Windows**

```bat
compile.bat
run.bat
```

**Linux ou macOS**

```bash
bash compile.sh
bash run.sh
```

Execute primeiro o script de compilação. Ele cria a pasta `bin` com as classes Java. Depois, execute o script que abre o sistema. Não é necessário configurar um servidor de banco nem conectar-se à internet durante o uso.

Na primeira execução, a aplicação cria o banco `data/doaacao.db` a partir de `database/schema.sql`. Os dados ficam nesse arquivo. Para fazer uma cópia de segurança, feche o programa antes de copiá-lo.

## Como usar

1. Clique em **Nova doacao** e informe o doador ou grupo e a instituição destinatária.
2. Use **Adicionar item** para incluir alimentos ou produtos de higiene. Informe descrição, quantidade inteira positiva e unidade, como pacote, caixa ou unidade.
3. Para alimentos, preencha a validade no formato **AAAA-MM-DD**. Para higiene, deixe esse campo vazio.
4. Confirme para salvar. Toda doação deve conter pelo menos um item.
5. Selecione uma doação e clique em **Visualizar / editar** para consultar os itens, corrigir os dados ou alterar a situação para **ENTREGUE**.
6. Para remover um registro, selecione-o e clique em **Excluir**. A exclusão exige confirmação e remove também os itens da doação.

O rodapé apresenta a quantidade total de doações e a contagem por situação. Quantidades de itens com unidades diferentes não são somadas.

## Organização do projeto

| Arquivo ou pasta | Conteúdo |
| --- | --- |
| `src/App.java` | Classes do domínio, acesso ao banco e interface |
| `database/schema.sql` | Criação das tabelas, restrições e índice |
| `docs/` | Planejamento e PDF da AEP com diagramas |
| `lib/` | Bibliotecas JDBC e SLF4J |
| `compile.bat` / `compile.sh` | Compilação do código |
| `run.bat` / `run.sh` | Execução do sistema |

As pastas `bin` e `data` são geradas localmente e não são versionadas.

## Orientação a objetos

- **Abstração e encapsulamento:** `ItemDoacao` é uma classe abstrata com atributos privados.
- **Herança:** `Alimento` e `Higiene` estendem `ItemDoacao`.
- **Polimorfismo:** o método `resumo()` é sobrescrito com `@Override` e chamado por referências do tipo `ItemDoacao`.
- **Composição 1:N:** uma `Doacao` contém um ou mais itens, que pertencem exclusivamente a ela.
- **Persistência:** `DoacaoRepository` utiliza SQL preparado e transações para salvar o conjunto de doação e itens. A exclusão no banco ocorre em cascata.

As classes estão agrupadas em `App.java`, com responsabilidades distintas para domínio, persistência e interface.

## Testes automatizados

Após compilar, execute um dos comandos abaixo a partir da raiz do projeto.

**Windows**

```bat
java -cp "bin;lib/*" App --test
```

**Linux ou macOS**

```bash
java -cp 'bin:lib/*' App --test
```

Os testes verificam cadastro, consulta, atualização, exclusão em cascata, persistência após reabrir o banco, polimorfismo e rejeição de quantidade zero, data inválida e doação sem itens. Eles utilizam um banco temporário separado, sem alterar os registros da aplicação. A cobertura automatizada não inclui a interação com as telas.

## Escopo da versão

O sistema funciona localmente, para um operador, sem autenticação ou sincronização. Doador e instituição são campos do registro, sem cadastros independentes. Não há gestão de estoque, cadastro de beneficiários, exportação de relatórios ou registro das datas de recebimento e entrega.

A quantidade de cada item é inteira. A validade é registrada apenas para alimentos; o sistema não bloqueia itens vencidos. A conferência de vencimento, embalagem e aptidão para doação é manual.

## Dependências

- SQLite JDBC 3.46.1.0 (`org.xerial`). Licenças e avisos preservados no JAR original.
- SLF4J API e NOP 2.0.13. Licença MIT e avisos preservados nos JARs originais.
- Swing e JDBC: bibliotecas padrão do Java.

Binários obtidos no Maven Central: [SQLite JDBC](https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/) e [SLF4J](https://repo.maven.apache.org/maven2/org/slf4j/).
