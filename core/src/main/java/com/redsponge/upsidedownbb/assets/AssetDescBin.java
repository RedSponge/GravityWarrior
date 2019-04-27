package com.redsponge.upsidedownbb.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Holds all asset descriptors for the project
 */
public class AssetDescBin {

    public static final class SplashScreen {
        public static final AssetDescriptor<TextureAtlas> atlas = new AssetDescriptor<TextureAtlas>("splashscreen/splashscreen.atlas", TextureAtlas.class);
    }

    public static final class General {
        public static final AssetDescriptor<TextureAtlas> bar = new AssetDescriptor<TextureAtlas>("textures/bar/bar.atlas", TextureAtlas.class);
    }

    public static final class Fonts {
        public static final AssetDescriptor<BitmapFont> pixelmix = new AssetDescriptor<BitmapFont>("fonts/pixelmix.fnt", BitmapFont.class);
    }

    public static final class Skins {
        public static final AssetDescriptor<Skin> menu = new AssetDescriptor<Skin>("skins/menu/menu_skin.json", Skin.class);
    }

    public static final class Boss {
        public static final AssetDescriptor<TextureAtlas> frames = new AssetDescriptor<TextureAtlas>("textures/boss/frames.atlas", TextureAtlas.class);
        public static final AssetDescriptor<TextureAtlas> powers = new AssetDescriptor<TextureAtlas>("textures/boss/powers.atlas", TextureAtlas.class);

        public static final AssetDescriptor<Sound> bite = new AssetDescriptor<Sound>("sounds/boss_bite.wav", Sound.class);
        public static final AssetDescriptor<Sound> gpRise = new AssetDescriptor<Sound>("sounds/boss_gp_rise.wav", Sound.class);
        public static final AssetDescriptor<Sound> gpFall = new AssetDescriptor<Sound>("sounds/boss_gp_fall.wav", Sound.class);
        public static final AssetDescriptor<Sound> hit = new AssetDescriptor<Sound>("sounds/boss_hit.wav", Sound.class);
        public static final AssetDescriptor<Sound> gpHitGround = new AssetDescriptor<Sound>("sounds/boss_hit_ground.wav", Sound.class);
        public static final AssetDescriptor<Sound> dash = new AssetDescriptor<Sound>("sounds/boss_dash.wav", Sound.class);
    }

    public static final class Enemy {
        public static final AssetDescriptor<TextureAtlas> unpowered = new AssetDescriptor<TextureAtlas>("textures/enemy/unpowered.atlas", TextureAtlas.class);
        public static final AssetDescriptor<TextureAtlas> poweredOverlay = new AssetDescriptor<TextureAtlas>("textures/enemy/powered.atlas", TextureAtlas.class);

        public static final AssetDescriptor<Sound> attack = new AssetDescriptor<Sound>("sounds/enemy_attack.wav", Sound.class);
        public static final AssetDescriptor<Sound> hit = new AssetDescriptor<Sound>("sounds/enemy_hit.wav", Sound.class);
        public static final AssetDescriptor<Sound> stuck = new AssetDescriptor<Sound>("sounds/enemy_stuck.wav", Sound.class);
    }

    public static final class Background {
        public static final AssetDescriptor<Texture> arena = new AssetDescriptor<Texture>("textures/background/arena.png", Texture.class);
        public static final AssetDescriptor<Texture> sky = new AssetDescriptor<Texture>("textures/background/sky.png", Texture.class);
        public static final AssetDescriptor<Texture> menuSky = new AssetDescriptor<Texture>("textures/menu/menu_sky.png", Texture.class);
        public static final AssetDescriptor<Texture> desert = new AssetDescriptor<Texture>("textures/background/desert.png", Texture.class);
    }

    public static final class Particles {
        public static final AssetDescriptor<ParticleEffect> dust = new AssetDescriptor<ParticleEffect>("particles/walk_dust.p", ParticleEffect.class);
        public static final AssetDescriptor<ParticleEffect> groundPoundDust = new AssetDescriptor<ParticleEffect>("particles/gp_dust.p", ParticleEffect.class);
        public static final AssetDescriptor<ParticleEffect> dashStars = new AssetDescriptor<ParticleEffect>("particles/dash_stars.p", ParticleEffect.class);
    }

    public static final class Menu {
        public static final AssetDescriptor<Texture> title = new AssetDescriptor<Texture>("textures/menu/title.png", Texture.class);
    }

}
