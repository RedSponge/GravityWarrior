package com.redsponge.upsidedownbb.game.intro;

public enum IntroState {

    PART_1("{SLOW}This is {VAR=playerName},{WAIT} They are a warrior who possesses a very{COLOR=CYAN}{WAVE} powerful item"),
    PART_2("{SLOW}A {WAVE}{COLOR=CYAN}gravity shirt{ENDWAVE}{CLEARCOLOR}, which gives its owner the ability to switch gravity!{FAST}\n (kinda self explanatory :P)"),
    PART_3("{SLOW}They searched the land looking for worthy foes{WAIT}.{WAIT}.{WAIT}."),
    PART_4("{SLOW}And finally{WAIT}.{WAIT}.{WAIT}. they found one.")
    ;

    private final String caption;

    IntroState(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
}
