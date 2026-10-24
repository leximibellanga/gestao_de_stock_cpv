package view;

import dao.ProdutoDAO;
import modelo.Produto;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

public class PainelAlertas extends JPanel implements Actualizavel {

    private final ProdutoDAO dao = new ProdutoDAO();

    private final JLabel lblResumo = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Nome", "Categoria", "Qtd actual", "Mínimo", "Em falta", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);

    public PainelAlertas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> recarregar());

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(btnActualizar);
        topo.add(lblResumo);

        tabela.setRowHeight(24);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        recarregar();
    }

    @Override
    public void recarregar() {
        try {
            List<Produto> lista = dao.listarAbaixoDoMinimo();
            modelo.setRowCount(0);
            for (Produto p : lista) {
                modelo.addRow(new Object[]{
                    p.getCodigo(), p.getNome(), p.getCategoria(),
                    p.getQuantidade(), p.getStockMinimo(),
                    p.getStockMinimo() - p.getQuantidade(), p.getEstado()
                });
            }
            lblResumo.setText(lista.isEmpty()
                    ? "Nenhum produto abaixo do stock mínimo."
                    : "Produtos que precisam de reposição: " + lista.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro na base de dados:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
