package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Enemy;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.holders.Pair;

import java.util.HashMap;

public class EnemyPlayerRenderer implements IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Enemy.unpowered, Enemy.poweredOverlay};
    private EnemyPlayer player;

    private HashMap<String, Pair<Animation<TextureRegion>, Animation<TextureRegion>>> animations;

    private long startTime;

    public EnemyPlayerRenderer(EnemyPlayer player, Assets assets) {
        this.player = player;
        startTime = TimeUtils.nanoTime();
        initAnimation(assets);
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
        if(player.isTouchingWalls()) {
            currentAnimation = "idle";
        } else {
            currentAnimation = "run";
        }
        int x = player.pos.x;
        int y = player.pos.y;
        int w = player.size.x;
        int h = player.size.y;
        if(player.getDirection() == -1) {
            x += w;
            w *= -1;
        }
        if(player.isPowered()) {
            y += h;
            h *= -1;
        }
        Pair<Animation<TextureRegion>, Animation<TextureRegion>> animationPair = animations.get(currentAnimation);
        batch.draw(animationPair.a.getKeyFrame(GeneralUtils.secondsSince(startTime)), x, y, w, h);
        if(player.isPowered()) {
            batch.draw(animationPair.b.getKeyFrame(GeneralUtils.secondsSince(startTime)), x, y, w, h);
        }
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
