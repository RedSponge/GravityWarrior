package com.redsponge.upsidedownbb.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.rafaskoberg.gdx.typinglabel.TypingAdapter;
import com.rafaskoberg.gdx.typinglabel.TypingLabel;
import com.redsponge.upsidedownbb.GravityWarrior;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Background;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Skins;
import com.redsponge.upsidedownbb.game.intro.IntroEnemy;
import com.redsponge.upsidedownbb.game.intro.IntroState;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsDebugRenderer;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.transitions.TransitionTemplates;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GameAccessor;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;
import com.redsponge.upsidedownbb.utils.Settings;

public class IntroScreen extends AbstractScreen implements InputProcessor {

    private PhysicsWorld world;
    private IntroEnemy enemy;
    private PhysicsDebugRenderer pdr;

    private FitViewport viewport;
    private FitViewport textViewport;

    private Texture sky, desert;

    private Stage stage;
    private TypingLabel textDisplay;
    private int stateIndex;
    private boolean done;

    private long startTime;

    private Music backgroundMusic;

    public IntroScreen(GameAccessor ga) {
        super(ga);
    }

    @Override
    public void show() {
        GravityWarrior.discord.setPresenceState("Starting Soon...");
        GravityWarrior.discord.setPresenceDetails("Prepping for the battle");
        GravityWarrior.discord.updatePresence();

        viewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        textViewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        world = new PhysicsWorld();
        enemy = new IntroEnemy(world, assets);

        enemy.pos.set((int) (viewport.getWorldWidth() / 2 - enemy.size.x / 2), Constants.FLOOR_HEIGHT);

        world.addActor(enemy);
        pdr = new PhysicsDebugRenderer();

        sky = assets.get(Background.sky);
        desert = assets.get(Background.desert);

        PSolid floor = new PSolid(world);
        floor.pos.set(0, 0);
        floor.size.set((int) Constants.GAME_WIDTH, Constants.FLOOR_HEIGHT);
        world.addSolid(floor);

        stage = new Stage(textViewport, batch);

        Skin skin = assets.get(Skins.menu);

        textDisplay = new TypingLabel(IntroState.PART_1.getCaption(), skin);
        textDisplay.setTypingListener(new TypingAdapter() {
            @Override
            public void end() {
                done = true;
            }

            @Override
            public String replaceVariable(String variable) {
                if(variable.equals("playerName")) {
                    return Settings.playerName;
                }


                return super.replaceVariable(variable);
            }
        });
        textDisplay.setPosition(10, textViewport.getWorldHeight() - 100);
        textDisplay.setFontScale(0.8f);

        stage.addActor(textDisplay);

        Gdx.input.setInputProcessor(this);

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/prepare_to_fight.ogg"));
        backgroundMusic.setVolume(Settings.musicVol / 100);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        startTime = TimeUtils.nanoTime();
    }

    @Override
    public void tick(float delta) {
        world.update(delta);
        stage.act(delta);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();
        cam.zoom = GeneralUtils.lerp(cam.zoom, 0.5f, 0.1f);
        cam.position.set(enemy.pos.x, enemy.pos.y, 0);
        if(cam.position.y < viewport.getWorldHeight() / 2 * cam.zoom) cam.position.y = viewport.getWorldHeight() / 2 * cam.zoom;

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(sky, 0, 0);
        enemy.render(batch, shapeRenderer, assets);
        batch.draw(desert, 0, 0);
        batch.end();

        textViewport.apply();
        shapeRenderer.setProjectionMatrix(textViewport.getCamera().combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_BLEND_COLOR, GL20.GL_BLEND_SRC_ALPHA);

        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
        shapeRenderer.rect(0, textDisplay.getY() - 10, textViewport.getWorldWidth(), 40);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.draw();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        pdr.dispose();
        backgroundMusic.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        textViewport.update(width, height, true);
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return GeneralUtils.joinArrays(AssetDescriptor.class, IntroEnemy.REQUIRED_ASSETS, new AssetDescriptor[] {
                Background.sky, Background.desert, Skins.menu
        });
    }

    @Override
    public boolean keyDown(int keycode) {
        if(GeneralUtils.secondsSince(startTime) < 1) return false;
        if(textDisplay.hasEnded()) {
            stateIndex++;
            try {
                textDisplay.restart(IntroState.values()[stateIndex].getCaption());
            } catch (IndexOutOfBoundsException e) {
                ga.transitionTo(new GameScreen(ga), TransitionTemplates.sineSlide(1));
                Gdx.input.setInputProcessor(null);
            }
        } else {
            textDisplay.skipToTheEnd();
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
