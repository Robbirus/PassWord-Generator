package UI;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private JSpinner colWebsiteWidthSpinner;
    private JSpinner colUserWidthSpinner;
    private JCheckBox autoClearClipboardCb;
    private DashboardPanel dashboardPanel;

    public SettingsPanel(DashboardPanel dashboardPanel) {
        this.dashboardPanel = dashboardPanel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Panneau vertical pour empiler les sections
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // --- SECTION 1 : Personnalisation de l'IHM ---
        JPanel uiGroup = createCategoryPanel("Affichage & Colonnes");

        uiGroup.add(new JLabel("Largeur min. colonne Site Web (px) :"));
        colWebsiteWidthSpinner = new JSpinner(new SpinnerNumberModel(150, 80, 400, 10));
        uiGroup.add(colWebsiteWidthSpinner);

        uiGroup.add(Box.createVerticalStrut(10));

        uiGroup.add(new JLabel("Largeur min. colonne Identifiant (px) :"));
        colUserWidthSpinner = new JSpinner(new SpinnerNumberModel(150, 80, 400, 10));
        uiGroup.add(colUserWidthSpinner);

        contentPanel.add(uiGroup);
        contentPanel.add(Box.createVerticalStrut(20));

        // --- SECTION 2 : Sécurité & Presse-papier ---
        JPanel secGroup = createCategoryPanel("Sécurité");

        autoClearClipboardCb = new JCheckBox("Vider automatiquement le presse-papier après 30s", true);
        secGroup.add(autoClearClipboardCb);

        contentPanel.add(secGroup);
        contentPanel.add(Box.createVerticalStrut(20));

        // --- BOUTON DE SAUVEGARDE DES OPTIONS ---
        JButton btnSave = new JButton("Appliquer les paramètres");
        btnSave.setFont(new Font("Dialog", Font.BOLD, 14));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> applySettings());

        contentPanel.add(btnSave);

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    private JPanel createCategoryPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, 0, 0, new Font("Dialog", Font.BOLD, 14)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void applySettings() {
        int webWidth = (int) colWebsiteWidthSpinner.getValue();
        int userWidth = (int) colUserWidthSpinner.getValue();

        // Application directe des largeurs au tableau
        if (dashboardPanel != null) {
            dashboardPanel.updateColumnWidths(webWidth, userWidth);
        }

        JOptionPane.showMessageDialog(this, "Paramètres enregistrés !", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean isAutoClearClipboardEnabled() {
        return autoClearClipboardCb.isSelected();
    }
}