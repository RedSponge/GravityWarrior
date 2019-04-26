package com.redsponge.upsidedownbb;

import com.redsponge.upsidedownbb.screen.GameScreen;
import com.redsponge.upsidedownbb.utils.Settings;

public class GravityFighter extends EngineGame {

    @Override
    public void init() {
        Settings.setDefaults();
        setScreen(new GameScreen(ga));
    }

}