package view;

import dao.ProdutoDAO;
import excecao.ValidacaoException;
import modelo.EstadoProduto;
import modelo.Produto;
import util.Estilo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
import java.util.List;

public class PainelProdutos extends JPanel implements Actualizavel {

    private final ProdutoDAO dao = new ProdutoDAO();

    // Formulário
    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNome = new JTextField(25);
    private final JTextField txtCategoria = new JTextField(15);
    private final JTextField txtPreco = new JTextField(10);
    private final JTextField txtQuantidade = new JTextField(10);
    private final JTextField txtMinimo = new JTextField(10);
    private final JComboBox<EstadoProduto> cmbEstado = new JComboBox<>(EstadoProduto.values());

    // Pesquisa
    private final JTextField txtPesquisa = new JTextField(25);
    private final JLabel lblResumo = new JLabel(" ");

    // Tabela
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Código", "Nome", "Categoria", "Preço (MT)", "Qtd", "Mínimo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int coluna) {
            return (coluna == 0 || coluna == 5 || coluna == 6) ? Integer.class : Object.class;
        }
    };
    private final JTable tabela = new JTable(modelo);

    private int idSeleccionado = 0; // 0 = novo produto

    public PainelProdutos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(criarFormulario(), BorderLayout.NORTH);
        add(criarZonaTabela(), BorderLayout.CENTER);

        configurarTabela();
        novo();
        recarregar();
    }

    // ================= Construção da interface =================
    private JPanel criarFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Dados do produto"));

        campo(p, "Código:", txtCodigo, 0, 0);
        campo(p, "Nome:", txtNome, 1, 0);
        campo(p, "Categoria:", txtCategoria, 0, 1);
        campo(p, "Preço (MT):", txtPreco, 1, 1);
        campo(p, "Quantidade inicial:", txtQuantidade, 0, 2);
        campo(p, "Stock mínimo:", txtMinimo, 1, 2);
        campo(p, "Estado:", cmbEstado, 0, 3);

        txtQuantidade.setToolTipText("Depois de registado, a quantidade só muda por entradas/saídas.");

        JButton btnNovo = new JButton("Novo / Limpar");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");

        btnNovo.addActionListener(e -> novo());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(btnNovo);
        botoes.add(btnGuardar);
        botoes.add(btnEliminar);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 4;
        g.fill = GridBagConstraints.HORIZONTAL;
        p.add(botoes, g);

        return p;
    }

    private void campo(JPanel p, String rotulo, JComponent c, int coluna, int linha) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridy = linha;

        g.gridx = coluna * 2;
        p.add(new JLabel(rotulo), g);

        g.gridx = coluna * 2 + 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(c, g);
    }

    private JPanel criarZonaTabela() {
        JPanel p = new JPanel(new BorderLayout(6, 6));

        JButton btnPesquisar = new JButton("Pesquisar");
        JButton btnTodos = new JButton("Listar todos");
        JButton btnAbaixo = new JButton("Abaixo do mínimo");

        btnPesquisar.addActionListener(e -> pesquisar());
        txtPesquisa.addActionListener(e -> pesquisar()); // Enter também pesquisa
        btnTodos.addActionListener(e -> {
            txtPesquisa.setText("");
            recarregar();
        });
        btnAbaixo.addActionListener(e -> listarAbaixoDoMinimo());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        barra.add(new JLabel("Pesquisar (código, nome ou categoria):"));
        barra.add(txtPesquisa);
        barra.add(btnPesquisar);
        barra.add(btnTodos);
        barra.add(btnAbaixo);

        p.add(barra, BorderLayout.NORTH);
        p.add(new JScrollPane(tabela), BorderLayout.CENTER);
        p.add(lblResumo, BorderLayout.SOUTH);
        return p;
    }

    private void configurarTabela() {
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // tabela.setRowHeight(24);
        Estilo.estilizarTabela(tabela);
        tabela.getColumnModel().getColumn(0).setMaxWidth(50);

        // Evento: ao seleccionar uma linha, preenche o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                preencherFormulario();
            }
        });

        // Destaca a vermelho os produtos abaixo do stock mínimo
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean foco, int linha, int coluna) {
                Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, linha, coluna);
                if (!seleccionada) {
                    int qtd = (Integer) modelo.getValueAt(linha, 5);
                    int min = (Integer) modelo.getValueAt(linha, 6);
                    c.setBackground(qtd < min ? new Color(255, 215, 215) : Color.WHITE);
                }
                return c;
            }
        });
    }

    // ================= Acções =================
    private void novo() {
        idSeleccionado = 0;
        txtCodigo.setText("");
        txtNome.setText("");
        txtCategoria.setText("");
        txtPreco.setText("");
        txtQuantidade.setText("0");
        txtQuantidade.setEnabled(true);
        txtMinimo.setText("5");
        cmbEstado.setSelectedItem(EstadoProduto.ACTIVO);
        tabela.clearSelection();
        txtCodigo.requestFocusInWindow();
    }

    private void preencherFormulario() {
        int id = (Integer) modelo.getValueAt(tabela.getSelectedRow(), 0);
        try {
            Produto p = dao.buscarPorId(id);
            if (p == null) {
                aviso("O produto já não existe. A lista será actualizada.");
                recarregar();
                return;
            }
            idSeleccionado = p.getId();
            txtCodigo.setText(p.getCodigo());
            txtNome.setText(p.getNome());
            txtCategoria.setText(p.getCategoria());
            txtPreco.setText(String.valueOf(p.getPreco()));
            txtQuantidade.setText(String.valueOf(p.getQuantidade()));
            txtQuantidade.setEnabled(false); // só muda por movimentos
            txtMinimo.setText(String.valueOf(p.getStockMinimo()));
            cmbEstado.setSelectedItem(p.getEstado());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void guardar() {
        try {
            double preco = lerDecimal(txtPreco.getText(), "preço");
            int qtd = lerInteiro(txtQuantidade.getText(), "quantidade");
            int min = lerInteiro(txtMinimo.getText(), "stock mínimo");

            Produto p = new Produto();
            p.setId(idSeleccionado);
            p.setCodigo(txtCodigo.getText());
            p.setNome(txtNome.getText());
            p.setCategoria(txtCategoria.getText());
            p.setPreco(preco);
            p.setQuantidade(qtd);
            p.setStockMinimo(min);
            p.setEstado(ajustarEstado((EstadoProduto) cmbEstado.getSelectedItem(), qtd));

            if (idSeleccionado == 0) {
                dao.inserir(p);
                info("Produto registado com sucesso.");
            } else {
                dao.actualizar(p);
                info("Produto actualizado com sucesso.");
            }
            novo();
            recarregar();

        } catch (ValidacaoException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            aviso("Seleccione um produto na tabela para eliminar.");
            return;
        }
        int opcao = JOptionPane.showConfirmDialog(this,
                "Eliminar o produto \"" + txtNome.getText() + "\"?\n"
                + "O histórico de movimentos deste produto também será apagado.",
                "Confirmar eliminação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcao != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.eliminar(idSeleccionado);
            info("Produto eliminado.");
            novo();
            recarregar();
        } catch (ValidacaoException e) {
            aviso(e.getMessage());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void pesquisar() {
        String termo = txtPesquisa.getText().trim();
        if (termo.isEmpty()) {
            recarregar();
            return;
        }
        try {
            carregar(dao.pesquisar(termo));
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void listarAbaixoDoMinimo() {
        try {
            carregar(dao.listarAbaixoDoMinimo());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    /**
     * Recarrega todos os produtos (chamado também pela JanelaPrincipal ao mudar
     * de aba).
     */
    @Override
    public void recarregar() {
        try {
            carregar(dao.listarTodos());
        } catch (SQLException e) {
            erroBD(e);
        }
    }

    private void carregar(List<Produto> lista) {
        modelo.setRowCount(0);
        int abaixo = 0;
        for (Produto p : lista) {
            if (p.isAbaixoDoMinimo()) {
                abaixo++;
            }
            modelo.addRow(new Object[]{
                p.getId(), p.getCodigo(), p.getNome(), p.getCategoria(),
                String.format("%.2f", p.getPreco()),
                p.getQuantidade(), p.getStockMinimo(), p.getEstado()
            });
        }
        lblResumo.setText("Produtos listados: " + lista.size()
                + "   |   Abaixo do stock mínimo: " + abaixo);
    }

    // ================= Auxiliares =================
    // INACTIVO mantém-se; senão o estado depende da quantidade
    private EstadoProduto ajustarEstado(EstadoProduto escolhido, int quantidade) {
        if (escolhido == EstadoProduto.INACTIVO) {
            return escolhido;
        }
        return (quantidade == 0) ? EstadoProduto.ESGOTADO : EstadoProduto.ACTIVO;
    }

    private int lerInteiro(String texto, String campo) throws ValidacaoException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacaoException("O campo " + campo + " deve ser um número inteiro válido.");
        }
    }

    private double lerDecimal(String texto, String campo) throws ValidacaoException {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ValidacaoException("O campo " + campo + " deve ser um número válido.");
        }
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void erroBD(SQLException e) {
        JOptionPane.showMessageDialog(this,
                "Erro na base de dados:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
