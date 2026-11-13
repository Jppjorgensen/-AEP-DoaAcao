package br.com.doaacao;

import br.com.doaacao.controller.DoacaoController;
import br.com.doaacao.dao.ConexaoFactory;
import br.com.doaacao.dao.DoacaoDAOSqlite;
import br.com.doaacao.teste.VerificacaoSistema;
import br.com.doaacao.view.TelaPrincipal;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

/**
 * Ponto de entrada do DoaAção.
 *   sem argumentos -> abre o sistema
 *   --test         -> roda as verificações automáticas num banco temporário
 */
public class App {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--test")) {
            VerificacaoSistema.main(args);
            return;
        }
        ConexaoFactory.preparar(Path.of("data", "doaacao.db"), Path.of("database", "schema.sql"));
        DoacaoController controller = new DoacaoController(new DoacaoDAOSqlite());
        SwingUtilities.invokeLater(() -> new TelaPrincipal(controller).abrir());
    }
}
