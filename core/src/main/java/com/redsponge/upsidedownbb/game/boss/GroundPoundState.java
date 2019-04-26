package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.Logger;
import com.redsponge.upsidedownbb.utils.Settings;

public enum GroundPoundState implements State<BossPlayer> {

    RAISE() {
        private int startY;
        private int wantedX;
        private int startX;
        private Vector2 vel;
        private int wantedY;
        private float timeCounter;
        private int direction;

        @Override
        public void enter(BossPlayer entity) {
            super.enter(entity);
            this.wantedX = entity.getEnemyPlayer().pos.x - entity.size.x / 2;
            this.startX = entity.pos.x;

            if(Math.abs(wantedX - startX) > Constants.MAX_GROUND_POUND_DISTANCE) {
                Logger.log(this, "Too Far!");
                wantedX = (int) (startX + Constants.MAX_GROUND_POUND_DISTANCE * Math.signum(wantedX - startX));
            }

            this.vel = entity.getVel();
            this.wantedY = 150;
            this.startY = entity.pos.y;
            entity.setDirection(entity.getEnemyPlayer().getRelativePositionFromBossMultiplier());
            timeCounter = 0;
            direction = entity.pos.x < wantedX ? 1 : -1;
        }

        @Override
        public void update(BossPlayer entity) {
            timeCounter += Gdx.graphics.getDeltaTime();
            final int overAllX = Math.abs(startX - wantedX);
            float progress = timeCounter / 0.5f;
            if(progress > 1) progress = 1;

            vel.y = 0;
            vel.x = 0;

            int neededY = startY + (int) (Interpolation.circleOut.apply(progress) * wantedY);
            int neededX = startX + (int) (Interpolation.circleOut.apply(progress) * overAllX) * direction;
            int toMoveX = Math.abs(neededX - entity.pos.x);

            // For GP Raise Debug:
            // Logger.log(this, "ToMove:",toMoveX, "StartX:",startX, "Interpolated:", Interpolation.circleOut.apply(progress), "Need to be in:",neededX, "Current Pos:",entity.pos.x, "Time Progress:",progress, "Direction:",direction, "FinalX:",overAllX);

            entity.moveX(toMoveX * direction, null);
            entity.moveY(Math.abs(neededY - entity.pos.y), null);

            if(progress >= 1) {
                entity.getGroundPoundStateMachine().changeState(FALL);
            }
        }
    },

    FALL() {
        private Vector2 vel;

        @Override
        public void enter(BossPlayer entity) {
            vel = entity.getVel();
            vel.y = 0;
            entity.getGPFallSound().play(Settings.soundVol);
        }

        @Override
        public void update(BossPlayer entity) {

            vel.y += -100;
            vel.x = 0;

            if(entity.isOnGround()) {
                entity.getGroundPoundStateMachine().changeState(INACTIVE);
                entity.getGPHitGroundSound().play(Settings.soundVol);
                entity.getRenderer().startGPDust();
                entity.getContainingScreen().setScreenShakes(3);
            }
        }
    },


    INACTIVE(),

    GLOBAL() {
        @Override
        public void update(BossPlayer entity) {
            if(entity.isGroundPounding() && entity.getEnemyPlayer().isTouchingEnemy()) {
                entity.getEnemyPlayer().attacked(20);
            }
        }
    }
    ;


    @Override
    public void enter(BossPlayer entity) {

    }

    @Override
    public void update(BossPlayer entity) {

    }

    @Override
    public void exit(BossPlayer entity) {

    }

    @Override
    public boolean onMessage(BossPlayer entity, Telegram telegram) {
        return false;
    }
}
