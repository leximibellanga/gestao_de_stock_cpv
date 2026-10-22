package util;

import dao.MovimentoDAO;
import dao.ProdutoDAO;
import excecao.StockInsuficienteException;
import excecao.ValidacaoException;
import modelo.Movimento;
import modelo.Produto;
import modelo.TipoMovimento;

public class TesteDAO {

    public static void main(String[] args) {
        ProdutoDAO pdao = new ProdutoDAO();
        MovimentoDAO mdao = new MovimentoDAO();
        try {
            System.out.println("== Todos os produtos ==");
            for (Produto p : pdao.listarTodos()) {
                System.out.println(p.getId() + " | " + p + " | qtd=" + p.getQuantidade()
                        + " | min=" + p.getStockMinimo() + " | " + p.getEstado());
            }

            System.out.println("== Abaixo do mínimo ==");
            for (Produto p : pdao.listarAbaixoDoMinimo()) {
                System.out.println(p + " (qtd " + p.getQuantidade() + ")");
            }

            Produto primeiro = pdao.listarTodos().get(0);

            mdao.registar(new Movimento(primeiro.getId(), TipoMovimento.ENTRADA, 10, "Teste entrada"));
            System.out.println("Entrada registada. Nova qtd: " + pdao.buscarPorId(primeiro.getId()).getQuantidade());

            mdao.registar(new Movimento(primeiro.getId(), TipoMovimento.SAIDA, 99999, "Teste saída"));

        } catch (StockInsuficienteException e) {
            System.out.println("Erro esperado -> " + e.getMessage());
        } catch (ValidacaoException e) {
            System.out.println("Validação -> " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro de BD: " + e.getMessage());
        }
    }
}
