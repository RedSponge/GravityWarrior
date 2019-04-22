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
    private boolean plungeHit;

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
        return Math.abs((pos.x + size.x / 2) - (boss.pos.x + boss.size.x / 2));
    }

    private int getRelativePositionFromBossMultiplier() {
        return (int) Math.signum((pos.x + size.x / 2) - (boss.pos.x + boss.size.x / 2));
    }

    public void knockBack() {
        vel.x = 100 * getRelativePositionFromBossMultiplier();
        vel.y = 300;
        onGround = false;
    }

    public void takenHit() {
        knockBack();
        hitTime = TimeUtils.nanoTime();
        gravitySwitched = false;
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
            vel.x = -getRelativePositionFromBossMultiplier() * 200;
            if(Math.abs((pos.x + size.x / 2f) - (boss.pos.x + boss.size.x / 2f)) < 2) {
                vel.x = 0;
                vel.y = 0;
                gravityAttackStage = GravityAttackStage.PLUNGING;
                onGround = false;
                plungeHit = false;
            }
        } else if(gravityAttackStage == GravityAttackStage.PLUNGING) {
            gravitySwitched = false;
            if(GeneralUtils.rectanglesIntersect(pos, size, boss.pos, boss.size)) {
                Logger.log(this, "Hit enemy with plunge!");
                knockBack();
                plungeHit = true;
            }
            if(pos.y + size.y > 400) {
                onGround = false;
            } else if(onGround) {
                Logger.log(this, "Ended attack!");
                gravityAttackStage = GravityAttackStage.INACTIVE;
                if(plungeHit) {
                    stateMachine.changeState(EnemyPlayerState.RUN_AWAY);
                } else {
                    hitTime = TimeUtils.nanoTime();
                    stateMachine.changeState(EnemyPlayerState.HIT);
                    vel.set(0, 0);
                }
            }
        }
    }

    public void setGravitySwitched(boolean gravitySwitched) {
        this.gravitySwitched = gravitySwitched;
    }
}
