package view;

import dao.MovimentoDAO;
import dao.ProdutoDAO;
import excecao.StockInsuficienteException;
import excecao.ValidacaoException;
import modelo.EstadoProduto;
import modelo.Movimento;
import modelo.Produto;
import modelo.TipoMovimento;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PainelMovimentos extends JPanel implements Actualizavel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoDAO movimentoDAO = new MovimentoDAO();

    private final JComboBox<Produto> cmbProduto = new JComboBox<>();
    private final JComboBox<TipoMovimento> cmbTipo = new JComboBox<>(TipoMovimento.values());
    private final JTextField txtQuantidade = new JTextField(8);
    private final JTextField txtObservacao = new JTextField(25);
    private final JLabel lblStockActual = new JLabel(" ");
    private final JCheckBox chkFiltrar = new JCheckBox("Mostrar só o histórico do produto seleccionado");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Data", "Produto", "Tipo", "Qtd", "Observação"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);

    private boolean aCarregar = false; // evita eventos enquanto o combo é preenchido

    public PainelMovimentos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(criarFormulario(), BorderLayout.NORTH);
        add(criarZonaHistorico(), BorderLayout.CENTER);

        configurarComponentes();
        recarregar();
    }

    // ================= Construção da interface =================
    private JPanel criarFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Registar movimento de stock"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        p.add(new JLabel("Produto:"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(cmbProduto, g);

        g.gridwidth = 1;
        g.weightx = 0;
        g.fill = GridBagConstraints.NONE;
        g.gridx = 0;
        g.gridy = 1;
        p.add(new JLabel("Tipo:"), g);
        g.gridx = 1;
        p.add(cmbTipo, g);
        g.gridx = 2;
        p.add(new JLabel("Quantidade:"), g);
        g.gridx = 3;
        p.add(txtQuantidade, g);

        g.gridx = 0;
        g.gridy = 2;
        p.add(new JLabel("Observação:"), g);
        g.gridx = 1;
        g.gridwidth = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(txtObservacao, g);

        g.gridx = 0;
        g.gridy = 3;
        g.gridwidth = 4;
        p.add(lblStockActual, g);

        JButton btnRegistar = new JButton("Registar movimento");
        btnRegistar.addActionListener(e -> registar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(btnRegistar);
        g.gridy = 4;
        p.add(botoes, g);

        return p;
    }

    private JPanel criarZonaHistorico() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Histórico de movimentos"));
        p.add(chkFiltrar, BorderLayout.NORTH);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return p;
    }

    private void configurarComponentes() {
        tabela.setRowHeight(24);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);

        // Mostra "codigo - nome (stock: X)" no combo
        cmbProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Produto) {
                    Produto p = (Produto) valor;
                    setText(p.getCodigo() + " - " + p.getNome() + " (stock: " + p.getQuantidade() + ")");
                }
                return this;
            }
        });

        // Eventos
        cmbProduto.addActionListener(e -> {
            if (!aCarregar) {
                actualizarStockActual();
                if (chkFiltrar.isSelected()) {
                    carregarHistorico();
                }
            }
        });
        chkFiltrar.addActionListener(e -> carregarHistorico());

        // Cor por tipo de movimento
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean foco, int linha, int coluna) {
                Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, linha, coluna);
                if (!seleccionada) {
                    Object tipo = modelo.getValueAt(linha, 3);
                    c.setBackground(tipo == TipoMovimento.ENTRADA
                            ? new Color(220, 245, 220) : new Color(255, 235, 205));
                }
                return c;
            }
        });
    }

    // ================= Acções =================
    private void registar() {
        Produto seleccionado = (Produto) cmbProduto.getSelectedItem();
        if (seleccionado == null) {
            aviso("Seleccione um produto.");
            return;
        }
        try {
            int qtd = lerInteiro(txtQuantidade.getText());
            TipoMovimento tipo = (TipoMovimento) cmbTipo.getSelectedItem();

            movimentoDAO.registar(new Movimento(
                    seleccionado.getId(), tipo, qtd, txtObservacao.getText().trim()));

            Produto actual = produtoDAO.buscarPorId(seleccionado.getId());
            String msg = tipo + " registada com sucesso.\nStock actual de "
                    + actual.getNome() + ": " + actual.getQuantidade();

            if (actual.getQuantidade() == 0) {
                msg += "\n\nATENÇÃO: o produto ficou ESGOTADO.";
            } else if (actual.isAbaixoDoMinimo()) {
                msg += "\n\nATENÇÃO: stock abaixo do mínimo (" + actual.getStockMinimo() + ").";
            }
            JOptionPane.showMessageDialog(this, msg, "Movimento registado",
                    actual.isAbaixoDoMinimo() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

            txtQuantidade.setText("");
            txtObservacao.setText("");
            recarregar();

        } catch (StockInsuficienteException | ValidacaoException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    @Override
    public void recarregar() {
        aCarregar = true;
        try {
            Produto anterior = (Produto) cmbProduto.getSelectedItem();
            int idAnterior = (anterior == null) ? 0 : anterior.getId();

            cmbProduto.removeAllItems();
            Produto reposto = null;
            for (Produto p : produtoDAO.listarTodos()) {
                if (p.getEstado() != EstadoProduto.INACTIVO) { // inactivos não movimentam
                    cmbProduto.addItem(p);
                    if (p.getId() == idAnterior) {
                        reposto = p;
                    }
                }
            }
            if (reposto != null) {
                cmbProduto.setSelectedItem(reposto);
            }
        } catch (SQLException e) {
            erroBD(e);
        } finally {
            aCarregar = false;
        }
        actualizarStockActual();
        carregarHistorico();
    }

    private void carregarHistorico() {
        try {
            List<Movimento> lista;
            Produto p = (Produto) cmbProduto.getSelectedItem();
            if (chkFiltrar.isSelected() && p != null) {
                lista = movimentoDAO.listarPorProduto(p.getId());
            } else {
                lista = movimentoDAO.listarTodos();
            }
            modelo.setRowCount(0);
            for (Movimento m : lista) {
                modelo.addRow(new Object[]{
                    m.getId(),
                    m.getDataMovimento() == null ? "" : m.getDataMovimento().format(FORMATO),
                    m.getProdutoNome(), m.getTipo(), m.getQuantidade(), m.getObservacao()
                });
            }
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void actualizarStockActual() {
        Produto p = (Produto) cmbProduto.getSelectedItem();
        if (p == null) {
            lblStockActual.setText("Sem produtos disponíveis para movimentar.");
        } else {
            lblStockActual.setText("Stock actual: " + p.getQuantidade()
                    + "   |   Mínimo: " + p.getStockMinimo()
                    + "   |   Estado: " + p.getEstado());
        }
    }

    // ================= Auxiliares =================
    private int lerInteiro(String texto) throws ValidacaoException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException("A quantidade deve ser um número inteiro válido.");
        }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void erroBD(SQLException e) {
        JOptionPane.showMessageDialog(this, "Erro na base de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
