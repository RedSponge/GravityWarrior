package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Particles;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

import java.util.HashMap;

public class BossPlayerRenderer implements IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Boss.frames, Particles.dashStars};

    private ParticleEffect dustEffect;
    private ParticleEffect gpDustEffect;
    private ParticleEffect dashEffect;

    private BossPlayer bossPlayer;
    private float timePassed;
    private HashMap<String, Animation<TextureRegion>> animations;

    private ParticleEffectPool dashEffectPool;
//    private DelayedRemovalArray<PooledEffect> dashEffects;

    public BossPlayerRenderer(BossPlayer bossPlayer, Assets assets) {
        this.bossPlayer = bossPlayer;
        this.timePassed = 0;
        this.dustEffect = assets.get(Particles.dust);
        this.gpDustEffect = assets.get(Particles.groundPoundDust);
        this.dashEffect = assets.get(Particles.dashStars);

//        dashEffectPool = new ParticleEffectPool(dashStarsEffect, 3, 10);

        initAnimation(assets);
    }

    private void initAnimation(Assets assets) {
        TextureAtlas atlas = assets.get(Boss.frames);
        animations = new HashMap<>();
        for(AnimationDescriptor animation : Constants.BOSS_ANIMATION_DATA) {
            int startsAt = 1;
            if(animation.name.equals("defeated")) startsAt = -1;
            animations.put(animation.name, GeneralUtils.getAnimation(animation, atlas, startsAt));
            Logger.log(this, "Loaded animation", animation);
        }
    }

    public void startGPDust() {
        gpDustEffect.setPosition(bossPlayer.pos.x + bossPlayer.size.x / 2, bossPlayer.pos.y);
        gpDustEffect.start();
    }

    public void startDash() {
        dashEffect.reset();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets) {
        timePassed += Gdx.graphics.getDeltaTime();

        int x = bossPlayer.pos.x;
        int dir = bossPlayer.getDirection();
        if(dir == 0) dir = 1;
        if(dir == -1) {
            x += bossPlayer.size.x;
        }
        int w = 128;
        int h = 128;

        String animation = "run";

        if(bossPlayer.getEnemyPlayer().getHealth() <= 0) {
            animation = "laugh";
        }
        else if(bossPlayer.getHealth() <= 0) {
            animation = "defeated";
        }
        else if(bossPlayer.isPunching()) {
            animation = "punch";
            timePassed = bossPlayer.getPunchTimeCounter();
            w = 256;
        }

        if(bossPlayer.isOnGround() && bossPlayer.getHealth() > 0) {
            dustEffect.setPosition(x, bossPlayer.pos.y);
            dustEffect.draw(batch, Gdx.graphics.getDeltaTime());
        }

        final float gb;
        final float timeSinceHit = bossPlayer.getTimeSinceHit();
        final float recoveryTime = 0.2f;

        if(timeSinceHit < recoveryTime) {
            gb = timeSinceHit / recoveryTime;
        } else {
            gb = 1;
        }

        batch.setColor(new Color(1, gb, gb, 1));
        TextureRegion toDraw = animations.get(animation).getKeyFrame(timePassed);
        batch.draw(toDraw, x, bossPlayer.pos.y, w * dir, h);
        batch.setColor(Color.WHITE);

        gpDustEffect.draw(batch, Gdx.graphics.getDeltaTime());

        if(bossPlayer.isDashing()) {
            dashEffect.setPosition(bossPlayer.pos.x + bossPlayer.size.x * (bossPlayer.getDirection() == -1 ? 1 : 0), bossPlayer.pos.y);
        }
        dashEffect.draw(batch, Gdx.graphics.getDeltaTime());
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
