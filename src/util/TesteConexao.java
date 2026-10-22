package util;

import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {
        try (Connection con = Conexao.getConexao()) {
            System.out.println("Ligacao bem sucedida: " + con.getCatalog());
        } catch (SQLException e) {
            System.err.println("Erro ao ligar a BD: " + e.getMessage());
        }
    }
}
