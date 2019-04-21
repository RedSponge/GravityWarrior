package com.redsponge.upsidedownbb;

import com.redsponge.upsidedownbb.screen.GameScreen;

public class BossBattle extends EngineGame {

    @Override
    public void init() {
        setScreen(new GameScreen(ga));
    }

}