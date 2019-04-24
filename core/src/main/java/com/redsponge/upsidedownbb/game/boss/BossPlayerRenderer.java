package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

import java.util.HashMap;

public class BossPlayerRenderer implements IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Boss.frames};

    private BossPlayer bossPlayer;
    private long startTime;
    private HashMap<String, Animation<TextureRegion>> animations;

    public BossPlayerRenderer(BossPlayer bossPlayer, Assets assets) {
        this.bossPlayer = bossPlayer;
        this.startTime = TimeUtils.nanoTime();

        initAnimation(assets);
    }

    private void initAnimation(Assets assets) {
        TextureAtlas atlas = assets.get(Boss.frames);
        animations = new HashMap<>();
        for(AnimationDescriptor animation : Constants.BOSS_ANIMATION_DATA) {
            animations.put(animation.name, GeneralUtils.getAnimation(animation, atlas, 1));
            Logger.log(this, "Loaded animation", animation);
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets) {
        int x = bossPlayer.pos.x;
        int dir = bossPlayer.getDirection();
        if(dir == 0) dir = 1;
        if(dir == -1) {
            x += bossPlayer.size.x;
        }
        int w = 128;
        int h = 128;

        String animation = "run";
        if(bossPlayer.isPunching()) {
            animation = "punch";
            startTime = bossPlayer.getPunchStartTime();
            w = 256;
        }

        TextureRegion toDraw = animations.get(animation).getKeyFrame(GeneralUtils.secondsSince(startTime));
        batch.draw(toDraw, x, bossPlayer.pos.y, w * dir, h);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
