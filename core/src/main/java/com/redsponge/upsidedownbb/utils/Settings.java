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
    public static String playerName;

    public static boolean knowsPowers;
    public static boolean knowsHowToMove;
    public static boolean sawIntro;

    public static void setDefaults() {
        keyDash = Keys.SHIFT_LEFT;
        keyGroundPound = Keys.SPACE;
        keyPunch = Keys.X;
        keyPause = Keys.ESCAPE;

        musicVol = 0;
        soundVol = 50;

        playerName = GeneralUtils.getCurrentUserName();
        if(playerName == null) {
            playerName = GeneralUtils.randomItem(Constants.RANDOM_PLAYER_NAMES);
        }

        playerName = GeneralUtils.capitalizeWord(playerName).split(" ")[0];

        knowsPowers = false;
        knowsHowToMove = false;
        sawIntro = false;

        winStyle = WinStyle.REGULAR;
    }

}
