package com.redsponge.upsidedownbb.game.boss;

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
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.input.InputTranslator;
import com.redsponge.upsidedownbb.input.SimpleInputTranslator;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;

public class BossPlayer extends PActor implements IUpdated, Telegraph {

    private InputTranslator input;
    private boolean onGround;
    private Vector2 vel;

    private int direction;

    private PunchBox punchBox;
    private long punchStartTime;

    private EnemyPlayer enemyPlayer;
    private long dashStart;
    private StateMachine<BossPlayer, GroundPoundState> groundPoundStateMachine;

    public BossPlayer(PhysicsWorld worldIn) {
        super(worldIn);
        input = new SimpleInputTranslator();
        size.set(Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        pos.set((int) (Constants.GAME_WIDTH / 2 - size.x / 2), 100);

        vel = new Vector2(0, 0);
        dashStart = 0;
        direction = 1;

        groundPoundStateMachine = new DefaultStateMachine<BossPlayer, GroundPoundState>(this, GroundPoundState.INACTIVE);
        groundPoundStateMachine.setGlobalState(GroundPoundState.GLOBAL);
    }

    public void setEnemyPlayer(EnemyPlayer enemyPlayer) {
        this.enemyPlayer = enemyPlayer;
    }

    @Override
    public void update(float delta) {
        if(input.isJustPunching() && !isPunching()) {
            beginPunch();
        }
        processPunch();
        groundPoundStateMachine.update();

        if(!isPunching() && !isGroundPounding()) {
            vel.add(0, Constants.WORLD_GRAVITY);
            vel.x = direction * getSpeed();
            if(GeneralUtils.secondsSince(dashStart) < 0.2f) {
                vel.x *= 20;
            }
            if(Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT)) {
                dashStart = TimeUtils.nanoTime();
            }
            if(onGround && input.isJustJumping() && !isGroundPounding()) {
                beginGroundPound();
                onGround = false;
            }
        }

        moveX(vel.x * delta, null);
        moveY(vel.y * delta, () -> {
            if (vel.y < 0) {
                onGround = true;
            }
        });
        onGround = collideFirst(pos.copy().add(0, -1)) instanceof Platform;
    }

    public boolean isGroundPounding() {
        return groundPoundStateMachine.getCurrentState() != GroundPoundState.INACTIVE;
    }

    private void beginGroundPound() {
        groundPoundStateMachine.changeState(GroundPoundState.RAISE);
    }

    private boolean isPunching() {
        return punchBox != null;
    }

    private void processPunch() {
        if(GeneralUtils.secondsSince(punchStartTime) > Constants.PUNCH_LENGTH && punchBox != null) {
            endPunch();
        }
    }

    private void endPunch() {
        if(punchBox != null) {
            punchBox.remove();
            punchBox = null;
        }
    }

    private void beginPunch() {
        punchBox = new PunchBox(worldIn);

        int dir = getDirection();
        int offsetX = dir == 1 ? size.x + 1 : -Constants.PUNCH_SIZE.x;
        punchBox.pos.set(pos.copy().add(offsetX, Constants.PUNCH_SIZE.y / 2));
        punchBox.size.set(Constants.PUNCH_SIZE.copy());

        worldIn.addSolid(punchBox);
        punchStartTime = TimeUtils.nanoTime();

        MessageManager.getInstance().dispatchMessage(0, this, enemyPlayer, MessageType.BOSS_PUNCH_BEGIN);
    }

    public int getDirection() {
        return direction;
    }

    private float getSpeed() {
        if(onGround) {
            return 30;
        }
        return 50;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        switch (msg.message) {
            case MessageType.PLAYER_HIT:
                endPunch();
                break;
        }
        return false;
    }

    public EnemyPlayer getEnemyPlayer() {
        return enemyPlayer;
    }

    public Vector2 getVel() {
        return vel;
    }

    public StateMachine<BossPlayer, GroundPoundState> getGroundPoundStateMachine() {
        return groundPoundStateMachine;
    }

    public boolean isOnGround() {
        return onGround;
    }
}