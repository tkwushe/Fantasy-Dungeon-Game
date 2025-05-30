package com.game.gui;

import javax.swing.*;

import com.game.engine.GameEngine;
import com.game.event.GameEventType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Panel responsible for displaying and managing the dungeon map visualization.
 * Uses double buffering for smooth rendering and maintains room positions and states.
 */
public class MapPanel extends JPanel {
    // Maps room IDs to their screen positions
    private final Map<String, Rectangle> roomPositions;
    private String currentRoomId;
    // Keeps track of which rooms contain treasure
    private final Set<String> treasureRooms;

    // Double buffering to prevent flickering during redraws
    private BufferedImage buffer;
    private boolean needsRedraw = true;

    // Constants for map layout and sizing
    private static final int GRID_SIZE = 12;  // Maximum rooms in each direction
    private static final int ROOM_SIZE = 30;  // Size of each room square
    private static final int ROOM_SPACING = 8;  // Gap between rooms
    private static final int PANEL_PADDING = 20;  // Border padding
    
    // Calculate total panel dimensions based on grid layout
    private static final int DEFAULT_WIDTH = (ROOM_SIZE * GRID_SIZE) + (ROOM_SPACING * (GRID_SIZE - 1)) + (PANEL_PADDING * 2);
    private static final int DEFAULT_HEIGHT = DEFAULT_WIDTH;  // Keep panel square
    
    // Color scheme for different room states
    private static final Color VISITED_ROOM_COLOR = Color.DARK_GRAY;
    private static final Color CURRENT_ROOM_COLOR = Color.YELLOW;
    private static final Color PLAYER_COLOR = new Color(0, 255, 0);  // Bright green
    private static final Color BORDER_COLOR = new Color(0, 255, 0);
    private static final Color TREASURE_ROOM_COLOR = new Color(255, 0, 0);  // Bright red for treasure rooms

    /**
     * Creates a new map panel with double buffering for smooth rendering.
     * Initializes collections for tracking room positions and states.
     */
    public MapPanel() {
        roomPositions = new HashMap<>();
        treasureRooms = new HashSet<>();
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        createBuffer();
    }

    /**
     * Creates or recreates the drawing buffer.
     * Used for double buffering to prevent flickering during redraws.
     */
    private void createBuffer() {
        // Ensure minimum dimensions of 1x1 to prevent BufferedImage creation errors
        buffer = new BufferedImage(Math.max(1, getWidth()), Math.max(1, getHeight()), 
                                 BufferedImage.TYPE_INT_ARGB);
        needsRedraw = true;
    }

    /**
     * Reveals a room on the map at the specified coordinates.
     * Calculates the room's screen position based on its grid coordinates.
     *
     * @param roomId The room identifier in format "x,y"
     * @param isTreasure Whether this room contains treasure
     */
    public void revealRoom(String roomId, boolean isTreasure) {
        if (roomId == null) return;
        
        if (!roomPositions.containsKey(roomId)) {
            try {
                // Parse room coordinates from ID (format: "x,y")
                String[] coords = roomId.split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                
                // Calculate pixel position relative to panel center
                int centerX = DEFAULT_WIDTH / 2;
                int centerY = DEFAULT_HEIGHT / 2;
                
                // Convert grid coordinates to screen coordinates
                // Subtract ROOM_SIZE/2 to center rooms on their grid position
                int roomX = centerX + (x * (ROOM_SIZE + ROOM_SPACING)) - (ROOM_SIZE / 2);
                int roomY = centerY + (y * (ROOM_SIZE + ROOM_SPACING)) - (ROOM_SIZE / 2);
                
                // Store room position and treasure status
                roomPositions.put(roomId, new Rectangle(roomX, roomY, ROOM_SIZE, ROOM_SIZE));
                if (isTreasure) {
                    treasureRooms.add(roomId);
                }
                needsRedraw = true;
                repaint();
            } catch (Exception e) {
                GameEngine.getInstance().fireEvent(GameEventType.GAME_MESSAGE, 
                    "\nError: Unable to reveal this area of the map.");
            }
        }
    }

