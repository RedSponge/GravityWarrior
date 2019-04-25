package com.redsponge.upsidedownbb;

import com.redsponge.upsidedownbb.screen.MenuScreen;
import com.redsponge.upsidedownbb.utils.Settings;

public class BossBattle extends EngineGame {

    @Override
    public void init() {
        Settings.setDefaults();
        setScreen(new MenuScreen(ga));
    }

}