package com.redsponge.upsidedownbb.game.intro;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Enemy;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.IntVector2;
import com.redsponge.upsidedownbb.utils.holders.Pair;

import java.util.HashMap;

public class IntroEnemy extends PActor implements IUpdated, IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Enemy.unpowered, Enemy.poweredOverlay};
    private final int renderWidth;
    private final int renderHeight;

    private HashMap<String, Pair<Animation<TextureRegion>, Animation<TextureRegion>>> animations;
    private float timePassed;
    private IntVector2 vel;

    public IntroEnemy(PhysicsWorld worldIn, Assets assets) {
        super(worldIn);
        pos.x = pos.y = 100;
        size.x = Constants.PLAYER_WIDTH;
        size.y = Constants.PLAYER_HEIGHT;

        renderWidth = 64/2;
        renderHeight = 96/2;

        vel = new IntVector2(0, 0);



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
    public void update(float delta) {
        timePassed += delta;

        vel.y += Constants.WORLD_GRAVITY / 2;
        moveY(vel.y * delta, null);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets) {
        TextureRegion toDraw = animations.get("idle").a.getKeyFrame(timePassed);

        int xOff = -(renderWidth - size.x) / 2;
        int yOff = 0;


        batch.draw(toDraw, pos.x + xOff, pos.y + yOff, renderWidth, renderHeight);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
