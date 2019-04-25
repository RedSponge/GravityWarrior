package com.redsponge.upsidedownbb.utils;

import com.badlogic.gdx.Input.Keys;

public class Settings {

    public static int keyDash;
    public static int keyGroundPound;
    public static int keyPunch;

    public static int musicVol;
    public static int soundVol;

    public static void setDefaults() {
        keyDash = Keys.SHIFT_LEFT;
        keyGroundPound = Keys.SPACE;
        keyPunch = Keys.Z;

        musicVol = 50;
        soundVol = 50;
    }

}
