package com.redsponge.upsidedownbb.utils;

import com.badlogic.gdx.Input.Keys;
import com.redsponge.upsidedownbb.game.enemy.WinStyle;

public class Settings {

    public static int keyDash;
    public static int keyGroundPound;
    public static int keyPunch;

    public static float musicVol;
    public static float soundVol;
    public static int keyPause;

    public static WinStyle winStyle;

    public static void setDefaults() {
        keyDash = Keys.SHIFT_LEFT;
        keyGroundPound = Keys.SPACE;
        keyPunch = Keys.Z;
        keyPause = Keys.ESCAPE;

        musicVol = 0;
        soundVol = 0;

        winStyle = WinStyle.REGULAR;
    }

}
