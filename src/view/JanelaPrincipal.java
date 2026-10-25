package view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Component;

public class JanelaPrincipal extends JFrame {

    public JanelaPrincipal() {
        super("Gestão de Stock");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dashboard", new PainelDashboard()); // tela inicial
        abas.addTab("Produtos", new PainelProdutos());
        abas.addTab("Movimentos", new PainelMovimentos());
        abas.addTab("Alertas de stock", new PainelAlertas());

        // Evento: ao mudar de aba, o painel volta a ler os dados da BD
        abas.addChangeListener(e -> {
            Component c = abas.getSelectedComponent();
            if (c instanceof Actualizavel) {
                ((Actualizavel) c).recarregar();
            }
        });

        add(abas);
    }
}