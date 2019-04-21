package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.redsponge.upsidedownbb.game.MessageType;

public enum EnemyPlayerState implements State<EnemyPlayer> {

    RUN_AWAY() {
        @Override
        public void update(EnemyPlayer entity) {
            entity.moveAwayFromBoss();
        }

        @Override
        public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
            return super.onMessage(entity, telegram);
        }
    },

    GLOBAL_STATE() {
        @Override
        public void update(EnemyPlayer entity) {

        }

        @Override
        public boolean onMessage(EnemyPlayer entity, Telegram telegram) {
            if(telegram.message == MessageType.BOSS_PUNCH_BEGIN) {
                entity.startDuck();
            } else if(telegram.message == MessageType.BOSS_PUNCH_END) {
                entity.endDuck();
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
