package modelo;

import excecao.StockInsuficienteException;
import excecao.ValidacaoException;
import java.time.LocalDateTime;

public class Produto {

    private int id;
    private String codigo;
    private String nome;
    private String categoria;
    private double preco;
    private int quantidade;
    private int stockMinimo;
    private EstadoProduto estado;
    private LocalDateTime dataRegisto;

    public Produto() {
        this.estado = EstadoProduto.ACTIVO;
    }

    public Produto(String codigo, String nome, String categoria, double preco,
            int quantidade, int stockMinimo) throws ValidacaoException {
        this();
        setCodigo(codigo);
        setNome(nome);
        setCategoria(categoria);
        setPreco(preco);
        setQuantidade(quantidade);
        setStockMinimo(stockMinimo);
    }

    // ---------- Regras de negócio ----------
    public boolean isAbaixoDoMinimo() {
        return quantidade < stockMinimo;
    }

    public void adicionarStock(int qtd) throws ValidacaoException {
        if (qtd <= 0) {
            throw new ValidacaoException("A quantidade de entrada deve ser maior que zero.");
        }
        this.quantidade += qtd;
        actualizarEstado();
    }

    public void removerStock(int qtd) throws ValidacaoException, StockInsuficienteException {
        if (qtd <= 0) {
            throw new ValidacaoException("A quantidade de saída deve ser maior que zero.");
        }
        if (qtd > this.quantidade) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Disponível: " + this.quantidade + ", pedido: " + qtd + ".");
        }
        this.quantidade -= qtd;
        actualizarEstado();
    }

    // INACTIVO mantém-se; caso contrário depende da quantidade
    private void actualizarEstado() {
        if (estado == EstadoProduto.INACTIVO) {
            return;
        }
        estado = (quantidade == 0) ? EstadoProduto.ESGOTADO : EstadoProduto.ACTIVO;
    }

    // ---------- Getters e setters (com validação) ----------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) throws ValidacaoException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacaoException("O código é obrigatório.");
        }
        if (codigo.trim().length() > 20) {
            throw new ValidacaoException("O código não pode ter mais de 20 caracteres.");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws ValidacaoException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidacaoException("O nome é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = (categoria == null) ? "" : categoria.trim();
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) throws ValidacaoException {
        if (preco < 0) {
            throw new ValidacaoException("O preço não pode ser negativo.");
        }
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) throws ValidacaoException {
        if (quantidade < 0) {
            throw new ValidacaoException("A quantidade não pode ser negativa.");
        }
        this.quantidade = quantidade;
        actualizarEstado();
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) throws ValidacaoException {
        if (stockMinimo < 0) {
            throw new ValidacaoException("O stock mínimo não pode ser negativo.");
        }
        this.stockMinimo = stockMinimo;
    }

    public EstadoProduto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProduto estado) {
        this.estado = estado;
    }

    public LocalDateTime getDataRegisto() {
        return dataRegisto;
    }

    public void setDataRegisto(LocalDateTime dataRegisto) {
        this.dataRegisto = dataRegisto;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}
