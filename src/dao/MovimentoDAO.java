package dao;

import excecao.StockInsuficienteException;
import excecao.ValidacaoException;
import modelo.EstadoProduto;
import modelo.Movimento;
import modelo.Produto;
import modelo.TipoMovimento;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovimentoDAO {

    /**
     * Regista uma entrada/saída numa única transacção: 
     * 1) bloqueia a linha do produto, 
     * 2) aplica a regra no modelo, 
     * 3) actualiza o produto, 
     * 4) grava o movimento. Se falhar algo, faz rollback.
     */
    public void registar(Movimento m)
            throws SQLException, ValidacaoException, StockInsuficienteException {

        try (Connection con = Conexao.getConexao()) {
            con.setAutoCommit(false);
            try {
                Produto p;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM produtos WHERE id=? FOR UPDATE")) {
                    ps.setInt(1, m.getProdutoId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ValidacaoException("Produto não encontrado.");
                        }
                        p = ProdutoDAO.mapear(rs);
                    }
                }

                if (p.getEstado() == EstadoProduto.INACTIVO) {
                    throw new ValidacaoException("Não é possível movimentar um produto INACTIVO.");
                }

                if (m.getTipo() == TipoMovimento.ENTRADA) {
                    p.adicionarStock(m.getQuantidade());
                } else {
                    p.removerStock(m.getQuantidade());
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE produtos SET quantidade=?, estado=? WHERE id=?")) {
                    ps.setInt(1, p.getQuantidade());
                    ps.setString(2, p.getEstado().name());
                    ps.setInt(3, p.getId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO movimentos (produto_id, tipo, quantidade, observacao) VALUES (?,?,?,?)")) {
                    ps.setInt(1, m.getProdutoId());
                    ps.setString(2, m.getTipo().name());
                    ps.setInt(3, m.getQuantidade());
                    ps.setString(4, m.getObservacao());
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException | ValidacaoException | StockInsuficienteException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Movimento> listarTodos() throws SQLException {
        String sql = "SELECT m.*, p.nome AS produto_nome FROM movimentos m "
                + "JOIN produtos p ON p.id = m.produto_id ORDER BY m.data_movimento DESC, m.id DESC";
        return consultar(sql, 0);
    }

    public List<Movimento> listarPorProduto(int produtoId) throws SQLException {
        String sql = "SELECT m.*, p.nome AS produto_nome FROM movimentos m "
                + "JOIN produtos p ON p.id = m.produto_id WHERE m.produto_id=? "
                + "ORDER BY m.data_movimento DESC, m.id DESC";
        return consultar(sql, produtoId);
    }

    private List<Movimento> consultar(String sql, int produtoId) throws SQLException {
        List<Movimento> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (produtoId > 0) {
                ps.setInt(1, produtoId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movimento m = new Movimento();
                    m.setId(rs.getInt("id"));
                    m.setProdutoId(rs.getInt("produto_id"));
                    m.setProdutoNome(rs.getString("produto_nome"));
                    m.setTipo(TipoMovimento.valueOf(rs.getString("tipo")));
                    m.setQuantidade(rs.getInt("quantidade"));
                    m.setObservacao(rs.getString("observacao"));
                    Timestamp ts = rs.getTimestamp("data_movimento");
                    if (ts != null) {
                        m.setDataMovimento(ts.toLocalDateTime());
                    }
                    lista.add(m);
                }
            }
        }
        return lista;
    }
}
