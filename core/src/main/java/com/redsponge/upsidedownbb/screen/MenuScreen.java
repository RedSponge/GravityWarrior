package com.redsponge.upsidedownbb.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Background;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Menu;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Skins;
import com.redsponge.upsidedownbb.game.enemy.WinStyle;
import com.redsponge.upsidedownbb.transitions.TransitionTemplate;
import com.redsponge.upsidedownbb.transitions.TransitionTemplates;
import com.redsponge.upsidedownbb.ui.FieldSlider;
import com.redsponge.upsidedownbb.ui.KeySelector;
import com.redsponge.upsidedownbb.ui.KeySelectorGroup;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GameAccessor;
import com.redsponge.upsidedownbb.utils.Settings;

public class MenuScreen extends AbstractScreen {

    private Texture sky;

    private FitViewport viewport;

    private float xSkyOffset;
    private Stage stage;

    private Image title;
    private Skin skin;
    private Runnable pendingMenuLayout;

    private boolean titleLoaded;

    public MenuScreen(GameAccessor ga) {
        super(ga);
    }

    @Override
    public void show() {
        sky = assets.get(Background.menuSky);
        Texture titleT = assets.get(Menu.title);

        viewport = new FitViewport(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        stage = new Stage(viewport, batch);

        title = new Image(titleT);
        title.setPosition(-titleT.getWidth(), 240);

        stage.addActor(title);
        title.addAction(Actions.sequence(Actions.moveToAligned(viewport.getWorldWidth() / 2, 240, Align.bottom, 3, Interpolation.exp5), Actions.run(() -> titleLoaded = true)));
        skin = assets.get(Skins.menu);

        pendingMenuLayout = this::setupMenuButtons;

        Gdx.input.setInputProcessor(stage);
    }

    public void setupMenuButtons() {
        Button start = getButton(skin, "Start");
        Button options = getButton(skin, "Options");
        Button customizing = getButton(skin, "Customizables");
        Button exit = getButton(skin, "Exit");

        setupEnterActions(180, titleLoaded ? 0 : 1.5f, start, options, customizing, exit);

        start.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(Settings.sawIntro) {
                    ga.transitionTo(new GameScreen(ga), TransitionTemplates.sineSlide(1));
                } else {
                    ga.transitionTo(new IntroScreen(ga), TransitionTemplates.sineSlide(1));
                    Settings.sawIntro = true;
                }
            }
        });

        options.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearStage();
                pendingMenuLayout = MenuScreen.this::setupOptionButtons;
            }
        });

        customizing.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearStage();
                pendingMenuLayout = MenuScreen.this::setupCustomizingButtons;
            }
        });

        exit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    private void setupOptionButtons() {
        Button back = getBackButton(skin, this::setupMenuButtons);

        Table music = new Table(skin);
        Label musicL = new Label("Music: ", skin);

        FieldSlider musicSlider = new FieldSlider(0, 100, 1, false, skin, Settings.class, null, "musicVol");
        music.add(musicL, musicSlider);


        Table sound = new Table(skin);
        Label soundL = new Label("Sound: ", skin);

        FieldSlider soundSlider = new FieldSlider(0, 100, 1, false, skin, Settings.class, null, "soundVol");
        sound.add(soundL, soundSlider);


        KeySelectorGroup keys = new KeySelectorGroup(skin);
        KeySelector attack = new KeySelector(skin, Settings.class, null, "keyPunch");
        KeySelector dash = new KeySelector(skin, Settings.class, null, "keyDash");
        KeySelector ground_pound = new KeySelector(skin, Settings.class, null, "keyGroundPound");

        String[] lbls = new String[] {"Attack", "Dash", "Ground Pound"};
        KeySelector[] selectors = {attack, dash, ground_pound};

        for(int i = 0; i < lbls.length; i++) {
            keys.addLabel(lbls[i], selectors[i]);
        }
        keys.build();

        music.pack();
        stage.addActor(music);

        sound.pack();

        setupEnterActions(180, 0, music, sound, keys, back);
    }

    private void setupCustomizingButtons() {
        Table winStyles = new Table();

        Label winStylesL = new Label("Win Style:", skin);

        SelectBox<WinStyle> winStylesSB = new SelectBox<WinStyle>(skin);
        winStylesSB.setItems(WinStyle.values());
        winStylesSB.setWidth(100);
        winStylesSB.setSelected(Settings.winStyle);

        winStyles.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.winStyle = winStylesSB.getSelected();
            }
        });

        winStyles.add(winStylesL).pad(10);
        winStyles.add(winStylesSB).pad(10);
        winStyles.pack();

        Button back = getBackButton(skin, this::setupMenuButtons);

        setupEnterActions(150, 0, winStyles, back);
    }

    @SuppressWarnings("SameParameterValue")
    private void setupEnterActions(int startY, float enterDelay, Actor... actors) {
        int yDown = 0;
        for (int i = 0; i < actors.length; i++) {
            Actor b = actors[i];
            yDown += b.getHeight() + 10;
            int y = startY - yDown;
            if(i % 2 == 1) {
                b.setPosition(-b.getWidth(), y);
            } else {
                b.setPosition(viewport.getWorldWidth(), y);
            }
            b.addAction(Actions.sequence(Actions.delay(i * 0.5f + enterDelay), Actions.moveToAligned(viewport.getWorldWidth() / 2, y, Align.bottom, 1, Interpolation.exp5Out)));
            stage.addActor(b);
        }
    }

    private Button getButton(Skin skin, String text) {
        return new TextButton(text, skin);
    }

    private Button getBackButton(Skin skin, Runnable backTo) {
        Button b = getButton(skin, "Back");
        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clearStage();
                pendingMenuLayout = backTo;
            }
        });
        return b;
    }

    private Array<Actor> getNonTitleActors() {
        Array<Actor> actors = new Array<>(stage.getActors());
        actors.removeValue(title, true);
        return actors;
    }

    private void clearStage() {
        float delay = 0;
        Array<Actor> actors = getNonTitleActors();
        actors.reverse();
        for(Actor a : actors) {
            a.addAction(Actions.sequence(Actions.delay(delay), Actions.moveTo(a.getX(), -a.getWidth(), 0.5f, Interpolation.exp5), Actions.removeActor()));
            delay += 0.2f;
        }
    }

    @Override
    public void tick(float delta) {
        xSkyOffset += delta * 20;
        if(xSkyOffset > sky.getWidth()) {
            xSkyOffset -= sky.getWidth();
        }

        stage.act(delta);
        if(pendingMenuLayout != null && getNonTitleActors().isEmpty()) {
            pendingMenuLayout.run();
            pendingMenuLayout = null;
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.setColor(Color.WHITE);

        batch.draw(sky, -xSkyOffset, 0);
        batch.draw(sky, sky.getWidth() - xSkyOffset, 0);

        batch.end();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public AssetDescriptor[] getRequiredAssets() {
        return new AssetDescriptor[] {Background.menuSky, Menu.title, Skins.menu};
    }
}
