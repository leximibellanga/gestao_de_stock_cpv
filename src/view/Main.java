package view;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se falhar, usa o aspecto padrão do Swing
        }
        // Swing deve arrancar na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
