package com.redsponge.upsidedownbb.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Background;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Particles;
import com.redsponge.upsidedownbb.game.Platform;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.game.boss.BossPlayerRenderer;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayerRenderer;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsDebugRenderer;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GameAccessor;
import com.redsponge.upsidedownbb.utils.GeneralUtils;

public class GameScreen extends AbstractScreen implements InputProcessor {

    private Texture arenaBackground;
    private Texture sky;

    private PhysicsWorld world;
    private BossPlayer boss;
    private PSolid floor;
    private PhysicsDebugRenderer pdr;

    private BossPlayerRenderer bossRenderer;
    private EnemyPlayerRenderer enemyRenderer;

    private FitViewport gameViewport;
    private FitViewport guiViewport;

    private TextureRegion bossDash, bossGP, bossPunch;

    private Music backgroundMusic;

    private ParticleEffect dust;

    public GameScreen(GameAccessor ga) {
        super(ga);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        assets.finishLoading();
        gameViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        world = new PhysicsWorld();
        boss = new BossPlayer(world, assets);

        floor = new Platform(world);

        floor.pos.set(0, 0);
        floor.size.set((int) Constants.ARENA_WIDTH, Constants.FLOOR_HEIGHT);

        pdr = new PhysicsDebugRenderer();

        world.addActor(boss);
        world.addSolid(floor);

        Platform rWall = new Platform(world), lWall = new Platform(world);
        rWall.pos.set(0, 0);
        rWall.size.set(1, (int) Constants.ARENA_HEIGHT);

        lWall.pos.set((int) Constants.ARENA_WIDTH - 1, 0);
        lWall.size.set(1, (int) Constants.ARENA_HEIGHT);

        Platform ceiling = new Platform(world);
        ceiling.size.set((int) Constants.ARENA_WIDTH, 1);
        ceiling.pos.set(0, Constants.CEILLING_HEIGHT);

        world.addSolid(rWall);
        world.addSolid(lWall);
        world.addSolid(ceiling);

        EnemyPlayer enemyPlayer = new EnemyPlayer(world, boss, assets);
        boss.setEnemyPlayer(enemyPlayer);
        world.addActor(enemyPlayer);

        bossRenderer = new BossPlayerRenderer(boss, assets);
        enemyRenderer = new EnemyPlayerRenderer(enemyPlayer, assets);

        guiViewport = new FitViewport(Constants.GUI_WIDTH, Constants.GUI_HEIGHT);

        TextureAtlas powers = assets.get(Boss.powers);
        bossDash = powers.findRegion("dash");
        bossGP = powers.findRegion("ground_pound");
        bossPunch = powers.findRegion("punch");

        arenaBackground = assets.get(Background.arena);
        sky = assets.get(Background.sky);

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/fight_with_a_cube.wav"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        boss.setRenderer(bossRenderer);
    }

    @Override
    public void tick(float delta) {
        GdxAI.getTimepiece().update(delta);
        MessageManager.getInstance().update();
        world.update(delta);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        Vector3 camPos = gameViewport.getCamera().position;
        float zoom = 0.8f;
        ((OrthographicCamera) gameViewport.getCamera()).zoom = zoom;
        camPos.lerp(new Vector3(boss.pos.x, boss.pos.y, 0), 0.1f);

        if(camPos.x < gameViewport.getWorldWidth() * zoom / 2) camPos.x = gameViewport.getWorldWidth() * zoom / 2;
        if(camPos.y < gameViewport.getWorldHeight() * zoom / 2) camPos.y = gameViewport.getWorldHeight() * zoom / 2;
        if(camPos.x > Constants.ARENA_WIDTH - gameViewport.getWorldWidth() * zoom / 2) camPos.x = Constants.ARENA_WIDTH - gameViewport.getWorldWidth() * zoom / 2;



        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);

        batch.begin();
        batch.draw(sky, 0, 0);
        bossRenderer.render(batch, shapeRenderer, assets);
        enemyRenderer.render(batch, shapeRenderer, assets);

        batch.draw(arenaBackground, 0, 0);
        batch.end();

        guiViewport.apply();
        batch.setProjectionMatrix(guiViewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(guiViewport.getCamera().combined);
        drawGUI();
    }

    private void drawGUI() {
        batch.begin();
        TextureRegion[] powers = {bossPunch, bossDash, bossGP};
        float[] percents = {boss.getPercentCooldownForPunch(), boss.getPercentCooldownForDash(), boss.getPercentCooldownForGroundPound()};
        int powerSpacing = 6;
        int powerStartMargin = 20;
        for (int i = 0; i < powers.length; i++) {
            TextureRegion power = powers[i];
            batch.draw(power, powerStartMargin + i * (power.getRegionWidth() + powerSpacing), 200);
        }
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ALPHA, GL20.GL_BLEND_SRC_ALPHA);

        for (int i = 0; i < powers.length; i++) {
            TextureRegion power = powers[i];
            float percent = percents[i];
            if(percent < 1) {
                shapeRenderer.rect(powerStartMargin + i * (power.getRegionWidth() + powerSpacing), 200, power.getRegionWidth(), power.getRegionHeight() * (1 - percent));
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        pdr.dispose();
        backgroundMusic.dispose();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        guiViewport.update(width, height, true);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return GeneralUtils.joinArrays(AssetDescriptor.class, EnemyPlayer.REQUIRED_ASSETS, BossPlayerRenderer.REQUIRED_ASSETS,
                BossPlayer.REQUIRED_ASSETS, EnemyPlayerRenderer.REQUIRED_ASSETS, new AssetDescriptor[] {
                        Particles.dust, Particles.groundPoundDust, Boss.powers, Background.arena, Background.sky});
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
        boss.setDirection((int) Math.signum(x - boss.pos.x));
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
