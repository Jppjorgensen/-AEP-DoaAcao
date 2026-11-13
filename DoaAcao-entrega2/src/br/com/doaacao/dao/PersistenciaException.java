package br.com.doaacao.dao;

/**
 * Erro ao falar com o banco. Embrulha a SQLException para que as camadas de cima
 * (controller e telas) não precisem conhecer nada de JDBC.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
