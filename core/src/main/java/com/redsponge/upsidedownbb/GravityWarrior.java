package com.redsponge.upsidedownbb;

import com.redsponge.upsidedownbb.screen.IntroScreen;
import com.redsponge.upsidedownbb.utils.Settings;

public class GravityWarrior extends EngineGame {

    @Override
    public void init() {
        Settings.setDefaults();
        setScreen(new IntroScreen(ga));
    }

}