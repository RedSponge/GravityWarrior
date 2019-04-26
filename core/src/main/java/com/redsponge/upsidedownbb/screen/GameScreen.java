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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Background;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Fonts;
import com.redsponge.upsidedownbb.assets.AssetDescBin.General;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Particles;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Skins;
import com.redsponge.upsidedownbb.game.Platform;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.game.boss.BossPlayerRenderer;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayerRenderer;
import com.redsponge.upsidedownbb.physics.PEntity;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsDebugRenderer;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.transitions.TransitionTemplates;
import com.redsponge.upsidedownbb.ui.FieldSlider;
import com.redsponge.upsidedownbb.ui.KeySelector;
import com.redsponge.upsidedownbb.ui.KeySelectorGroup;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GameAccessor;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;
import com.redsponge.upsidedownbb.utils.Settings;

public class GameScreen extends AbstractScreen implements InputProcessor {

    private Texture arenaBackground;
    private Texture sky;

    private PhysicsWorld world;
    private BossPlayer boss;

    private PhysicsDebugRenderer pdr;

    private BossPlayerRenderer bossRenderer;
    private EnemyPlayerRenderer enemyRenderer;

    private FitViewport gameViewport;
    private FitViewport guiViewport;

    private TextureRegion bossDash, bossGP, bossPunch;
    private NinePatch barOutline, barInside;

    private Music backgroundMusic;

    private EnemyPlayer enemyPlayer;

    private BitmapFont font;
    private long gameFinishTime;

    private Stage pauseMenu;

    private boolean paused;
    private Skin pauseSkin;

    private FitViewport pauseViewport;

    private int screenShakes;

    private ScalingViewport overlayViewport;

    public GameScreen(GameAccessor ga) {
        super(ga);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);

        assets.finishLoading();
        gameViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        world = new PhysicsWorld();
        boss = new BossPlayer(world, this, assets);

        PSolid floor = new Platform(world);

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

        enemyPlayer = new EnemyPlayer(world, boss, assets, this);
        boss.setEnemyPlayer(enemyPlayer);
        world.addActor(enemyPlayer);

        bossRenderer = new BossPlayerRenderer(boss, assets);
        enemyRenderer = new EnemyPlayerRenderer(enemyPlayer, assets);

        enemyPlayer.setRenderer(enemyRenderer);

        guiViewport = new FitViewport(Constants.GUI_WIDTH, Constants.GUI_HEIGHT);

        TextureAtlas powers = assets.get(Boss.powers);
        bossDash = powers.findRegion("dash");
        bossGP = powers.findRegion("ground_pound");
        bossPunch = powers.findRegion("punch");

        arenaBackground = assets.get(Background.arena);
        sky = assets.get(Background.sky);

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/fight_with_a_cube.wav"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();

        TextureAtlas bar = assets.get(General.bar);
        barInside = new NinePatch(bar.findRegion("inside"), 2, 2, 1, 1);
        barOutline = new NinePatch(bar.findRegion("outline"), 2, 2, 1, 1);

        boss.setRenderer(bossRenderer);

        font = assets.get(Fonts.pixelmix);

        pauseViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        pauseMenu = new Stage(pauseViewport, batch);
        pauseSkin = assets.get(Skins.menu);

