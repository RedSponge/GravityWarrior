package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.redsponge.upsidedownbb.game.MessageType;

public enum EnemyPlayerState implements State<EnemyPlayer> {

    RUN_AWAY() {

        @Override
        public void update(EnemyPlayer entity) {
            entity.moveAwayFromBoss();
            if(MathUtils.randomBoolean(0.05f) && (entity.distanceFromBoss() > 100 || entity.isTouchingWalls())) {
                if(MathUtils.randomBoolean()) {
                    entity.getStateMachine().changeState(SUPER_ATTACK_FROM_TOP);
                } else {
                    entity.getStateMachine().changeState(RUN_TO_ATTACK);
                }
            }
        }

        @Override
        public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
            switch (telegram.message) {
                case MessageType.BOSS_PUNCH_BEGIN:
                    if (entity.shouldDuck() && !entity.isDucking()) {
                        entity.getStateMachine().changeState(DUCK_DODGE);
                    }
                    break;
            }
            return super.onMessage(entity, telegram);
        }
    },

    RUN_TO_ATTACK() {
        @Override
        public void update(EnemyPlayer entity) {
            entity.runToBoss();
            if(entity.canAttackBoss()) {
                entity.attackBoss();
                entity.getStateMachine().changeState(RUN_AWAY);
            }
        }
    },

    SUPER_ATTACK_FROM_TOP() {
        @Override
        public void enter(EnemyPlayer entity) {
            entity.startGravityAttack();
        }

        @Override
        public void update(EnemyPlayer entity) {
            entity.updateGravityAttack();
        }
    },

    DUCK_DODGE() {
        @Override
        public void enter(EnemyPlayer entity) {
            entity.startDuck();
        }

        @Override
        public void update(EnemyPlayer entity) {
            entity.processDuck();
        }
    },

    HIT() {
        @Override
        public void enter(EnemyPlayer entity) {
            entity.takenHit();
        }

        @Override
        public void update(EnemyPlayer entity) {
            if(entity.hasRecoveredFromHit()) {
                entity.getStateMachine().changeState(RUN_AWAY);
            }
        }
    },

    GLOBAL_STATE() {
        @Override
        public void update(EnemyPlayer entity) {

        }

        @Override
        public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
            switch (telegram.message) {
                case MessageType.PLAYER_HIT:
                    if(entity.hasRecoveredFromHit()) {
                        entity.getStateMachine().changeState(HIT);
                    }
                    break;
            }
            return super.onMessage(entity, telegram);
        }
    };


    @Override
    public void enter(EnemyPlayer entity) {

    }

    @Override
    public void exit(EnemyPlayer entity) {

    }

    @Override
    public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
        return false;
    }
}
