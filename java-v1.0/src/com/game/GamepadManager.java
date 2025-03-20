package com.game;

import net.java.games.input.*;

public class GamepadManager {
    private Controller gamepad;
    private float deadzone = 0.1f;
    private boolean initialized = false;

    public GamepadManager() {
        initialize();
    }

    private void initialize() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller controller : controllers) {
            if (controller.getType() == Controller.Type.GAMEPAD) {
                gamepad = controller;
                initialized = true;
                System.out.println("Gamepad found: " + gamepad.getName());
                break;
            }
        }
        if (!initialized) {
            System.out.println("No gamepad found");
        }
    }

    public void poll() {
        if (!initialized || !gamepad.poll()) {
            return;
        }
    }

    public float getXAxis() {
        if (!initialized) return 0;
        Component axis = gamepad.getComponent(Component.Identifier.Axis.X);
        float value = axis.getPollData();
        return Math.abs(value) < deadzone ? 0 : value;
    }

    public float getYAxis() {
        if (!initialized) return 0;
        Component axis = gamepad.getComponent(Component.Identifier.Axis.Y);
        float value = axis.getPollData();
        return Math.abs(value) < deadzone ? 0 : value;
    }

    public boolean isShootPressed() {
        if (!initialized) return false;
        Component button = gamepad.getComponent(Component.Identifier.Button._2);
        return button.getPollData() == 1.0f;
    }

    public boolean isConnected() {
        return initialized && gamepad != null;
    }
}
