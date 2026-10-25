package view;

import dao.DashboardDAO;
import dao.MovimentoDAO;
import dao.ProdutoDAO;
import modelo.Movimento;
import modelo.Produto;
import modelo.ResumoStock;
import util.Estilo;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PainelDashboard extends JPanel implements Actualizavel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final int LINHAS_TABELA = 8;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoDAO movimentoDAO = new MovimentoDAO();

    // Valores dos cartões
    private final JLabel lblProdutos = new JLabel("0");
    private final JLabel lblUnidades = new JLabel("0");
    private final JLabel lblValor = new JLabel("0.00 MT");
    private final JLabel lblAbaixo = new JLabel("0");
    private final JLabel lblEsgotados = new JLabel("0");

    private final JLabel lblRodape = new JLabel(" ");
    private final GraficoCategorias grafico = new GraficoCategorias();

    private final DefaultTableModel modeloCriticos = novoModelo(
            new String[] { "Código", "Produto", "Qtd", "Mínimo", "Estado" });
    private final DefaultTableModel modeloMovimentos = novoModelo(
            new String[] { "Data", "Produto", "Tipo", "Qtd" });

    public PainelDashboard() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(criarCartoes(), BorderLayout.NORTH);
        add(criarCentro(), BorderLayout.CENTER);
        add(lblRodape, BorderLayout.SOUTH);

        recarregar();
    }

    // ================= Construção da interface =================

    private JPanel criarCartoes() {
        JPanel p = new JPanel(new GridLayout(1, 5, 10, 10));
        p.add(criarCartao("Total de produtos", lblProdutos, new Color(52, 120, 220)));
        p.add(criarCartao("Unidades em stock", lblUnidades, new Color(46, 160, 90)));
        p.add(criarCartao("Valor do stock", lblValor, new Color(120, 90, 200)));
        p.add(criarCartao("Abaixo do mínimo", lblAbaixo, new Color(210, 50, 50)));
        p.add(criarCartao("Esgotados", lblEsgotados, new Color(230, 140, 20)));
        return p;
    }

    private JPanel criarCartao(String titulo, JLabel valor, Color cor) {
        JPanel c = new JPanel(new BorderLayout(0, 4));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, cor),
                BorderFactory.createEmptyBorder(12, 14, 12, 10)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(Color.GRAY);

        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 24f));
        valor.setForeground(cor);

        c.add(lblTitulo, BorderLayout.NORTH);
        c.add(valor, BorderLayout.CENTER);
        return c;
    }

    private JPanel criarCentro() {
        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.setBorder(BorderFactory.createTitledBorder("Unidades em stock por categoria"));
        esquerda.add(grafico, BorderLayout.CENTER);

        JPanel direita = new JPanel(new GridLayout(2, 1, 10, 10));
        direita.add(criarTabela("Produtos críticos (abaixo do mínimo)", modeloCriticos));
        direita.add(criarTabela("Últimos movimentos", modeloMovimentos));

        centro.add(esquerda);
        centro.add(direita);
        return centro;
    }

    private JScrollPane criarTabela(String titulo, DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        Estilo.estilizarTabela(t);
        // t.setRowHeight(24);
        t.setEnabled(false); // só leitura
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder(titulo));
        return sp;
    }

    private static DefaultTableModel novoModelo(String[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
    }

    // ================= Dados =================

    @Override
    public void recarregar() {
        try {
            ResumoStock r = dashboardDAO.obterResumo();

            lblProdutos.setText(String.valueOf(r.getTotalProdutos()));
            lblUnidades.setText(String.valueOf(r.getTotalUnidades()));
            lblValor.setText(String.format("%,.2f MT", r.getValorStock()));
            lblAbaixo.setText(String.valueOf(r.getAbaixoMinimo()));
            lblEsgotados.setText(String.valueOf(r.getEsgotados()));

            lblRodape.setText("Últimos 7 dias:  entradas " + r.getEntradas7Dias()
                    + " un.   |   saídas " + r.getSaidas7Dias() + " un.   |   Produtos inactivos: "
                    + r.getInactivos());

            grafico.setDados(r.getUnidadesPorCategoria());

            // Produtos críticos (os mais baixos primeiro)
            modeloCriticos.setRowCount(0);
            List<Produto> criticos = produtoDAO.listarAbaixoDoMinimo();
            for (int i = 0; i < Math.min(LINHAS_TABELA, criticos.size()); i++) {
                Produto p = criticos.get(i);
                modeloCriticos.addRow(new Object[] {
                        p.getCodigo(), p.getNome(), p.getQuantidade(), p.getStockMinimo(), p.getEstado()
                });
            }

            // Últimos movimentos
            modeloMovimentos.setRowCount(0);
            List<Movimento> movs = movimentoDAO.listarTodos();
            for (int i = 0; i < Math.min(LINHAS_TABELA, movs.size()); i++) {
                Movimento m = movs.get(i);
                modeloMovimentos.addRow(new Object[] {
                        m.getDataMovimento() == null ? "" : m.getDataMovimento().format(FORMATO),
                        m.getProdutoNome(), m.getTipo(), m.getQuantidade()
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar o dashboard:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= Gráfico de barras (Java2D) =================

    private static class GraficoCategorias extends JPanel {

        private Map<String, Integer> dados = new java.util.LinkedHashMap<>();
        private final Color[] cores = {
                new Color(52, 120, 220), new Color(46, 160, 90), new Color(230, 140, 20),
                new Color(120, 90, 200), new Color(210, 50, 50), new Color(0, 150, 160)
        };

        GraficoCategorias() {
            setBackground(Color.WHITE);
        }

        void setDados(Map<String, Integer> novos) {
            this.dados = novos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics();

            if (dados.isEmpty()) {
                String msg = "Sem dados para mostrar.";
                g2.setColor(Color.GRAY);
                g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }

            int max = 1;
            int larguraRotulo = 0;
            for (Map.Entry<String, Integer> e : dados.entrySet()) {
                max = Math.max(max, e.getValue());
                larguraRotulo = Math.max(larguraRotulo, fm.stringWidth(e.getKey()));
            }

            int margem = 14;
            int xBarra = margem + larguraRotulo + 10;
            int larguraMax = getWidth() - xBarra - margem - 50; // espaço para o valor
            int n = dados.size();
            int alturaLinha = Math.min(46, (getHeight() - 2 * margem) / n);
            int alturaBarra = Math.max(12, alturaLinha - 16);

            int i = 0;
            for (Map.Entry<String, Integer> e : dados.entrySet()) {
                int y = margem + i * alturaLinha;
                int largura = (int) ((double) e.getValue() / max * Math.max(larguraMax, 10));

                g2.setColor(Color.DARK_GRAY);
                g2.drawString(e.getKey(), margem, y + (alturaBarra + fm.getAscent()) / 2 - 2);

                g2.setColor(cores[i % cores.length]);
                g2.fillRoundRect(xBarra, y, Math.max(largura, 3), alturaBarra, 8, 8);

                g2.setColor(Color.DARK_GRAY);
                g2.drawString(String.valueOf(e.getValue()), xBarra + Math.max(largura, 3) + 6,
                        y + (alturaBarra + fm.getAscent()) / 2 - 2);
                i++;
            }

            // Eixo vertical
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(xBarra - 2, margem - 4, xBarra - 2, margem + n * alturaLinha - 6);
            g2.dispose();
        }
    }
}