package dao;

import modelo.ResumoStock;
import util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    public ResumoStock obterResumo() throws SQLException {
        ResumoStock r = new ResumoStock();

        try (Connection con = Conexao.getConexao()) {

            // Totais gerais
            String sqlTotais = "SELECT COUNT(*) AS total, "
                    + "COALESCE(SUM(quantidade),0) AS unidades, "
                    + "COALESCE(SUM(quantidade * preco),0) AS valor, "
                    + "COALESCE(SUM(quantidade < stock_minimo),0) AS abaixo, "
                    + "COALESCE(SUM(estado = 'ESGOTADO'),0) AS esgotados, "
                    + "COALESCE(SUM(estado = 'INACTIVO'),0) AS inactivos "
                    + "FROM produtos";
            try (PreparedStatement ps = con.prepareStatement(sqlTotais);
                    ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setTotalProdutos(rs.getInt("total"));
                    r.setTotalUnidades(rs.getInt("unidades"));
                    r.setValorStock(rs.getDouble("valor"));
                    r.setAbaixoMinimo(rs.getInt("abaixo"));
                    r.setEsgotados(rs.getInt("esgotados"));
                    r.setInactivos(rs.getInt("inactivos"));
                }
            }

            // Entradas e saídas dos últimos 7 dias
            String sqlMov = "SELECT tipo, COALESCE(SUM(quantidade),0) AS total FROM movimentos "
                    + "WHERE data_movimento >= NOW() - INTERVAL 7 DAY GROUP BY tipo";
            try (PreparedStatement ps = con.prepareStatement(sqlMov);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("ENTRADA".equals(rs.getString("tipo"))) {
                        r.setEntradas7Dias(rs.getInt("total"));
                    } else {
                        r.setSaidas7Dias(rs.getInt("total"));
                    }
                }
            }

            // Unidades por categoria (top 6)
            String sqlCat = "SELECT IF(categoria IS NULL OR categoria = '', 'Sem categoria', categoria) AS cat, "
                    + "SUM(quantidade) AS unidades FROM produtos "
                    + "GROUP BY cat ORDER BY unidades DESC LIMIT 6";
            Map<String, Integer> categorias = new LinkedHashMap<>();
            try (PreparedStatement ps = con.prepareStatement(sqlCat);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.put(rs.getString("cat"), rs.getInt("unidades"));
                }
            }
            r.setUnidadesPorCategoria(categorias);
        }
        return r;
    }
}