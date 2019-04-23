package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
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
        private long startTime;

        @Override
        public void enter(BossPlayer entity) {
            super.enter(entity);
            this.wantedX = entity.getEnemyPlayer().pos.x - entity.size.x / 2;
            this.startX = entity.pos.x;
            this.vel = entity.getVel();
            this.wantedY = 200;
            this.startY = entity.pos.y;
            startTime = TimeUtils.nanoTime();
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

            float progress = GeneralUtils.secondsSince(startTime) / 0.5f;

            vel.y = 0;
            vel.x = 0;

            int neededY = startY + (int) (Interpolation.circleOut.apply(progress) * wantedY);
            int neededX = startX + (int) (Interpolation.circleOut.apply(progress) * overAllX) * direction;

            entity.moveX(Math.abs(neededX - entity.pos.x) * direction, null);
            entity.moveY(Math.abs(neededY - entity.pos.y), null);


            if(progress >= 0.9f) {
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