    /**
     * Updates the player's current position on the map.
     * Triggers a redraw only if the position has changed.
     *
     * @param roomId The room ID where the player is now located
     */
    public void updatePlayerPosition(String roomId) {
        if (roomId != null && !roomId.equals(currentRoomId)) {
            currentRoomId = roomId;
            needsRedraw = true;
            repaint();
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
            createBuffer();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight()) {
            createBuffer();
        }
        
        if (needsRedraw) {
            // Draw to buffer
            Graphics2D g2d = buffer.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear buffer
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw revealed rooms
            drawRooms(g2d);
            
            // Draw map key
            drawMapKey(g2d);
            
            g2d.dispose();
            needsRedraw = false;
        }
        
        // Draw buffer to screen
        g.drawImage(buffer, 0, 0, this);
    }

    private void drawRooms(Graphics2D g2d) {
        if (roomPositions.isEmpty()) return;
        
        for (Map.Entry<String, Rectangle> entry : roomPositions.entrySet()) {
            Rectangle room = entry.getValue();
            String roomId = entry.getKey();
            
            // Fill room
            if (roomId.equals(currentRoomId)) {
                g2d.setColor(CURRENT_ROOM_COLOR);
            } else if (treasureRooms.contains(roomId)) {
                g2d.setColor(TREASURE_ROOM_COLOR);
            } else {
                g2d.setColor(VISITED_ROOM_COLOR);
            }
            g2d.fillRect(room.x, room.y, room.width, room.height);
            
            // Draw room border
            g2d.setColor(BORDER_COLOR);
            g2d.drawRect(room.x, room.y, room.width, room.height);

            // Draw treasure indicator
            if (treasureRooms.contains(roomId)) {
                g2d.setColor(Color.RED);
                int margin = 8;
                g2d.drawString("T", room.x + margin, room.y + room.height - margin);
            }
        }

        // Draw player position
        if (currentRoomId != null && roomPositions.containsKey(currentRoomId)) {
            Rectangle currentRoom = roomPositions.get(currentRoomId);
            g2d.setColor(PLAYER_COLOR);
            int margin = 5;
            g2d.fillOval(
                currentRoom.x + margin,
                currentRoom.y + margin,
                currentRoom.width - (2 * margin),
                currentRoom.height - (2 * margin)
            );
        }
    }

    private void drawMapKey(Graphics2D g2d) {
        int keyX = 10;
        int keyY = getHeight() - 100;  // Moved up to accommodate new legend item
        int keySquareSize = 15;
        int textOffset = 20;
        Font keyFont = new Font("Times New Roman", Font.PLAIN, 12);
        g2d.setFont(keyFont);
        g2d.setColor(Color.GREEN);  // Set text color

        // Current Room
        g2d.setColor(CURRENT_ROOM_COLOR);
        g2d.fillRect(keyX, keyY, keySquareSize, keySquareSize);
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(keyX, keyY, keySquareSize, keySquareSize);
        g2d.drawString("Current Room", keyX + keySquareSize + 5, keyY + keySquareSize);

        // Visited Room
        g2d.setColor(VISITED_ROOM_COLOR);
        g2d.fillRect(keyX, keyY + textOffset, keySquareSize, keySquareSize);
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(keyX, keyY + textOffset, keySquareSize, keySquareSize);
        g2d.drawString("Visited Room", keyX + keySquareSize + 5, keyY + textOffset + keySquareSize);

        // Treasure Room
        g2d.setColor(TREASURE_ROOM_COLOR);
        g2d.fillRect(keyX, keyY + textOffset * 2, keySquareSize, keySquareSize);
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(keyX, keyY + textOffset * 2, keySquareSize, keySquareSize);
        g2d.drawString("Treasure Room", keyX + keySquareSize + 5, keyY + textOffset * 2 + keySquareSize);

        // Player Position
        g2d.setColor(PLAYER_COLOR);
        g2d.fillOval(keyX + 2, keyY + textOffset * 3 + 2, keySquareSize - 4, keySquareSize - 4);
        g2d.drawString("Player", keyX + keySquareSize + 5, keyY + textOffset * 3 + keySquareSize);
    }

    public void resetMap() {
        roomPositions.clear();
        treasureRooms.clear();
        currentRoomId = null;
        needsRedraw = true;
        repaint();
    }
} 