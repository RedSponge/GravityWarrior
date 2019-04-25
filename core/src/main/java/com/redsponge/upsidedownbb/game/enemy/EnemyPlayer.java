package com.redsponge.upsidedownbb.game.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Enemy;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.game.MessageType;
import com.redsponge.upsidedownbb.game.Platform;
import com.redsponge.upsidedownbb.game.boss.BossPlayer;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.screen.GameScreen;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Logger;

public class EnemyPlayer extends PActor implements IUpdated, Telegraph {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Enemy.attack};

    private boolean gravitySwitched;
    private Vector2 vel;
    private BossPlayer boss;
    private long duckStart;
    private long attackStart;

    private StateMachine<EnemyPlayer, EnemyPlayerState> stateMachine;
    private StateMachine<EnemyPlayer, GravityAttackState> gravityAttackStateMachine;
    private long hitTime;
    private boolean onGround;
    private boolean headStuck;
    private Sound attackSound;

    private int health;
    private GameScreen containingScreen;

    public EnemyPlayer(PhysicsWorld worldIn, BossPlayer boss, Assets assets, GameScreen containingScreen) {
        super(worldIn);
        this.boss = boss;
        this.containingScreen = containingScreen;
        pos.set(100, 100);
        size.set(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        vel = new Vector2();

        stateMachine = new DefaultStateMachine<EnemyPlayer, EnemyPlayerState>(this, EnemyPlayerState.RUN_AWAY);
        stateMachine.setGlobalState(EnemyPlayerState.GLOBAL_STATE);

        gravityAttackStateMachine = new DefaultStateMachine<EnemyPlayer, GravityAttackState>(this, GravityAttackState.INACTIVE);

        attackSound = assets.get(Enemy.attack);
        health = Constants.PLAYER_MAX_HEALTH;
    }

    @Override
    public void update(float delta) {
        if(Gdx.input.isKeyJustPressed(Keys.I)) {
            gravitySwitched = !gravitySwitched;
            vel.y = 0;
        }

        int mult = gravitySwitched ? -1 : 1;
        vel.add(0, Constants.WORLD_GRAVITY * mult);

        if(!containingScreen.isGameFinished()) {

            if (onGround) {
                vel.x *= 0.9f;
            }
        }

        moveY(vel.y * delta, null);

        if(containingScreen.isGameFinished()) vel.x = 0;

        moveX(vel.x * delta, () -> {if(!hasRecoveredFromHit()) {vel.x *= -1;}});

        onGround = collideFirst(pos.copy().add(0, -1)) instanceof Platform
        || collideFirst(pos.copy().add(0, size.y + 1)) instanceof Platform;

        if(!containingScreen.isGameFinished()) {
            if (boss.getPunchBox() != null) {
                if (GeneralUtils.rectanglesIntersect(pos, size, boss.getPunchBox().pos, boss.getPunchBox().size)) {
                    MessageManager.getInstance().dispatchMessage(0, this, this, MessageType.PLAYER_HIT);
                    MessageManager.getInstance().dispatchMessage(0, this, boss, MessageType.PLAYER_HIT);
                }
            }

            if (hasRecoveredFromHit() && !isAttacking()) ;
            stateMachine.update();
        }
    }

    public void moveAwayFromBoss() {
        vel.x = Math.signum(pos.x - boss.pos.x) * 100;
    }

    public StateMachine<EnemyPlayer, EnemyPlayerState> getStateMachine() {
        return stateMachine;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return stateMachine.handleMessage(msg) || gravityAttackStateMachine.handleMessage(msg);
    }

    public void startDuck() {
        size.y = Constants.PLAYER_DUCK_HEIGHT;
        duckStart = TimeUtils.nanoTime();
    }

    public void endDuck() {
        size.y = Constants.PLAYER_HEIGHT;
        stateMachine.changeState(EnemyPlayerState.RUN_AWAY);
    }

    public boolean isRunning() {
        return vel.x != 0 && !isTouchingWalls();
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
        return distanceFromBoss() < 20 && boss.isOnGround() && !isAttacking();
    }

    public void attackBoss() {
        Logger.log(this, "Attacked Boss!");
        attackStart = TimeUtils.nanoTime();
        attackSound.play();
        boss.attacked(Constants.REGULAR_HIT_DAMAGE);
    }

    public boolean isAttacking() {
        return GeneralUtils.secondsSince(attackStart) < Constants.SLICE_LENGTH;
    }

    public int getHealth() {
        return health;
    }

    public int distanceFromBoss() {
        return Math.abs((pos.x + size.x / 2) - (boss.pos.x + boss.size.x / 2)) - Constants.BOSS_WIDTH / 2;
    }

    public int getRelativePositionFromBossMultiplier() {
        return (int) Math.signum((pos.x + size.x / 2) - (boss.pos.x + boss.size.x / 2));
    }

    public int getBestKnockbackMultiplier() {
        return getRelativePositionFromBossMultiplier() * (isTouchingWalls() ? -1 : 1);
    }

    public void knockBack() {
        vel.x = 100 * getBestKnockbackMultiplier();
        vel.y = 300;
        onGround = false;
    }

    public void attacked(int health) {
        attacked(false, health);
    }

    public void attacked(boolean bypassInvincibility, int health) {
        if(hasRecoveredFromHit() || bypassInvincibility) {
            knockBack();
            hitTime = TimeUtils.nanoTime();
            gravitySwitched = false;
            headStuck = false;
            gravityAttackStateMachine.changeState(GravityAttackState.INACTIVE);
            stateMachine.changeState(EnemyPlayerState.GOT_HIT);
            this.health -= health;
        }
    }

    public boolean hasRecoveredFromHit() {
        return GeneralUtils.secondsSince(hitTime) > 1;
    }

    public boolean isTouchingWalls() {
        return pos.x == 1 || pos.x + size.x == Constants.ARENA_WIDTH - 1;
    }

    public boolean isDucking() {
        return GeneralUtils.secondsSince(duckStart) < 0.5f;
    }

    public void startGravityAttack() {
        gravityAttackStateMachine.changeState(GravityAttackState.FLYING);
        vel.y = 0;
    }

    public void updateGravityAttack() {
        gravityAttackStateMachine.update();
    }

    public void setGravitySwitched(boolean gravitySwitched) {
        this.gravitySwitched = gravitySwitched;
    }

    public boolean isTouchingEnemy() {
        return GeneralUtils.rectanglesIntersect(pos, size, boss.pos, boss.size);
    }

    public boolean isPowered() {
        return gravitySwitched;
    }

    public Vector2 getVel() {
        return vel;
    }

    public int getDirection() {
        return vel.x < 0 ? -1 : 1;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public StateMachine<EnemyPlayer, GravityAttackState> getGravityAttackStateMachine() {
        return gravityAttackStateMachine;
    }

    public BossPlayer getBoss() {
        return boss;
    }

    public void startHeadStuck() {
        hitTime = TimeUtils.nanoTime();
        headStuck = true;
    }

    public void setHeadStuck(boolean headStuck) {
        this.headStuck = headStuck;
    }

    public boolean isHeadStuck() {
        return gravityAttackStateMachine.getCurrentState() == GravityAttackState.INACTIVE && headStuck;
    }

    public long getDuckStartTime() {
        return duckStart;
    }

    public long getAttackStartTime() {
        return attackStart;
    }

    public long getHitTime() {
        return hitTime;
    }
}
