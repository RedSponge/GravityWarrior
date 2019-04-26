package com.redsponge.upsidedownbb.game.enemy;

public enum WinStyle {

    REGULAR("Regular \\o/"),
    CURSED("Cursed <o/"),

    ;

    private String display;

    WinStyle(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
