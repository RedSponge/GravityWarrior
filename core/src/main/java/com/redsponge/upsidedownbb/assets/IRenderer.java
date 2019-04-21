package com.redsponge.upsidedownbb.assets;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IRenderer extends IAssetRequirer {

    void render(SpriteBatch batch, ShapeRenderer shapeRenderer, Assets assets);

}
