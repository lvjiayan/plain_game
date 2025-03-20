package com.game;

import javax.swing.JFrame;

public class ShootingGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Shooting Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
