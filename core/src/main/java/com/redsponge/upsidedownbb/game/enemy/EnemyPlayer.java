package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.game.boss.PunchBox;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;

public class EnemyPlayer extends PActor implements IUpdated, Telegraph {

    private boolean gravitySwitched;
    private Vector2 vel;
    private BossPlayer boss;
    private long duckStart;

    private StateMachine<EnemyPlayer, EnemyPlayerState> stateMachine;

    public EnemyPlayer(PhysicsWorld worldIn, BossPlayer boss) {
        super(worldIn);
        this.boss = boss;
        pos.set(200, 200);
        size.set(30, 60);
        vel = new Vector2();

        stateMachine = new DefaultStateMachine<EnemyPlayer, EnemyPlayerState>(this, EnemyPlayerState.RUN_AWAY);
        stateMachine.setGlobalState(EnemyPlayerState.GLOBAL_STATE);
    }

    @Override
    public void update(float delta) {
        if(Gdx.input.isKeyJustPressed(Keys.I)) {
            gravitySwitched = !gravitySwitched;
            vel.y = 0;
        }

        stateMachine.update();

        int mult = gravitySwitched ? -1 : 1;
        vel.add(0, Constants.WORLD_GRAVITY * mult);
        moveY(vel.y * delta, null);
        moveX(vel.x * delta, null);

        PSolid collision = collideFirst(pos);
        if(collision instanceof PunchBox) {
            remove();
        }

        if(GeneralUtils.secondsSince(duckStart) > 0.5f) {
            endDuck();
        }
    }

    public void moveAwayFromBoss() {
        vel.x = Math.signum(pos.x - boss.pos.x) * 50;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return stateMachine.handleMessage(msg);
    }

    public void startDuck() {
        size.y = 10;
        duckStart = TimeUtils.nanoTime();
    }

    public void endDuck() {
        size.y = 60;
    }
}
