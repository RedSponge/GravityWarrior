package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AssetDescBin.Boss;
import com.redsponge.upsidedownbb.assets.Assets;
import com.redsponge.upsidedownbb.game.MessageType;
import com.redsponge.upsidedownbb.game.Platform;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.input.InputTranslator;
import com.redsponge.upsidedownbb.input.SimpleInputTranslator;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.screen.GameScreen;
import com.redsponge.upsidedownbb.utils.Constants;
import com.redsponge.upsidedownbb.utils.GeneralUtils;
import com.redsponge.upsidedownbb.utils.Settings;

public class BossPlayer extends PActor implements IUpdated, Telegraph {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Boss.bite, Boss.gpFall, Boss.gpRise, Boss.hit};

    private InputTranslator input;
    private boolean onGround;
    private Vector2 vel;

    private int direction;

    private PunchBox punchBox;
    private long punchStartTime;
    private long gpStartTime;
    private long hitTime;

    private EnemyPlayer enemyPlayer;
    private long dashStart;
    private StateMachine<BossPlayer, GroundPoundState> groundPoundStateMachine;
    private int health;

    private GameScreen containingScreen;

    private Sound biteSound, gpRiseSound;
    private Sound gpFallSound;
    private Sound hitSound;

    private BossPlayerRenderer renderer;

    public BossPlayer(PhysicsWorld worldIn, GameScreen containingScreen, Assets assets) {
        super(worldIn);
        this.containingScreen = containingScreen;
        input = new SimpleInputTranslator();
        size.set(Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        pos.set((int) (Constants.ARENA_WIDTH / 2 - size.x / 2), 100);

        vel = new Vector2(0, 0);
        dashStart = 0;
        direction = 1;

        groundPoundStateMachine = new DefaultStateMachine<BossPlayer, GroundPoundState>(this, GroundPoundState.INACTIVE);
        groundPoundStateMachine.setGlobalState(GroundPoundState.GLOBAL);

        biteSound = assets.get(Boss.bite);
        gpRiseSound = assets.get(Boss.gpRise);
        gpFallSound = assets.get(Boss.gpFall);
        hitSound = assets.get(Boss.hit);

        health = Constants.BOSS_MAX_HEALTH;
    }

    public void setRenderer(BossPlayerRenderer renderer) {
        this.renderer = renderer;
    }

    public void setEnemyPlayer(EnemyPlayer enemyPlayer) {
        this.enemyPlayer = enemyPlayer;
    }

    @Override
    public void update(float delta) {
        if(!containingScreen.isGameFinished()) {
            if (input.isJustPunching() && !isPunching() && getPercentCooldownForPunch() >= 1) {
                beginPunch();
            }
            processPunch();
            groundPoundStateMachine.update();
        }


        if(!isPunching() && !isGroundPounding()) {
            vel.add(0, Constants.WORLD_GRAVITY);
            if(!containingScreen.isGameFinished()) {
                vel.x = direction * getSpeed();
                if (GeneralUtils.secondsSince(dashStart) < 0.2f) {
                    vel.x = direction * 400;
                }
                if (input.isJustDashing() && getPercentCooldownForDash() >= 1) {
                    dashStart = TimeUtils.nanoTime();
                }
                if (onGround && input.isJustJumping() && !isGroundPounding() && getPercentCooldownForGroundPound() >= 1) {
                    beginGroundPound();
                    onGround = false;
                }
            } else {
                if(onGround) {
                    vel.x = 0;
                }
            }
        }

        if(!isPunching()) {
            moveX(vel.x * delta, null);
            moveY(vel.y * delta, () -> {
                if (vel.y < 0) {
                    onGround = true;
                }
            });
        }
        onGround = collideFirst(pos.copy().add(0, -1)) instanceof Platform;
    }

    public float getPercentCooldownForPunch() {
        return GeneralUtils.secondsSince(punchStartTime) / Constants.PUNCH_COOLDOWN;
    }

    public float getPercentCooldownForGroundPound() {
        return GeneralUtils.secondsSince(gpStartTime) / Constants.GROUND_POUND_COOLDOWN;
    }

    public boolean isGroundPounding() {
        return groundPoundStateMachine.getCurrentState() != GroundPoundState.INACTIVE;
    }

    private void beginGroundPound() {
        gpStartTime = TimeUtils.nanoTime();
        groundPoundStateMachine.changeState(GroundPoundState.RAISE);
        gpRiseSound.play(Settings.soundVol);
    }

    public void attacked(int health) {
        if(groundPoundStateMachine.getCurrentState() != GroundPoundState.INACTIVE) return;

        this.health -= health;
        hitSound.play(Settings.soundVol);
        hitTime = TimeUtils.nanoTime();
    }

    public boolean isPunching() {
        return GeneralUtils.secondsSince(punchStartTime) < Constants.PUNCH_LENGTH;
    }

    private void processPunch() {
        if(isPunching() && GeneralUtils.secondsSince(punchStartTime) > Constants.PUNCH_BOX_DELAY && punchBox == null) {
            createPunchBox();
        }
        if(GeneralUtils.secondsSince(punchStartTime) > Constants.PUNCH_LENGTH && punchBox != null) {
            endPunch();
        }
    }

    private void createPunchBox() {
        punchBox = new PunchBox(worldIn);
        int dir = getDirection();
        int offsetX = dir == 1 ? size.x + 1 : -Constants.PUNCH_SIZE.x;
        punchBox.pos.set(pos.copy().add(offsetX, Constants.PUNCH_SIZE.y / 2));
        punchBox.size.set(Constants.PUNCH_SIZE.copy());

        worldIn.addActor(punchBox);

        MessageManager.getInstance().dispatchMessage(0, this, enemyPlayer, MessageType.BOSS_PUNCH_BEGIN);
    }

    private void endPunch() {
        if(punchBox != null) {
            punchBox.remove();
            punchBox = null;
        }
    }

    public float getPercentCooldownForDash() {
        return GeneralUtils.secondsSince(dashStart) / Constants.DASH_COOLDOWN;
    }

    private void beginPunch() {
        punchStartTime = TimeUtils.nanoTime();
        biteSound.play(Settings.soundVol);
    }

    public int getDirection() {
        return direction;
    }

    private float getSpeed() {
        return 80;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
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

    public PunchBox getPunchBox() {
        return punchBox;
    }

    public long getPunchStartTime() {
        return punchStartTime;
    }

    public Sound getGPFallSound() {
        return gpFallSound;
    }

    public BossPlayerRenderer getRenderer() {
        return renderer;
    }

    public int getHealth() {
        return health;
    }

    public long getHitTime() {
        return hitTime;
    }
}