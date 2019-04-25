package com.redsponge.upsidedownbb.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import java.util.HashMap;

public class KeySelectorGroup extends Table {


    private HashMap<String, KeySelector> keySelectors;
    public KeySelectorGroup(Skin skin) {
        super(skin);

        keySelectors = new HashMap<>();
    }

    public void addLabel(String label, KeySelector ks) {
        keySelectors.put(label, ks);
    }

    public void deactivateAll() {
        for (KeySelector value : keySelectors.values()) {
            value.setActive(false);
        }
        getStage().setKeyboardFocus(null);
    }

    public void build() {
        clear();
        for (String label : keySelectors.keySet()) {
            add(new Label(label, getSkin())).pad(10);
        }
        row();
        for (KeySelector value : keySelectors.values()) {
            value.setKeySelectorGroup(this);
            add(value);
        }

        pack();
    }
}
