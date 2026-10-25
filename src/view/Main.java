package view;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import util.Estilo;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // se falhar, usa o aspecto padrão do Swing
        }
        Estilo.aplicarTema(); // depois do Look and Feel
        
        // Swing deve arrancar na Event Dispatch Threadcls
        SwingUtilities.invokeLater(() -> new JanelaPrincipal().setVisible(true));
    }
}
