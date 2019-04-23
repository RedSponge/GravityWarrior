package com.redsponge.upsidedownbb.assets;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

public class AnimationDescriptor {

    public final int numFrames;
    public final String name;
    public final float frameDuration;
    public final PlayMode playMode;

    public AnimationDescriptor(int numFrames, String name, float frameDuration, PlayMode playMode) {
        this.numFrames = numFrames;
        this.name = name;
        this.frameDuration = frameDuration;
        this.playMode = playMode;
    }
}
