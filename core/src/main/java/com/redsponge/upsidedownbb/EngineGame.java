package com.redsponge.upsidedownbb;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.redsponge.upsidedownbb.assets.AssetDescBin;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.screen.AbstractScreen;
import com.redsponge.upsidedownbb.transitions.Transition;
import com.redsponge.upsidedownbb.transitions.TransitionManager;
import com.redsponge.upsidedownbb.utils.Discord;
import com.redsponge.upsidedownbb.utils.GameAccessor;

public abstract class EngineGame extends Game {

    protected GameAccessor ga;
    protected SpriteBatch batch;
    protected ShapeRenderer shapeRenderer;
    protected Assets assets;
    protected TransitionManager transitionManager;
    protected Discord discord;

    private boolean assetLoadingComplete;

    /**
     * Initialization - Called when the program boots up
     */
    public abstract void init();

    @Override
    public final void create() {
        ShaderProgram.pedantic = false;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        ga = new GameAccessor(this);
        assets = new Assets();
        transitionManager = new TransitionManager(this, shapeRenderer);

        transitionManager.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        discord = new Discord("553575233018265601", "");
        init();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f);
        // Try catch to keep intellij from freezing when error is detected
        try {

            if(!assetLoadingComplete) {
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                if(this.assets.updateAssetManager()) {
                    assetLoadingComplete = true;
                    transitionManager.beginExit();

                    this.screen.show();
                    this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }

                return;
            }

            final AbstractScreen currentScreen = (AbstractScreen) screen;

            assets.updateAssetManager();
            if (screen != null) {
                currentScreen.tick(delta);
                currentScreen.render();
            }

            if (transitionManager.isActive()) {
                transitionManager.render(delta);
            }
        } catch (Exception e) {

            e.printStackTrace();
            Gdx.app.exit();
        }
    }

    /**
     * Sets a transition to a new screen
     * @param screen The new screen to be displayed
     * @param transition The transition to use
     * @param length The length of the transition
     * @param interFrom The interpolation in the 1st half
     * @param interTo The interpolation in the 2nd half
     */
    public void transitionToScreen(AbstractScreen screen, Transition transition, float length, Interpolation interFrom, Interpolation interTo) {
        transitionManager.startTransition(screen, transition, length, interFrom, interTo);
        transitionManager.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    @Override
    public void setScreen(Screen screen) {
        assert screen instanceof AbstractScreen;
        setScreen((AbstractScreen) screen);
    }

    public void setScreen(AbstractScreen screen) {
        if(this.screen != null) {
            this.screen.hide();
            if(((AbstractScreen) this.screen).shouldDispose()) {
                this.screen.dispose();
            }

            assets.unload((AbstractScreen) this.screen);
        }

        this.screen = screen;

        if(screen != null) {
            this.assets.load(screen);
            assetLoadingComplete = false;
        }
    }

    @Override
    public void resize(int width, int height) {
        if(assetLoadingComplete) {
            super.resize(width, height);
        }
        transitionManager.resize(width, height);
    }

    public SpriteBatch getSpriteBatch() {
        return batch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public Assets getAssets() {
        return assets;
    }

    @Override
    public void dispose() {
        discord.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        assets.dispose();
    }
}