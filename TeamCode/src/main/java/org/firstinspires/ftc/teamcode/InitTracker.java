package org.firstinspires.ftc.teamcode;

public class InitTracker {
    private static InitTracker instance;

    public boolean didInit;

    private InitTracker() {
        didInit = false;
    }

    public static InitTracker getInstance() {
        if (instance != null)
            return instance;

        instance = new InitTracker();
        return instance;
    }
}
