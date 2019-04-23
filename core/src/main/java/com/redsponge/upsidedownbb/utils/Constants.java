package com.redsponge.upsidedownbb.utils;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;

public class Constants {

    public static final float WORLD_GRAVITY = -10;

    public static final float GAME_WIDTH = 640;
    public static final float GAME_HEIGHT = 480;

    public static final int FLOOR_HEIGHT = 20;
    public static final IntVector2 PUNCH_SIZE = new IntVector2(200, 100);

    public static final float PUNCH_LENGTH = 0.1f;
    public static final int BOSS_WIDTH = 200;
    public static final int BOSS_HEIGHT = 200;

    public static final int PLAYER_WIDTH = 64;
    public static final int PLAYER_HEIGHT = 96;

    public static final AnimationDescriptor[] ENEMY_ANIMATION_DATA = {
            new AnimationDescriptor(4, "idle", 0.1f, PlayMode.LOOP_PINGPONG),
            new AnimationDescriptor(8, "run", 0.1f, PlayMode.LOOP)
    };
    public static final int PLAYER_DUCK_HEIGHT = 32;
}
