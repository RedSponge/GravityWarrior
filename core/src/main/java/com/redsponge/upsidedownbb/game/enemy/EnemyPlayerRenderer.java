package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Enemy;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Particles;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;
import com.redsponge.upsidedownbb.utils.holders.Pair;

import java.util.HashMap;

public class EnemyPlayerRenderer implements IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Enemy.unpowered, Enemy.poweredOverlay};
    private ParticleEffect dustEffect;
    private EnemyPlayer player;
    private int renderWidth, renderHeight;

    private HashMap<String, Pair<Animation<TextureRegion>, Animation<TextureRegion>>> animations;

    private long startTime;
    private long dabStartTime;
    private boolean dabFlip;

    public EnemyPlayerRenderer(EnemyPlayer player, Assets assets) {
        this.player = player;
        startTime = TimeUtils.nanoTime();

        renderWidth = 64/2;
        renderHeight = 96/2;

        initAnimation(assets);
        dustEffect = assets.get(Particles.dust);
        dabStartTime = 0;
        dabFlip = false;
    }

    private void initAnimation(Assets assets) {
        animations = new HashMap<String, Pair<Animation<TextureRegion>, Animation<TextureRegion>>>();
        TextureAtlas unpoweredAtlas = assets.get(Enemy.unpowered);
        TextureAtlas poweredOverlayAtlas = assets.get(Enemy.poweredOverlay);

        for(AnimationDescriptor animation : Constants.ENEMY_ANIMATION_DATA) {
            Animation<TextureRegion> unpowered = GeneralUtils.getAnimation(animation, unpoweredAtlas, 1);
            Animation<TextureRegion> powered = GeneralUtils.getAnimation(animation, poweredOverlayAtlas, 1);
            animations.put(animation.name, new Pair<>(unpowered, powered));
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets) {
        String currentAnimation;
        boolean flip = false;
        GravityAttackState gravityAttackState = player.getGravityAttackStateMachine().getCurrentState();

        if(player.getHealth() <= 0) {
            currentAnimation = "fallen";
        } else if(player.getBoss().getHealth() <= 0) {
            if(dabStartTime == 0 || GeneralUtils.secondsSince(dabStartTime) > animations.get("dab").a.getAnimationDuration() + 1) {
                dabStartTime = TimeUtils.nanoTime();
                startTime = dabStartTime;
                dabFlip = !dabFlip;
            }
            flip = dabFlip;
            currentAnimation = "dab";
        } else if(player.isHeadStuck()) {
            currentAnimation = "head_stuck";
        } else if(!player.hasRecoveredFromHit()) {
            currentAnimation = "hit";
            flip = true;
        } else if(player.isAttacking()) {
            currentAnimation = "slice";
            startTime = player.getAttackStartTime();
        } else if(player.isDucking()) {
            currentAnimation = "duck";
            flip = true;
            if(GeneralUtils.secondsSince(player.getDuckStartTime()) < .01f) {
                startTime = TimeUtils.nanoTime();
            }
        } else if(gravityAttackState == GravityAttackState.PLUNGING && player.pos.y != Constants.FLOOR_HEIGHT) {
            currentAnimation = "plunging";
        } else if(player.isTouchingWalls() || player.getVel().x == 0) {
            currentAnimation = "idle";
        } else {
            currentAnimation = "run";
        }

        int xOff = -(renderWidth - player.size.x) / 2;
        int yOff = 0;

        int x = player.pos.x + xOff;
        int y = player.pos.y + yOff;

        int w = renderWidth;
        int h = renderHeight;

        if(player.getDirection() == -1 && !flip || player.getDirection() == 1 && flip) {
            x += w;
            w *= -1;
        }
        if(player.isPowered()) {
            y += player.size.y;
            h *= -1;
        }

        float alpha = 1;
        if(!player.hasRecoveredFromHit()) {
            alpha = .5f;
        }

        if(player.isOnGround() && player.isRunning()) {
            dustEffect.setPosition(x, y);
            dustEffect.draw(batch, Gdx.graphics.getDeltaTime());
        }

        batch.setColor(new Color(1, 1, 1, alpha));
        Pair<Animation<TextureRegion>, Animation<TextureRegion>> animationPair = animations.get(currentAnimation);
        batch.draw(animationPair.a.getKeyFrame(GeneralUtils.secondsSince(startTime)), x, y, w, h);
        if(player.isPowered()) {
            batch.draw(animationPair.b.getKeyFrame(GeneralUtils.secondsSince(startTime)), x, y, w, h);
        }
        batch.setColor(Color.WHITE);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
