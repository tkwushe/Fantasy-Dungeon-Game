package com.game.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import com.game.engine.GameEngine;
import com.game.util.LogManager;
import com.game.item.HealingItem;
import com.game.item.Item;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class GameWindowTest {
    private GameWindow gameWindow;
    private GameEngine gameEngine;
    private static final int TIMEOUT_SECONDS = 10;
    private JTextArea outputArea;

    @BeforeEach
    void setUp() throws IOException, InterruptedException, InvocationTargetException {
        try {
            LogManager.initialize(); // Initialize logging
            
            CountDownLatch initLatch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    gameWindow = GameWindow.getInstance();
                    gameEngine = GameEngine.getInstance();

                    Field outputAreaField = GameWindow.class.getDeclaredField("outputArea");
                    outputAreaField.setAccessible(true);
                    outputArea = (JTextArea) outputAreaField.get(gameWindow);

                    gameEngine.startGame();
                    gameEngine.processCommand("2"); // Select normal difficulty

                    gameWindow.setVisible(true);
                    gameWindow.toFront();

                    initLatch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize game window", e);
                }
            });
            if (!initLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for window initialization");
            }
            Thread.sleep(1000); // Wait for visibility

            SwingUtilities.invokeAndWait(() -> outputArea.setText(""));
        } catch (Exception e) {
            throw new RuntimeException("Test setup failed", e);
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        CountDownLatch disposeLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            if (gameWindow != null) {
                gameWindow.setVisible(false);
                gameWindow.dispose();
            }
            disposeLatch.countDown();
        });
        disposeLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void testWindowInitialization() throws Exception {
        CountDownLatch testLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                assertNotNull(gameWindow, "GameWindow should be initialized");
                assertTrue(gameWindow.isVisible(), "Window should be visible");
                assertEquals("Dungeon Adventure", gameWindow.getTitle());
            } finally {
                testLatch.countDown();
            }
        });
        assertTrue(testLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testCommandInput() throws Exception {
        CountDownLatch testLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                Field commandInputField;
                JTextField commandInput;
                try {
                    commandInputField = GameWindow.class.getDeclaredField("commandInput");
                    commandInputField.setAccessible(true);
                    commandInput = (JTextField) commandInputField.get(gameWindow);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access command input field", e);
                }

                assertNotNull(commandInput, "Command input should exist");

                commandInput.setText("help");
                commandInput.postActionEvent();
                commandInput.setText(""); // Clear input
            } finally {
                testLatch.countDown();
            }
        });
        assertTrue(testLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testGameStateReset() throws Exception {
        SwingUtilities.invokeAndWait(() -> outputArea.setText("Some initial text"));

        CountDownLatch resetLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                gameWindow.resetGameState();
                Thread.sleep(500);
                String currentText = outputArea.getText().trim();
                assertTrue(currentText.isEmpty(), "Output area should be cleared after reset");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Test interrupted", e);
            } finally {
                resetLatch.countDown();
            }
        });
        assertTrue(resetLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testMapPanelAndInventoryPanel() throws Exception {
        CountDownLatch testLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                // Test MapPanel
                MapPanel mapPanel = gameWindow.getMapPanel();
                assertNotNull(mapPanel, "Map panel should exist");
                assertTrue(mapPanel.getPreferredSize().width > 0, "Map panel should have width");
                assertTrue(mapPanel.getPreferredSize().height > 0, "Map panel should have height");

                // Test InventoryPanel
                Field inventoryPanelField;
                InventoryPanel inventoryPanel;
                try {
                    inventoryPanelField = GameWindow.class.getDeclaredField("inventoryPanel");
                    inventoryPanelField.setAccessible(true);
                    inventoryPanel = (InventoryPanel) inventoryPanelField.get(gameWindow);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Failed to access inventory panel field", e);
                }

                assertNotNull(inventoryPanel, "Inventory panel should exist");

                List<Item> testItems = List.of(new HealingItem("Test Potion", "A test healing item", 10, false));
                inventoryPanel.updateInventory(testItems);
            } finally {
                testLatch.countDown();
            }
        });
        assertTrue(testLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testMenuBar() throws Exception {
        CountDownLatch testLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                JMenuBar menuBar = gameWindow.getJMenuBar();
                assertNotNull(menuBar, "Menu bar should exist");

                JMenu gameMenu = null;
                for (int i = 0; i < menuBar.getMenuCount(); i++) {
                    if (menuBar.getMenu(i).getText().equals("Game")) {
                        gameMenu = menuBar.getMenu(i);
                        break;
                    }
                }
                assertNotNull(gameMenu, "Game menu should exist");

                boolean hasSaveOption = false;
                boolean hasLoadOption = false;

                for (int i = 0; i < gameMenu.getItemCount(); i++) {
                    JMenuItem item = gameMenu.getItem(i);
                    if (item.getText().equals("Save Game")) hasSaveOption = true;
                    if (item.getText().equals("Load Game")) hasLoadOption = true;
                }
                assertTrue(hasSaveOption, "Should have Save Game option");
                assertTrue(hasLoadOption, "Should have Load Game option");
            } finally {
                testLatch.countDown();
            }
        });
        assertTrue(testLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }

    @Test
    void testWindowSize() throws Exception {
        CountDownLatch testLatch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                Dimension size = gameWindow.getSize();
                assertTrue(size.width >= 1024, "Window width should be at least 1024");
                assertTrue(size.height >= 768, "Window height should be at least 768");
            } finally {
                testLatch.countDown();
            }
        });
        assertTrue(testLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Test timed out");
    }
}
