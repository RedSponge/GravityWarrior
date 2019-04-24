package com.redsponge.upsidedownbb.texturepacker;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/** Launches the desktop (LWJGL) application. */
public class TexturePackerLauncher {
    public static void main(String[] args) {
        TexturePacker.processIfModified("res/enemy/unpowered", "../assets/textures/enemy/", "unpowered");
        TexturePacker.processIfModified("res/enemy/powered", "../assets/textures/enemy/", "powered");
        TexturePacker.processIfModified("res/powers", "../assets/textures/boss/", "powers");
        TexturePacker.processIfModified("res/boss", "../assets/textures/boss/", "frames");
    }
}