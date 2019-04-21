package com.redsponge.upsidedownbb.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.game.boss.BossPlayerRenderer;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsDebugRenderer;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GameAccessor;
import com.redsponge.upsidedownbb.utils.GeneralUtils;

public class GameScreen extends AbstractScreen implements InputProcessor {

    public GameScreen(GameAccessor ga) {
        super(ga);
    }

    private PhysicsWorld world;
    private BossPlayer boss;
    private PSolid floor;
    private PhysicsDebugRenderer pdr;

    private BossPlayerRenderer bossRenderer;

    private Texture t;
    private FitViewport gameViewport;

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        assets.finishLoading();
        gameViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        world = new PhysicsWorld();
        boss = new BossPlayer(world);

        floor = new PSolid(world);

        floor.pos.set(0, 0);
        floor.size.set((int) gameViewport.getWorldWidth(), Constants.FLOOR_HEIGHT);

        pdr = new PhysicsDebugRenderer();

        world.addActor(boss);
        world.addSolid(floor);

        PSolid rWall = new PSolid(world), lWall = new PSolid(world);
        rWall.pos.set(0, 0);
        rWall.size.set(1, (int) gameViewport.getWorldHeight());

        lWall.pos.set((int) gameViewport.getWorldWidth(), 0);
        lWall.size.set(1, (int) gameViewport.getWorldHeight());

        PSolid ceiling = new PSolid(world);
        ceiling.size.set((int) gameViewport.getWorldWidth(), 1);
        ceiling.pos.set(0, (int) (gameViewport.getWorldHeight() - 1));

        world.addSolid(rWall);
        world.addSolid(lWall);
        world.addSolid(ceiling);

        EnemyPlayer enemyPlayer = new EnemyPlayer(world, boss);
        boss.setEnemyPlayer(enemyPlayer);
        world.addActor(enemyPlayer);

        bossRenderer = new BossPlayerRenderer(boss, assets);

        t = new Texture("textures/boss/idle.png");
    }

    @Override
    public void tick(float delta) {
        GdxAI.getTimepiece().update(delta);
        MessageManager.getInstance().update();
        world.update(delta);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(/*0.8f, 0.8f, 0.8f*/0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        pdr.render(world, gameViewport.getCamera().combined);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);

        batch.begin();
        bossRenderer.render(batch, shapeRenderer, assets);
        batch.end();
    }

    @Override
    public void dispose() {
        pdr.dispose();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return GeneralUtils.joinArrays(AssetDescriptor.class, BossPlayerRenderer.REQUIRED_ASSETS);
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        int x = (int) gameViewport.unproject(new Vector2(screenX, screenY)).x;
        System.out.println(x);
        boss.setWantedX(x);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
