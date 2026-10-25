package util;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public final class Estilo {
    // Paleta
    public static final Color COR_CABECALHO = new Color(0, 0, 0);
    public static final Color COR_SELECCAO = new Color(190, 215, 250);
    public static final Color COR_GRELHA = new Color(225, 225, 225);
    // Tipos de letra
    private static final String FAMILIA = "Segoe UI";
    private static final Font FONTE_BASE = new Font(FAMILIA, Font.PLAIN, 13);
    private static final Font FONTE_CABECALHO = new Font(FAMILIA, Font.BOLD, 15);

    private Estilo() {
    }

    /**
     * Aumenta o tipo de letra de toda a aplicação. Chamar DEPOIS de definir o Look
     * and Feel.
     */
    public static void aplicarTema() {
        String[] chaves = {
                "Label.font", "TextField.font", "Button.font", "ComboBox.font",
                "CheckBox.font", "Table.font", "TableHeader.font", "TitledBorder.font",
                "TabbedPane.font", "ToolTip.font", "OptionPane.messageFont", "OptionPane.buttonFont"
        };
        for (String chave : chaves) {
            UIManager.put(chave, new FontUIResource(FONTE_BASE));
        }
    }

    /**
     * Estilo comum das tabelas: linhas maiores, cabeçalho colorido e grelha suave.
     */
    public static void estilizarTabela(JTable tabela) {
        tabela.setFont(FONTE_BASE);
        tabela.setRowHeight(30);
        tabela.setShowVerticalLines(true);
        tabela.setGridColor(COR_GRELHA);
        tabela.setSelectionBackground(COR_SELECCAO);
        tabela.setSelectionForeground(Color.BLACK);

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new Dimension(0, 30));

        // Renderer próprio: funciona em qualquer Look and Feel
        cabecalho.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean seleccionada, boolean foco, int linha, int coluna) {
                super.getTableCellRendererComponent(t, valor, seleccionada, foco, linha, coluna);
                setOpaque(true);
                setBackground(COR_CABECALHO);
                setForeground(Color.WHITE);
                setFont(FONTE_CABECALHO);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(70)),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                return this;
            }
        });
    }
}
