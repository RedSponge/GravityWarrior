package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.redsponge.upsidedownbb.game.MessageType;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

public enum GravityAttackState implements State<EnemyPlayer> {

    INACTIVE() {

        @Override
        public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
            if(telegram.message == MessageType.PLAYER_FINISHED_GRAVITY) {
                entity.getStateMachine().changeState(EnemyPlayerState.RUN_AWAY);
                entity.setHeadStuck(false);
            }
            return false;
        }

    },

    FLYING() {
        @Override
        public void enter(EnemyPlayer entity) {
            entity.setGravitySwitched(true);
        }

        @Override
        public void update(EnemyPlayer entity) {
            if(entity.pos.y + entity.size.y >= Constants.CEILLING_HEIGHT - 1) {
                entity.getGravityAttackStateMachine().changeState(RUNNING);
            }
        }
    },

    RUNNING() {
        private Vector2 vel;

        @Override
        public void enter(EnemyPlayer entity) {
            vel = entity.getVel();
        }

        @Override
        public void update(EnemyPlayer entity) {
            vel.x =- entity.getRelativePositionFromBossMultiplier() * 400;
            if(Math.abs((entity.pos.x + entity.size.x /2f)-(entity.getBoss().pos.x + entity.getBoss().size.x /2f)) < 2){
                vel.x = 0;
                vel.y = 0;
                entity.getGravityAttackStateMachine().changeState(PLUNGING);
            }
        }
    },

    PLUNGING() {
        private boolean hit;

        @Override
        public void enter(EnemyPlayer entity) {
            hit = false;
            entity.setGravitySwitched(false);
        }

        @Override
        public void update(EnemyPlayer entity) {
            BossPlayer boss = entity.getBoss();
            if(GeneralUtils.rectanglesIntersect(boss.pos, boss.size, entity.pos.copy().add(0, 1), entity.size)) {
                hit = true;
                entity.knockBack();
                Logger.log(this, "Hit Enemy With Plunge!");
                entity.getBoss().attacked(Constants.PLUNGE_ATTACK_DAMAGE);
            }

            if(entity.pos.y < 300) {
                if(entity.pos.y <= Constants.FLOOR_HEIGHT + 1) {
                    float delay = 0.5f;
                    if(hit) {
                        Logger.log(this, "Ended With Successful Plunge!");
                    } else {
                        delay = 5;
                        Logger.log(this, "Stuck In Ground!");
                        entity.startHeadStuck();
                        entity.getVel().set(0, 0);
                    }
                    MessageManager.getInstance().dispatchMessage(delay, entity, entity, MessageType.PLAYER_FINISHED_GRAVITY);
                    entity.getGravityAttackStateMachine().changeState(INACTIVE);
                }
            }
        }
    }

    ;

    @Override
    public void enter(EnemyPlayer entity) {

    }

    @Override
    public void update(EnemyPlayer entity) {

    }

    @Override
    public void exit(EnemyPlayer entity) {

    }

    @Override
    public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
        return false;
    }
}