        pauseMenu.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Settings.keyPause) {
                    togglePause();
                    return true;
                }
                return false;
            }
        });


        setupPauseMenu();

        overlayViewport = new ScalingViewport(Scaling.fill, 1, 1);
        screenShakes = 10;
    }

    private void setupPauseMenu() {

        Label title = new Label("Pause Menu", pauseSkin);

        Table music = new Table(pauseSkin);
        Label musicL = new Label("Music: ", pauseSkin);

        FieldSlider musicSlider = new FieldSlider(0, 100, 1, false, pauseSkin, Settings.class, null, "musicVol");
        music.add(musicL, musicSlider);


        Table sound = new Table(pauseSkin);
        Label soundL = new Label("Sound: ", pauseSkin);

        FieldSlider soundSlider = new FieldSlider(0, 100, 1, false, pauseSkin, Settings.class, null, "soundVol");
        sound.add(soundL, soundSlider);


        KeySelectorGroup keys = new KeySelectorGroup(pauseSkin);
        KeySelector attack = new KeySelector(pauseSkin, Settings.class, null, "keyPunch");
        KeySelector dash = new KeySelector(pauseSkin, Settings.class, null, "keyDash");
        KeySelector ground_pound = new KeySelector(pauseSkin, Settings.class, null, "keyGroundPound");

        String[] lbls = new String[] {"Attack", "Dash", "Ground Pound"};
        KeySelector[] selectors = {attack, dash, ground_pound};

        for(int i = 0; i < lbls.length; i++) {
            keys.addLabel(lbls[i], selectors[i]);
        }

        keys.build();
        music.pack();
        sound.pack();

        title.setPosition(pauseViewport.getWorldWidth() / 2, 240, Align.bottom);
        music.setPosition(pauseViewport.getWorldWidth() / 2, 180, Align.bottom);
        sound.setPosition(pauseViewport.getWorldWidth() / 2, 140, Align.bottom);
        keys.setPosition(pauseViewport.getWorldWidth() / 2, 70, Align.bottom);

        Button backToGame = new TextButton("Back To Game", pauseSkin);
        backToGame.setPosition(pauseViewport.getWorldWidth() / 2, 40, Align.bottom);

        backToGame.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });

        Button backToMenu = new TextButton("Back To Menu", pauseSkin);
        backToMenu.setPosition(pauseViewport.getWorldWidth() / 2, 20, Align.center);

        backToMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ga.transitionTo(new MenuScreen(ga), TransitionTemplates.sineSlide(1));
            }
        });

        pauseMenu.addActor(title);
        pauseMenu.addActor(music);
        pauseMenu.addActor(sound);
        pauseMenu.addActor(keys);
        pauseMenu.addActor(backToGame);
        pauseMenu.addActor(backToMenu);
    }

    private boolean gameFinished;

    @Override
    public void tick(float delta) {
        backgroundMusic.setVolume(Settings.musicVol / 100f);
        if(paused) {
            pauseMenu.act();
            return;
        }

        if((boss.getHealth() <= 0 || enemyPlayer.getHealth() <= 0) && !gameFinished) {
            gameFinished = true;
            backgroundMusic.stop();
            backgroundMusic.dispose();
            gameFinishTime = TimeUtils.nanoTime();
            return;
        }

        GdxAI.getTimepiece().update(delta);
        MessageManager.getInstance().update();
        world.update(delta);

        pauseMenu.act(delta);
    }

    private void togglePause() {
        paused = !paused;
        if(paused) {
            Gdx.input.setInputProcessor(pauseMenu);
        } else {
            Gdx.input.setInputProcessor(this);
        }
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        Vector3 camPos = gameViewport.getCamera().position;
        float zoom = 0.8f;
        if(!gameFinished) {
            ((OrthographicCamera) gameViewport.getCamera()).zoom = zoom;
            camPos.lerp(new Vector3(boss.pos.x, boss.pos.y, 0), 0.1f);
        } else {
            zoom = 0.5f;
            PEntity target = boss.getHealth() <= 0 ? boss : enemyPlayer;
            camPos.lerp(new Vector3(target.pos.x, target.pos.y, 0), 0.1f);
            ((OrthographicCamera) gameViewport.getCamera()).zoom = GeneralUtils.lerp(((OrthographicCamera) gameViewport.getCamera()).zoom, zoom, 0.1f);
        }

        if (camPos.x < gameViewport.getWorldWidth() * zoom / 2) camPos.x = gameViewport.getWorldWidth() * zoom / 2;
        if (camPos.y < gameViewport.getWorldHeight() * zoom / 2)
            camPos.y = gameViewport.getWorldHeight() * zoom / 2;
        if (camPos.x > Constants.ARENA_WIDTH - gameViewport.getWorldWidth() * zoom / 2)
            camPos.x = Constants.ARENA_WIDTH - gameViewport.getWorldWidth() * zoom / 2;

        Vector3 unshaken = new Vector3(camPos);
        if(screenShakes > 0) {
            camPos.add(MathUtils.random(-5, 6), MathUtils.random(-5, 6), 0);
            screenShakes--;
        }


        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);

        batch.begin();
        batch.draw(sky, 0, 0);
        bossRenderer.render(batch, shapeRenderer, assets);
        enemyRenderer.render(batch, shapeRenderer, assets);

        batch.draw(arenaBackground, 0, 0);
        batch.end();

        pdr.render(world, gameViewport.getCamera().combined);

        camPos.set(unshaken);

        guiViewport.apply();
        batch.setProjectionMatrix(guiViewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(guiViewport.getCamera().combined);
        drawGUI();
    }

    private void drawGUI() {

        final int alphaMult;
        if(gameFinished) {
            alphaMult = (int) Math.max((1 - GeneralUtils.secondsSince(gameFinishTime) / 2) * 255, 0);
        } else {
            alphaMult = 255;
        }

        batch.begin();
        batch.setColor(new Color(1, 1, 1, alphaMult / 255f));

        TextureRegion[] powers = {bossPunch, bossDash, bossGP};
        float[] percents = {boss.getPercentCooldownForPunch(), boss.getPercentCooldownForDash(), boss.getPercentCooldownForGroundPound()};
        int powerSpacing = 6;
        int powerStartMargin = 20;
        for (int i = 0; i < powers.length; i++) {
            TextureRegion power = powers[i];
            batch.draw(power, powerStartMargin + i * (power.getRegionWidth() + powerSpacing), 200);
        }

        int barDistFromWalls = 20;
        int barY = 150;
        int barHeight = 20;
        int barWidth = 100;

        barOutline.draw(batch, barDistFromWalls, barY, barWidth, barHeight);
        barOutline.draw(batch, guiViewport.getWorldWidth() - barDistFromWalls - barWidth, barY, barWidth, barHeight);


        batch.setColor(new Color(Constants.BOSS_BAR_COLOR + alphaMult));
        barInside.draw(batch, barDistFromWalls, barY, barWidth * boss.getHealth() / Constants.BOSS_MAX_HEALTH, barHeight);
        batch.setColor(new Color(Constants.PLAYER_BAR_COLOR + alphaMult));
        barInside.draw(batch, guiViewport.getWorldWidth() - barDistFromWalls - barWidth, barY, barWidth * enemyPlayer.getHealth() / Constants.PLAYER_MAX_HEALTH, barHeight);

        final String drawnText;
        if(boss.getHealth() <= 0) {
            drawnText = "You Lost!";
        } else if(enemyPlayer.getHealth() <= 0) {
            drawnText = "You Won!";
        } else {
            drawnText = null;
        }


        String pressAnyKeyText = "Press any key to go back to menu";
        GlyphLayout layout;
        GlyphLayout pressAnyKeyLayout;

        if(drawnText != null) {
             layout = new GlyphLayout(font, drawnText);

             font.getData().setScale(0.5f);
             pressAnyKeyLayout = new GlyphLayout(font, pressAnyKeyText);
             font.getData().setScale(1);
        } else {
            layout = null;
            pressAnyKeyLayout = null;
        }

        batch.setColor(Color.WHITE);
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(new Color(1, 0, 0, 0.5f));
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ALPHA, GL20.GL_BLEND_SRC_ALPHA);
        if(!gameFinished) {


            for (int i = 0; i < powers.length; i++) {
                TextureRegion power = powers[i];
                float percent = percents[i];
                if (percent < 1) {
                    shapeRenderer.rect(powerStartMargin + i * (power.getRegionWidth() + powerSpacing), 200, power.getRegionWidth(), power.getRegionHeight() * (1 - percent));
                }
            }
        } else if(layout != null) {
            shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
            shapeRenderer.rect(0, 150 - layout.height - 5, guiViewport.getWorldWidth(), layout.height + 10);
            shapeRenderer.rect(0, 110 - pressAnyKeyLayout.height - 5, guiViewport.getWorldWidth(), pressAnyKeyLayout.height + 10);
        } else {
            Logger.log(this, "You shouldn't reach this place! PLACE: The last else in renderGUI using shapeRenderer");
        }
        shapeRenderer.end();

        if(paused) {
            overlayViewport.apply();
            shapeRenderer.setProjectionMatrix(overlayViewport.getCamera().combined);
            shapeRenderer.begin(ShapeType.Filled);
            shapeRenderer.setColor(new Color(0, 0, 0, 0.8f));
            shapeRenderer.rect(0, 0, 1, 1);
            shapeRenderer.end();
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);



        if(layout != null) {
            batch.begin();
            font.setColor(new Color(1, 1, 1, 1 - alphaMult / 255f));
            font.draw(batch, drawnText, guiViewport.getWorldWidth() / 2 - layout.width / 2, 150);

            font.getData().setScale(0.5f);

            font.draw(batch, pressAnyKeyText, guiViewport.getWorldWidth() / 2 - pressAnyKeyLayout.width / 2, 110);
            batch.end();
            font.getData().setScale(1);
        }

        if(paused) {
            pauseViewport.apply();
            pauseMenu.draw();
        }
    }

    public void setScreenShakes(int screenShakes) {
        this.screenShakes = screenShakes;
    }

    public int getScreenShakes() {
        return screenShakes;
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
        pauseViewport.update(width, height, true);
        overlayViewport.update(width, height, true);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return GeneralUtils.joinArrays(AssetDescriptor.class, EnemyPlayer.REQUIRED_ASSETS, BossPlayerRenderer.REQUIRED_ASSETS,
                BossPlayer.REQUIRED_ASSETS, EnemyPlayerRenderer.REQUIRED_ASSETS, new AssetDescriptor[] {
                        Particles.dust, Particles.groundPoundDust, Fonts.pixelmix, General.bar, Boss.powers, Background.arena, Background.sky, Skins.menu});
    }


    @Override
    public boolean keyDown(int keycode) {
        if(gameFinished) {
            ga.transitionTo(new MenuScreen(ga), TransitionTemplates.sineSlide(1));
        } else {
            if(keycode == Settings.keyPause) {
                togglePause();
            }
        }
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
