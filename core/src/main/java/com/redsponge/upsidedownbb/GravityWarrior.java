package com.redsponge.upsidedownbb;

import com.redsponge.upsidedownbb.screen.SplashScreenScreen;
import com.redsponge.upsidedownbb.utils.Discord;
import com.redsponge.upsidedownbb.utils.Settings;

public class GravityWarrior extends EngineGame {

    public static Discord discord;

    @Override
    public void init() {
        discord = super.discord;
        discord.updatePresence();
        Settings.setDefaults();
        setScreen(new SplashScreenScreen(ga));
    }

}