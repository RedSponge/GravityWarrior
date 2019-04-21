package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.assets.IRenderer;

public class BossPlayerRenderer implements IRenderer {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Boss.idle};

    private BossPlayer bossPlayer;
    private Texture idle;

    public BossPlayerRenderer(BossPlayer bossPlayer, Assets assets) {
        this.bossPlayer = bossPlayer;
        idle = assets.get(Boss.idle);
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets) {
        int x = bossPlayer.pos.x;
        int dir = bossPlayer.getDirection();
        if(dir == -1) {
            x += bossPlayer.size.x;
        }
        batch.draw(idle, x, bossPlayer.pos.y, bossPlayer.size.x * dir, bossPlayer.size.y);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return REQUIRED_ASSETS;
    }
}
