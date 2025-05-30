package com.game.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import com.game.item.Item;
import com.game.player.Player;

public class InventoryPanel extends JPanel {
    private DefaultListModel<String> inventoryModel;
    private JProgressBar healthBar;
    private JLabel statusLabel;
    private JLabel locationLabel;
    private JLabel difficultyLabel;
    private JLabel roomsExploredLabel;
    private JLabel itemCountLabel;
    private Timer pulseTimer;
    
    private static final Color PANEL_BG = new Color(0, 0, 0);
    private static final Color TEXT_COLOR = new Color(0, 255, 0);
    private static final Color BORDER_COLOR = new Color(0, 150, 0);
    private static final Color HEALTH_HIGH = new Color(0, 255, 0);
    private static final Color HEALTH_MED = new Color(255, 255, 0);
    private static final Color HEALTH_LOW = new Color(255, 0, 0);

    public InventoryPanel() {
        setPreferredSize(new Dimension(250, 600));
        setBackground(PANEL_BG);
        setBorder(createStyledBorder("Game Status"));
        setLayout(new BorderLayout(0, 10));

        // Create main panel with vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PANEL_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add inventory section
        addInventorySection(mainPanel);
        
        // Add separator
        addStyledSeparator(mainPanel);
        
        // Add status section
        addStatusSection(mainPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private Border createStyledBorder(String title) {
        Border line = BorderFactory.createLineBorder(BORDER_COLOR, 2);
        Border empty = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border compound = BorderFactory.createCompoundBorder(line, empty);

        return BorderFactory.createTitledBorder(
            compound,
            title,
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Times New Roman", Font.BOLD, 16),
            TEXT_COLOR
        );
    }

    private void addInventorySection(JPanel mainPanel) {
        JPanel inventoryPanel = new JPanel(new BorderLayout(0, 5));
        inventoryPanel.setBackground(PANEL_BG);
        inventoryPanel.setBorder(createStyledBorder("Inventory"));

        // Inventory List with custom renderer
        inventoryModel = new DefaultListModel<>();
        JList<String> inventoryList = new JList<>(inventoryModel);
        inventoryList.setBackground(PANEL_BG);
        inventoryList.setForeground(TEXT_COLOR);
        inventoryList.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        inventoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                if (isSelected) {
                    label.setBackground(new Color(0, 100, 0));
                } else {
                    label.setBackground(PANEL_BG);
                }
                return label;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(inventoryList);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(PANEL_BG);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inventoryPanel);
    }

    private void addStatusSection(JPanel mainPanel) {
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(PANEL_BG);
        statusPanel.setBorder(createStyledBorder("Character Stats"));

        // Health Bar with custom styling
        healthBar = createStyledHealthBar();
        addLabeledComponent(statusPanel, "Health:", healthBar);

        // Status indicators with icons
        statusLabel = createStyledLabel("❤"); // Heart symbol
        locationLabel = createStyledLabel("⌂"); // House symbol
        difficultyLabel = createStyledLabel("⚔"); // Crossed swords
        roomsExploredLabel = createStyledLabel("⚑"); // Flag
        itemCountLabel = createStyledLabel("⚖"); // Scales

        addLabeledComponent(statusPanel, "Status:", statusLabel);
        addLabeledComponent(statusPanel, "Location:", locationLabel);
        addLabeledComponent(statusPanel, "Difficulty:", difficultyLabel);
        addLabeledComponent(statusPanel, "Explored:", roomsExploredLabel);
        addLabeledComponent(statusPanel, "Items:", itemCountLabel);

        mainPanel.add(statusPanel);
    }

    private JProgressBar createStyledHealthBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setFont(new Font("Times New Roman", Font.BOLD, 14));
        
        // Set custom colors for the health bar
        bar.setBackground(new Color(20, 20, 20));  // Dark background
        bar.setForeground(HEALTH_HIGH);  // Start with high health color (green)
        bar.setString("100 HP - Good");
        
        // Custom UI to override default Java look
        bar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2d.setColor(bar.getBackground());
                g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
                
                // Draw progress
                g2d.setColor(bar.getForeground());
                int width = (int) ((c.getWidth() * bar.getPercentComplete()));
                g2d.fillRect(0, 0, width, c.getHeight());
                
                // Draw string
                if (bar.isStringPainted()) {
                    g2d.setColor(TEXT_COLOR);
                    String progressString = bar.getString();
                    g2d.setFont(bar.getFont());
                    
                    FontMetrics fm = g2d.getFontMetrics();
                    int stringWidth = fm.stringWidth(progressString);
                    int stringHeight = fm.getHeight();
                    
                    int x = (c.getWidth() - stringWidth) / 2;
                    int y = ((c.getHeight() - stringHeight) / 2) + fm.getAscent();
                    
                    g2d.drawString(progressString, x, y);
                }
            }
        });
        
        // More prominent border and size
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        
        // Larger size for better visibility
        bar.setPreferredSize(new Dimension(200, 25));
        bar.setOpaque(true);
        
        return bar;
    }

    private JLabel createStyledLabel(String icon) {
        JLabel label = new JLabel(icon + " ");
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        return label;
    }

    private void addStyledSeparator(JPanel panel) {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(PANEL_BG);
        separator.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(separator);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void addLabeledComponent(JPanel panel, String labelText, JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout(10, 0));
        wrapper.setBackground(PANEL_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Times New Roman", Font.BOLD, 12));
        
        wrapper.add(label, BorderLayout.WEST);
        wrapper.add(component, BorderLayout.CENTER);
        
        panel.add(wrapper);
    }

    public void updateInventory(List<Item> items) {
        SwingUtilities.invokeLater(() -> {
            inventoryModel.clear();
            if (items != null) {
                for (Item item : items) {
                    inventoryModel.addElement(item.getName());
                }
                itemCountLabel.setText("⚖ " + items.size());
            }
            revalidate();
            repaint();
        });
    }

    public void updateStatus(Player player) {
        if (player == null) return;
        
        SwingUtilities.invokeLater(() -> {
            int health = player.getPowerPoints();
            healthBar.setValue(health);
            updateHealthBarAppearance(health);
            
            // Update labels in one go
            statusLabel.setText("❤ " + player.getStatus());
            locationLabel.setText("⌂ Room " + player.getLocation().getRoomId());
            difficultyLabel.setText("⚔ " + player.getDifficultyLevel());
            roomsExploredLabel.setText("⚑ " + player.getVisitedRooms().size());
            itemCountLabel.setText("⚖ " + player.getInventory().size());
            
            revalidate();
            repaint();
        });
    }

    private void updateHealthBarAppearance(int health) {
        if (health < 25) {
            healthBar.setForeground(HEALTH_LOW);
            healthBar.setString(health + " HP - Critical!");
            startHealthBarPulsing();
        } else if (health < 50) {
            healthBar.setForeground(HEALTH_MED);
            healthBar.setString(health + " HP - Warning!");
            stopHealthBarPulsing();
        } else {
            healthBar.setForeground(HEALTH_HIGH);
            healthBar.setString(health + " HP - Good");
            stopHealthBarPulsing();
        }
    }

    private void startHealthBarPulsing() {
        if (pulseTimer == null) {
            pulseTimer = new Timer(500, e -> healthBar.setForeground(
                healthBar.getForeground().equals(HEALTH_LOW) ?
                HEALTH_MED : HEALTH_LOW
            ));
            pulseTimer.start();
        }
    }

    private void stopHealthBarPulsing() {
        if (pulseTimer != null) {
            pulseTimer.stop();
            pulseTimer = null;
        }
    }

    public void reset() {
        SwingUtilities.invokeLater(() -> {
            inventoryModel.clear();
            healthBar.setValue(100);
            healthBar.setString("100 HP - Good");
            healthBar.setForeground(HEALTH_HIGH);
            stopHealthBarPulsing();
            
            // Reset labels
            statusLabel.setText("Healthy");
            locationLabel.setText("Starting Room");
            difficultyLabel.setText("Normal");
            roomsExploredLabel.setText("0");
            itemCountLabel.setText("0");
            
            revalidate();
            repaint();
        });
    }

    public void cleanup() {
        stopHealthBarPulsing();
    }
} 