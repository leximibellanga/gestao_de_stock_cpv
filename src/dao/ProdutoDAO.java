package dao;

import excecao.ValidacaoException;
import modelo.EstadoProduto;
import modelo.Produto;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    // ---------- CREATE ----------
    public void inserir(Produto p) throws SQLException, ValidacaoException {
        if (existeCodigo(p.getCodigo(), 0)) {
            throw new ValidacaoException("Já existe um produto com o código " + p.getCodigo() + ".");
        }
        String sql = "INSERT INTO produtos (codigo, nome, categoria, preco, quantidade, stock_minimo, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getCategoria());
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getQuantidade());
            ps.setInt(6, p.getStockMinimo());
            ps.setString(7, p.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        }
    }

    // ---------- UPDATE ----------
    // A quantidade NÃO é alterada aqui: só muda através de entradas/saídas.
    public void actualizar(Produto p) throws SQLException, ValidacaoException {
        if (existeCodigo(p.getCodigo(), p.getId())) {
            throw new ValidacaoException("Já existe outro produto com o código " + p.getCodigo() + ".");
        }
        String sql = "UPDATE produtos SET codigo=?, nome=?, categoria=?, preco=?, stock_minimo=?, estado=? "
                + "WHERE id=?";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getCategoria());
            ps.setDouble(4, p.getPreco());
            ps.setInt(5, p.getStockMinimo());
            ps.setString(6, p.getEstado().name());
            ps.setInt(7, p.getId());
            if (ps.executeUpdate() == 0) {
                throw new ValidacaoException("Produto não encontrado (pode ter sido eliminado).");
            }
        }
    }

    // ---------- DELETE ----------
    public void eliminar(int id) throws SQLException, ValidacaoException {
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement("DELETE FROM produtos WHERE id=?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new ValidacaoException("Produto não encontrado.");
            }
        }
    }

    // ---------- READ ----------
    public Produto buscarPorId(int id) throws SQLException {
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement("SELECT * FROM produtos WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Produto> listarTodos() throws SQLException {
        return consultar("SELECT * FROM produtos ORDER BY codigo, nome", null);
    }

    public List<Produto> pesquisar(String termo) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE codigo LIKE ? OR nome LIKE ? OR categoria LIKE ? "
                + "ORDER BY nome";
        return consultar(sql, "%" + termo.trim() + "%");
    }

    public List<Produto> listarAbaixoDoMinimo() throws SQLException {
        return consultar("SELECT * FROM produtos WHERE quantidade < stock_minimo ORDER BY quantidade", null);
    }

    // ---------- Auxiliares ----------
    public boolean existeCodigo(String codigo, int idIgnorar) throws SQLException {
        String sql = "SELECT COUNT(*) FROM produtos WHERE codigo=? AND id<>?";
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, idIgnorar);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // Se 'termo' != null, é aplicado aos 3 parâmetros do LIKE (pesquisa)
    private List<Produto> consultar(String sql, String termo) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (termo != null) {
                ps.setString(1, termo);
                ps.setString(2, termo);
                ps.setString(3, termo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // Converte uma linha da BD num objecto Produto
    static Produto mapear(ResultSet rs) throws SQLException {
        try {
            Produto p = new Produto();
            p.setId(rs.getInt("id"));
            p.setCodigo(rs.getString("codigo"));
            p.setNome(rs.getString("nome"));
            p.setCategoria(rs.getString("categoria"));
            p.setPreco(rs.getDouble("preco"));
            p.setQuantidade(rs.getInt("quantidade"));
            p.setStockMinimo(rs.getInt("stock_minimo"));
            p.setEstado(EstadoProduto.valueOf(rs.getString("estado"))); // depois da quantidade
            Timestamp ts = rs.getTimestamp("data_registo");
            if (ts != null) {
                p.setDataRegisto(ts.toLocalDateTime());
            }
            return p;
        } catch (ValidacaoException e) {
            throw new SQLException("Dados inválidos na base de dados: " + e.getMessage(), e);
        }
    }
}
