package com.redsponge.upsidedownbb.utils;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;

public class Constants {

    public static final float WORLD_GRAVITY = -10;

    public static final float GAME_WIDTH = 720;
    public static final float GAME_HEIGHT = 480;

    public static final float ARENA_WIDTH = 960;
    public static final float ARENA_HEIGHT = 360;
    public static final int CEILLING_HEIGHT = 239;
    public static final int FLOOR_HEIGHT = 20;

    public static final IntVector2 PUNCH_SIZE = new IntVector2(100, 50);

    public static final float PUNCH_LENGTH = 0.5f;
    public static final int BOSS_WIDTH = 128;
    public static final int BOSS_HEIGHT = 128;

    public static final int PLAYER_WIDTH = 16;
    public static final int PLAYER_HEIGHT = 32;

    public static final float SLICE_LENGTH = 0.3f;

    public static final AnimationDescriptor[] ENEMY_ANIMATION_DATA = {
            new AnimationDescriptor(4, "idle", 0.1f, PlayMode.LOOP_PINGPONG),
            new AnimationDescriptor(8, "run", 0.1f, PlayMode.LOOP),
            new AnimationDescriptor(2, "hit", 0.5f, PlayMode.LOOP),
            new AnimationDescriptor(1, "fallen", 1, PlayMode.LOOP),
            new AnimationDescriptor(2, "head_stuck", 0.1f, PlayMode.LOOP),
            new AnimationDescriptor(1, "plunging", 1, PlayMode.LOOP),
            new AnimationDescriptor(7, "slice", SLICE_LENGTH / 7, PlayMode.LOOP),
            new AnimationDescriptor(3, "duck", 0.01f, PlayMode.NORMAL)
    };

    public static final AnimationDescriptor[] BOSS_ANIMATION_DATA = {
            new AnimationDescriptor(4, "run", 0.1f, PlayMode.LOOP_PINGPONG),
            new AnimationDescriptor(7, "punch", PUNCH_LENGTH / 7, PlayMode.NORMAL),
    };

    public static final int PLAYER_DUCK_HEIGHT = 16;

    public static final float GUI_WIDTH = 320;
    public static final float GUI_HEIGHT = 240;

    public static final float PUNCH_COOLDOWN = 2;
    public static final float DASH_COOLDOWN = 1;
    public static final float GROUND_POUND_COOLDOWN = 10;

    public static final int MAX_GROUND_POUND_DISTANCE = 300;
    public static final float PUNCH_BOX_DELAY = 0.3f;
}
