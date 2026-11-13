package br.com.doaacao.dao;

import br.com.doaacao.model.Doacao;

import java.util.List;
import java.util.Optional;

/** Operações de banco da doação (CRUD). O controller depende só desta interface. */
public interface DoacaoDAO {

    /** Grava a doação com todos os itens e preenche o id gerado. */
    void salvar(Doacao doacao);

    /** Regrava dados e itens de uma doação existente. Retorna false se o id não existir. */
    boolean atualizar(Doacao doacao);

    /** Apaga a doação; os itens saem junto (ON DELETE CASCADE). Retorna false se o id não existir. */
    boolean excluir(long id);

    Optional<Doacao> buscarPorId(long id);

    /** Todas as doações, das mais recentes para as mais antigas. */
    List<Doacao> listarTodas();
}
