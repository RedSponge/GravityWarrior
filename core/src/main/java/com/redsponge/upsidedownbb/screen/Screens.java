package com.redsponge.upsidedownbb.screen;

public enum Screens {

    SPLASHSCREEN(SplashScreenScreen.class),
    OTHER(GameScreen.class);

    private Class<? extends AbstractScreen> screen;

    Screens(Class<? extends AbstractScreen> screen) {
        this.screen = screen;
    }
}
