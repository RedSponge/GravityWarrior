package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

public enum GroundPoundState implements State<BossPlayer> {
    RAISE() {
        private int startY;
        private int wantedX;
        private int startX;
        private Vector2 vel;
        private int wantedY;

        @Override
        public void enter(BossPlayer entity) {
            super.enter(entity);
            this.wantedX = entity.getEnemyPlayer().pos.x - entity.size.x / 2;
            this.startX = entity.pos.x;
            this.vel = entity.getVel();
            this.wantedY = 300;
            this.startY = entity.pos.y;
        }

        @Override
        public void update(BossPlayer entity) {
            int direction = (entity.pos.x < wantedX ? 1 : -1);

            int distanceX;
            int overAllX;

            if(direction == 1) {
                distanceX = Math.abs(entity.pos.x - wantedX);
                overAllX = Math.abs(startX - wantedX);
            } else {
                distanceX = Math.abs(wantedX - entity.pos.x);
                overAllX = Math.abs(wantedX - startX);
            }

            float progress = 1 - (Math.abs(distanceX / (float) overAllX));

            vel.x = 500 * direction;
            vel.y = 0;
            entity.pos.y = startY + (int) (Interpolation.circleOut.apply(progress) * 100);
            Logger.log(this, "Current Progress:", progress);

            if(progress >= 0.9f || entity.pos.x == 1 || entity.pos.x + entity.size.x == Constants.GAME_WIDTH ) {
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
        }

        @Override
        public void update(BossPlayer entity) {
            Logger.log(this, "Falling!");
            vel.y += -100;
            vel.x = 0;

            if(entity.isOnGround()) {
                entity.getGroundPoundStateMachine().changeState(INACTIVE);
            }
        }
    },


    INACTIVE(),

    GLOBAL() {
        @Override
        public void update(BossPlayer entity) {
            if(entity.isGroundPounding() && entity.getEnemyPlayer().isTouchingEnemy()) {
                entity.getEnemyPlayer().takenHit();
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
