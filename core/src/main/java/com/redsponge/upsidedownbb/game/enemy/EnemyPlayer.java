package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.game.MessageType;
import com.redsponge.upsidedownbb.game.Platform;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.game.boss.PunchBox;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PSolid;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

public class EnemyPlayer extends PActor implements IUpdated, Telegraph {

    private boolean gravitySwitched;
    private Vector2 vel;
    private BossPlayer boss;
    private long duckStart;

    private StateMachine<EnemyPlayer, EnemyPlayerState> stateMachine;
    private long hitTime;
    private boolean onGround;

    private int gravityAttackStage;

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

        if(onGround) {
            vel.x *= 0.9f;
        }

        moveY(vel.y * delta, null);
        moveX(vel.x * delta, null);

        if(collideFirst(pos.copy().add(0, -1)) instanceof Platform) {onGround = true;}
        if(collideFirst(pos.copy().add(0, size.y + 1)) instanceof Platform) {onGround = true;}

        PSolid collision = collideFirst(pos);
        if(collision instanceof PunchBox) {
            MessageManager.getInstance().dispatchMessage( 0, this, this, MessageType.PLAYER_HIT);
            MessageManager.getInstance().dispatchMessage( 0, this, boss, MessageType.PLAYER_HIT);
        }
    }

    public void moveAwayFromBoss() {
        vel.x = Math.signum(pos.x - boss.pos.x) * 50;
    }

    public StateMachine<EnemyPlayer, EnemyPlayerState> getStateMachine() {
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
        stateMachine.changeState(EnemyPlayerState.RUN_AWAY);
    }

    public boolean shouldDuck() {
        if(!onGround) return false;
        return boss.getDirection() == getRelativePositionFromBossMultiplier() && distanceFromBoss() < 200;
    }

    public void processDuck() {
        if(GeneralUtils.secondsSince(duckStart) > 0.5f) {
            endDuck();
        }
    }

    public void runToBoss() {
        vel.x = Math.signum(boss.pos.x - pos.x) * 150;
    }

    public boolean canAttackBoss() {
        return distanceFromBoss() < 20;
    }

    public void attackBoss() {
        Logger.log(this, "Attacked Boss!");
    }

    public int distanceFromBoss() {
        int selfX, bossX;
        if(pos.x > boss.pos.x + boss.size.x) {
            selfX = pos.x;
            bossX = boss.pos.x + boss.size.x;
        } else {
            selfX = pos.x + size.x;
            bossX = boss.pos.x;
        }
        return Math.abs(bossX - selfX);
    }

    private int getRelativePositionFromBossMultiplier() {
        int selfX, bossX;
        if(pos.x > boss.pos.x + boss.size.x) {
            selfX = pos.x;
            bossX = boss.pos.x + boss.size.x;
        } else {
            selfX = pos.x + size.x;
            bossX = boss.pos.x;
        }
        return (int) Math.signum(selfX - bossX);
    }

    public void knockBack() {
        vel.x = 100 * getRelativePositionFromBossMultiplier();
        vel.y = 300;
        onGround = false;
    }

    public void takenHit() {
        knockBack();
        hitTime = TimeUtils.nanoTime();
    }

    public boolean hasRecoveredFromHit() {
        return GeneralUtils.secondsSince(hitTime) > 1;
    }

    public boolean isTouchingWalls() {
        return pos.x == 1 || pos.x + size.x == Constants.GAME_WIDTH;
    }

    public boolean isDucking() {
        return GeneralUtils.secondsSince(duckStart) < 0.5f;
    }

    public void startGravityAttack() {
        gravitySwitched = true;
        vel.y = 0;
        gravityAttackStage = GravityAttackStage.FLYING;
    }

    public void updateGravityAttack() {
        if(gravityAttackStage == GravityAttackStage.FLYING) {
            if(pos.y + size.y == Constants.GAME_HEIGHT - 1) {
                gravityAttackStage = GravityAttackStage.RUNNING_TO_ENEMY;
            }
        }
        else if(gravityAttackStage == GravityAttackStage.RUNNING_TO_ENEMY) {
            vel.x = (boss.pos.x + boss.size.x / 2) - (pos.x + size.x / 2);
            if(Math.abs((pos.x + size.x / 2f) - (boss.pos.x + boss.size.x / 2f)) < 2) {
                vel.x = 0;
                vel.y = 0;
                gravityAttackStage = GravityAttackStage.PLUNGING;
            }
        } else if(gravityAttackStage == GravityAttackStage.PLUNGING) {
            gravitySwitched = false;
            if(onGround) {
                gravityAttackStage = GravityAttackStage.INACTIVE;
            }
        }
    }
}
