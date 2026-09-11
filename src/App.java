import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** DoaAcao: aplicativo local, sem dados reais de beneficiarios. */
public class App {
    public static abstract class ItemDoacao {
        private final String descricao, unidade;
        private final int quantidade;
        public ItemDoacao(String descricao, int quantidade, String unidade) {
            this.descricao = obrigatorio(descricao, "Descricao");
            this.unidade = obrigatorio(unidade, "Unidade");
            if (quantidade <= 0) throw new IllegalArgumentException("Quantidade deve ser positiva.");
            this.quantidade = quantidade;
        }
        public String getDescricao() { return descricao; }
        public String getUnidade() { return unidade; }
        public int getQuantidade() { return quantidade; }
        public abstract String getTipo();
        public abstract String getValidade();
        public abstract String resumo();
    }
    public static final class Alimento extends ItemDoacao {
        private final LocalDate validade;
        public Alimento(String d, int q, String u, String v) {
            super(d,q,u);
            try { validade = LocalDate.parse(v); }
            catch (Exception e) { throw new IllegalArgumentException("Validade do alimento: use AAAA-MM-DD."); }
        }
        @Override public String getTipo() { return "ALIMENTO"; }
        @Override public String getValidade() { return validade.toString(); }
        @Override public String resumo() { return getDescricao()+" | validade "+validade; }
    }
    public static final class Higiene extends ItemDoacao {
        public Higiene(String d, int q, String u) { super(d,q,u); }
        @Override public String getTipo() { return "HIGIENE"; }
        @Override public String getValidade() { return ""; }
        @Override public String resumo() { return getDescricao()+" | conferir embalagem antes da entrega"; }
    }
    public static final class Doacao {
        private final long id;
        private final String doador, destino, status;
        private final List<ItemDoacao> itens;
        public Doacao(long id, String doador, String destino, String status, List<ItemDoacao> itens) {
            this.id=id; this.doador=obrigatorio(doador,"Doador");
            this.destino=obrigatorio(destino,"Instituicao destinataria");
            if (!List.of("RECEBIDA","ENTREGUE").contains(status)) throw new IllegalArgumentException("Status invalido.");
            this.status=status;
            if (itens==null || itens.isEmpty()) throw new IllegalArgumentException("Inclua pelo menos um item.");
            this.itens=List.copyOf(itens);
        }
        public long getId() { return id; }
        public String getDoador() { return doador; }
        public String getDestino() { return destino; }
        public String getStatus() { return status; }
        public List<ItemDoacao> getItens() { return itens; }
    }
    public static final class DoacaoRepository implements AutoCloseable {
        private final Connection con;
        public DoacaoRepository(Path banco, Path script) throws Exception {
            con=DriverManager.getConnection("jdbc:sqlite:"+banco.toAbsolutePath());
            try (Statement s=con.createStatement()) {
                s.execute("PRAGMA foreign_keys = ON");
                for (String sql:Files.readString(script).split(";")) if (!sql.isBlank()) s.execute(sql);
            }
        }
        public long salvar(Doacao d) throws SQLException {
            con.setAutoCommit(false);
            try {
                long id=d.getId();
                String sql=id==0 ? "INSERT INTO doacoes(doador,destino,status) VALUES(?,?,?)" : "UPDATE doacoes SET doador=?,destino=?,status=? WHERE id=?";
                try (PreparedStatement p=con.prepareStatement(sql)) {
                    p.setString(1,d.getDoador()); p.setString(2,d.getDestino()); p.setString(3,d.getStatus());
                    if (id!=0) p.setLong(4,id);
                    if (p.executeUpdate()!=1) throw new SQLException("Doacao nao encontrada.");
                }
                if (id==0) try (Statement s=con.createStatement(); ResultSet r=s.executeQuery("SELECT last_insert_rowid()")) { r.next(); id=r.getLong(1); }
                try (PreparedStatement p=con.prepareStatement("DELETE FROM itens_doacao WHERE doacao_id=?")) { p.setLong(1,id); p.executeUpdate(); }
                try (PreparedStatement p=con.prepareStatement("INSERT INTO itens_doacao(doacao_id,tipo,descricao,quantidade,unidade,validade) VALUES(?,?,?,?,?,?)")) {
                    for (ItemDoacao i:d.getItens()) {
                        p.setLong(1,id); p.setString(2,i.getTipo()); p.setString(3,i.getDescricao());
                        p.setInt(4,i.getQuantidade()); p.setString(5,i.getUnidade());
                        if (i.getValidade().isEmpty()) p.setNull(6,Types.VARCHAR); else p.setString(6,i.getValidade());
                        p.executeUpdate();
                    }
                }
                con.commit(); return id;
            } catch (SQLException e) { con.rollback(); throw e; }
            finally { con.setAutoCommit(true); }
        }
        public List<Doacao> listar() throws SQLException {
            List<Doacao> lista=new ArrayList<>();
            try (Statement s=con.createStatement(); ResultSet r=s.executeQuery("SELECT * FROM doacoes ORDER BY id DESC")) {
                while (r.next()) {
                    long id=r.getLong("id"); List<ItemDoacao> itens=new ArrayList<>();
                    try (PreparedStatement p=con.prepareStatement("SELECT * FROM itens_doacao WHERE doacao_id=? ORDER BY id")) {
                        p.setLong(1,id);
                        try (ResultSet ri=p.executeQuery()) {
                            while (ri.next()) itens.add(ri.getString("tipo").equals("ALIMENTO") ?
                                new Alimento(ri.getString("descricao"),ri.getInt("quantidade"),ri.getString("unidade"),ri.getString("validade")) :
                                new Higiene(ri.getString("descricao"),ri.getInt("quantidade"),ri.getString("unidade")));
                        }
                    }
                    lista.add(new Doacao(id,r.getString("doador"),r.getString("destino"),r.getString("status"),itens));
                }
            }
            return lista;
        }
        public void excluir(long id) throws SQLException {
            try (PreparedStatement p=con.prepareStatement("DELETE FROM doacoes WHERE id=?")) {
                p.setLong(1,id); if (p.executeUpdate()!=1) throw new SQLException("Doacao nao encontrada.");
            }
        }
        @Override public void close() throws SQLException { con.close(); }
    }
    private static String obrigatorio(String s,String campo) {
        if (s==null || s.isBlank()) throw new IllegalArgumentException(campo+" e obrigatorio.");
        if (s.trim().length()>150) throw new IllegalArgumentException(campo+": maximo de 150 caracteres.");
        return s.trim();
    }
    private final DoacaoRepository repository;
    private final JFrame janela=new JFrame("DoaAcao | Gestao de doacoes");
    private final DefaultTableModel modelo=new DefaultTableModel(new String[]{"ID","Doador","Instituicao","Status","Itens"},0) {
        @Override public boolean isCellEditable(int r,int c) { return false; }
    };
    private final JTable tabela=new JTable(modelo);
    private final JLabel resumo=new JLabel(" ");
    private List<Doacao> doacoes=new ArrayList<>();
    private App(DoacaoRepository repository) throws SQLException {
        this.repository=repository;
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(940,520); janela.setLocationRelativeTo(null);
        JPanel topo=new JPanel(new GridLayout(2,1,0,8));
        topo.setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        topo.add(new JLabel("DoaAcao - Registre, acompanhe e organize as entregas"));
        JPanel botoes=new JPanel();
        botao(botoes,"Nova doacao",()->editar(null));
        botao(botoes,"Visualizar / editar",()->editar(selecionada()));
        botao(botoes,"Excluir",()->{
            Doacao d=selecionada();
            if (JOptionPane.showConfirmDialog(janela,"Excluir a doacao #"+d.getId()+" e todos os seus itens?","Confirmar exclusao",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                repository.excluir(d.getId()); atualizar();
            }
        });
        botao(botoes,"Atualizar",()->atualizar());
        topo.add(botoes); janela.add(topo,BorderLayout.NORTH);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabela.setRowHeight(28);
        janela.add(new JScrollPane(tabela),BorderLayout.CENTER);
        resumo.setBorder(BorderFactory.createEmptyBorder(12,14,12,14)); janela.add(resumo,BorderLayout.SOUTH);
        atualizar(); janela.setVisible(true);
    }
    private interface Acao { void executar() throws Exception; }
    private void botao(JPanel p,String texto,Acao acao) {
        JButton b=new JButton(texto); p.add(b);
        b.addActionListener(e->{ try { acao.executar(); } catch(Exception ex) { erro(ex); } });
    }
    private void erro(Exception ex) { JOptionPane.showMessageDialog(janela,ex.getMessage(),"Verifique os dados",JOptionPane.ERROR_MESSAGE); }
    private Doacao selecionada() {
        int linha=tabela.getSelectedRow();
        if (linha<0) throw new IllegalArgumentException("Selecione uma doacao na tabela.");
        return doacoes.get(linha);
    }
    private void atualizar() throws SQLException {
        doacoes=repository.listar(); modelo.setRowCount(0); int entregues=0;
        for (Doacao d:doacoes) {
            modelo.addRow(new Object[]{d.getId(),d.getDoador(),d.getDestino(),d.getStatus(),d.getItens().size()});
            if (d.getStatus().equals("ENTREGUE")) entregues++;
        }
        resumo.setText("Doacoes: "+doacoes.size()+"    |    Recebidas: "+(doacoes.size()-entregues)+"    |    Entregues: "+entregues);
    }
    private void editar(Doacao atual) {
        JTextField doador=new JTextField(atual==null?"":atual.getDoador());
        JTextField destino=new JTextField(atual==null?"":atual.getDestino());
        JComboBox<String> status=new JComboBox<>(new String[]{"RECEBIDA","ENTREGUE"});
        if (atual!=null) status.setSelectedItem(atual.getStatus());
        DefaultTableModel linhas=new DefaultTableModel(new String[]{"Tipo","Descricao","Quantidade","Unidade","Validade AAAA-MM-DD"},0);
        if(atual!=null) for(ItemDoacao i:atual.getItens()) linhas.addRow(new Object[]{i.getTipo(),i.getDescricao(),i.getQuantidade(),i.getUnidade(),i.getValidade()});
        JTable itens=new JTable(linhas); itens.setRowHeight(27);
        itens.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"ALIMENTO","HIGIENE"})));
        JPanel cabecalho=new JPanel(new GridLayout(3,2,8,8));
        cabecalho.add(new JLabel("Doador ou identificacao do grupo:")); cabecalho.add(doador);
        cabecalho.add(new JLabel("Instituicao destinataria:")); cabecalho.add(destino);
        cabecalho.add(new JLabel("Situacao:")); cabecalho.add(status);
        JPanel painel=new JPanel(new BorderLayout(8,8)); painel.add(cabecalho,BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(itens); scroll.setPreferredSize(new java.awt.Dimension(800,230)); painel.add(scroll,BorderLayout.CENTER);
        JPanel comandos=new JPanel();
        botao(comandos,"Adicionar item",()->{ if(itens.isEditing()) itens.getCellEditor().stopCellEditing(); linhas.addRow(new Object[]{"ALIMENTO","",1,"unidade",""}); });
        botao(comandos,"Remover item",()->{ if(itens.isEditing()) itens.getCellEditor().stopCellEditing(); int r=itens.getSelectedRow(); if(r>=0) linhas.removeRow(r); });
        painel.add(comandos,BorderLayout.SOUTH);
        while(JOptionPane.showConfirmDialog(janela,painel,atual==null?"Nova doacao":"Editar doacao #"+atual.getId(),JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
            try {
                if(itens.isEditing() && !itens.getCellEditor().stopCellEditing()) throw new IllegalArgumentException("Conclua a edicao do item.");
                List<ItemDoacao> lista=new ArrayList<>();
                for(int r=0;r<linhas.getRowCount();r++) {
                    String tipo=celula(linhas,r,0), descricao=celula(linhas,r,1), unidade=celula(linhas,r,3), validade=celula(linhas,r,4);
                    int quantidade;
                    try { quantidade=Integer.parseInt(celula(linhas,r,2)); } catch(Exception e) { throw new IllegalArgumentException("Quantidade do item "+(r+1)+": use um numero inteiro positivo."); }
                    if(!List.of("ALIMENTO","HIGIENE").contains(tipo)) throw new IllegalArgumentException("Tipo de item invalido.");
                    if(tipo.equals("HIGIENE") && !validade.isEmpty()) throw new IllegalArgumentException("Neste prototipo, validade e registrada apenas para alimentos.");
                    lista.add(tipo.equals("ALIMENTO")?new Alimento(descricao,quantidade,unidade,validade):new Higiene(descricao,quantidade,unidade));
                }
                Doacao d=new Doacao(atual==null?0:atual.getId(),doador.getText(),destino.getText(),(String)status.getSelectedItem(),lista);
                repository.salvar(d); atualizar();
                StringBuilder feedback=new StringBuilder("Doacao salva.\n");
                for(ItemDoacao i:d.getItens()) feedback.append(i.resumo()).append("\n");
                JOptionPane.showMessageDialog(janela,feedback.toString()); return;
            } catch(Exception e) { erro(e); }
        }
    }
    private static String celula(DefaultTableModel m,int r,int c) { Object v=m.getValueAt(r,c); return v==null?"":v.toString().trim(); }
    private static void verificar(boolean ok,String mensagem) { if(!ok) throw new AssertionError(mensagem); }
    private static void testar() throws Exception {
        Path banco=Files.createTempFile("doaacao-teste-",".db");
        try {
            long id;
            try(DoacaoRepository r=new DoacaoRepository(banco,Path.of("database/schema.sql"))) {
                id=r.salvar(new Doacao(0,"Grupo teste","Instituicao teste","RECEBIDA",List.of(new Alimento("Arroz",2,"pacote","2027-12-01"),new Higiene("Sabonete",3,"unidade"))));
                verificar(r.listar().size()==1 && r.listar().get(0).getItens().size()==2,"Cadastro com composicao");
            }
            try(DoacaoRepository r=new DoacaoRepository(banco,Path.of("database/schema.sql"))) {
                verificar(r.listar().get(0).getId()==id,"Persistencia apos reabrir banco");
                verificar(r.listar().get(0).getItens().get(0).resumo().contains("validade"),"Polimorfismo alimento");
                verificar(r.listar().get(0).getItens().get(1).resumo().contains("embalagem"),"Polimorfismo higiene");
                r.salvar(new Doacao(id,"Grupo alterado","Instituicao teste","ENTREGUE",List.of(new Higiene("Sabao",1,"barra"))));
                verificar(r.listar().get(0).getStatus().equals("ENTREGUE") && r.listar().get(0).getItens().size()==1,"Atualizacao");
                r.excluir(id); verificar(r.listar().isEmpty(),"Exclusao");
            }
            try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+banco); Statement s=c.createStatement(); ResultSet rs=s.executeQuery("SELECT count(*) FROM itens_doacao")) { rs.next(); verificar(rs.getInt(1)==0,"Exclusao em cascata"); }
            boolean rejeitou=false; try {new Alimento("Arroz",0,"pacote","2027-01-01");} catch(IllegalArgumentException e) {rejeitou=true;} verificar(rejeitou,"Quantidade invalida");
            rejeitou=false; try {new Alimento("Arroz",1,"pacote","2027-02-30");} catch(IllegalArgumentException e) {rejeitou=true;} verificar(rejeitou,"Data invalida");
            rejeitou=false; try {new Doacao(0,"Grupo","Destino","RECEBIDA",List.of());} catch(IllegalArgumentException e) {rejeitou=true;} verificar(rejeitou,"Doacao vazia");
            System.out.println("OK: CRUD, persistencia, cascata, polimorfismo e validacoes.");
        } finally { Files.deleteIfExists(banco); }
    }
    public static void main(String[] args) throws Exception {
        if(args.length>0 && args[0].equals("--test")) { testar(); return; }
        Files.createDirectories(Path.of("data"));
        DoacaoRepository repository=new DoacaoRepository(Path.of("data/doaacao.db"),Path.of("database/schema.sql"));
        Runtime.getRuntime().addShutdownHook(new Thread(()->{ try { repository.close(); } catch(Exception e) { System.err.println(e.getMessage()); } }));
        SwingUtilities.invokeLater(()->{ try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); new App(repository); } catch(Exception e) { JOptionPane.showMessageDialog(null,e.getMessage()); } });
    }
}
