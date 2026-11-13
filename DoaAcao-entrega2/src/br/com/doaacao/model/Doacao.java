package br.com.doaacao.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uma doação recebida pelo grupo: quem doou, para qual instituição vai e os itens.
 * Composição 1:N: os itens só existem dentro da doação e saem junto com ela.
 */
public class Doacao {

    private Long id; // nulo enquanto a doação não foi salva no banco
    private final String doador;
    private final String destino;
    private Situacao situacao;
    private final List<ItemDoacao> itens;

    /** Doação nova: sempre começa como RECEBIDA. */
    public Doacao(String doador, String destino, List<ItemDoacao> itens) {
        this(null, doador, destino, Situacao.RECEBIDA, itens);
    }

    public Doacao(Long id, String doador, String destino, Situacao situacao, List<ItemDoacao> itens) {
        this.doador = ItemDoacao.textoObrigatorio(doador, "Informe quem fez a doação");
        this.destino = ItemDoacao.textoObrigatorio(destino, "Informe a instituição que vai receber");
        if (situacao == null) {
            throw new IllegalArgumentException("Informe a situação da doação.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Inclua pelo menos um item na doação.");
        }
        this.id = id;
        this.situacao = situacao;
        this.itens = new ArrayList<>(itens);
    }

    public void marcarComoEntregue() {
        situacao = Situacao.ENTREGUE;
    }

    /** Alimentos desta doação que estão vencendo, mas só enquanto ela ainda não foi entregue. */
    public List<ItemDoacao> itensQuePrecisamDeAtencao(LocalDate hoje) {
        if (situacao == Situacao.ENTREGUE) {
            return List.of();
        }
        return itens.stream()
                .filter(item -> item.precisaDeAtencao(hoje))
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoador() {
        return doador;
    }

    public String getDestino() {
        return destino;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public List<ItemDoacao> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
