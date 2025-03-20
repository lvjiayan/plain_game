package com.game;

import java.awt.Rectangle;
import java.awt.Color;

public class GameObject {
    protected int x, y;
    protected int width, height;
    protected Color color;
    protected int speed;
    protected boolean isActive = true;
    private int velocityX = 0;
    private int velocityY = 0;
    private boolean isSpecialAI = false;  // 新增：标记是否为特殊AI

    public GameObject(int x, int y, int width, int height, Color color, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.speed = speed;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public Color getColor() { return color; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public void setVelocity(int vx, int vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public int getVelocityX() { return velocityX; }
    public int getVelocityY() { return velocityY; }
    
    // 新增：设置和获取特殊AI标记的方法
    public void setSpecialAI(boolean special) {
        this.isSpecialAI = special;
    }

    public boolean isSpecialAI() {
        return this.isSpecialAI;
    }
}
