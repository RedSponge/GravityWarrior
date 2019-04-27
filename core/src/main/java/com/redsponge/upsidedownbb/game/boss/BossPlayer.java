package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
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
import com.redsponge.upsidedownbb.utils.Settings;

public class BossPlayer extends PActor implements IUpdated, Telegraph {

    public static final AssetDescriptor[] REQUIRED_ASSETS = {Boss.bite, Boss.gpFall, Boss.gpRise, Boss.hit, Boss.gpHitGround, Boss.dash};

    private InputTranslator input;
    private boolean onGround;
    private Vector2 vel;

    private int direction;

    private PunchBox punchBox;
    private float punchTimeCounter;
    private float gpTimeCounter;
    private float timeSinceHit;
    private float dashTimeCounter;

    private EnemyPlayer enemyPlayer;
    private StateMachine<BossPlayer, GroundPoundState> groundPoundStateMachine;
    private int health;

    private GameScreen containingScreen;

    private Sound biteSound, gpRiseSound;
    private Sound gpFallSound;
    private Sound hitSound;
    private Sound gpHitGroundSound;
    private Sound dashSound;

    private BossPlayerRenderer renderer;

    public BossPlayer(PhysicsWorld worldIn, GameScreen containingScreen, Assets assets) {
        super(worldIn);
        this.containingScreen = containingScreen;
        input = new SimpleInputTranslator();
        size.set(Constants.BOSS_WIDTH, Constants.BOSS_HEIGHT);
        pos.set((int) (Constants.ARENA_WIDTH / 2 - size.x / 2), Constants.FLOOR_HEIGHT);

        vel = new Vector2(0, 0);
        dashTimeCounter = 0;
        direction = 1;

        groundPoundStateMachine = new DefaultStateMachine<BossPlayer, GroundPoundState>(this, GroundPoundState.INACTIVE);
        groundPoundStateMachine.setGlobalState(GroundPoundState.GLOBAL);

        biteSound = assets.get(Boss.bite);
        gpRiseSound = assets.get(Boss.gpRise);
        gpFallSound = assets.get(Boss.gpFall);
        hitSound = assets.get(Boss.hit);
        gpHitGroundSound = assets.get(Boss.gpHitGround);
        dashSound = assets.get(Boss.dash);

        health = Constants.BOSS_MAX_HEALTH;

        // Start at 10 to not trigger at the beginning
        dashTimeCounter = 10;
        punchTimeCounter = 10;
        gpTimeCounter = 10;
        timeSinceHit = 10;
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

            gpTimeCounter += delta;
            punchTimeCounter += delta;
            dashTimeCounter += delta;
        }
        timeSinceHit += delta;


        if(!isPunching() && !isGroundPounding()) {
            vel.add(0, Constants.WORLD_GRAVITY);
            if(!containingScreen.isGameFinished()) {
                vel.x = direction * getSpeed();
                if (isDashing()) {
                    vel.x = direction * 400;
                }
                if (input.isJustDashing() && getPercentCooldownForDash() >= 1) {
                    beginDash();
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

    private void beginDash() {
        dashSound.play(Settings.soundVol);
        dashTimeCounter = 0;
        renderer.startDash();
    }

    public float getPercentCooldownForPunch() {
        return punchTimeCounter / Constants.PUNCH_COOLDOWN;
    }

    public float getPercentCooldownForGroundPound() {
        return gpTimeCounter / Constants.GROUND_POUND_COOLDOWN;
    }

    public boolean isGroundPounding() {
        return groundPoundStateMachine.getCurrentState() != GroundPoundState.INACTIVE;
    }

    private void beginGroundPound() {
        gpTimeCounter = 0;
        groundPoundStateMachine.changeState(GroundPoundState.RISE);
        gpRiseSound.play(Settings.soundVol);
    }

    public void attacked(int health, boolean bypassInvincibility) {
        if(timeSinceHit < 0.5f && !bypassInvincibility) return;
        if(groundPoundStateMachine.getCurrentState() != GroundPoundState.INACTIVE) return;

        this.health -= health;
        hitSound.play(Settings.soundVol);
        timeSinceHit = 0;
    }

    public boolean isPunching() {
        return punchTimeCounter < Constants.PUNCH_LENGTH;
    }

    private void processPunch() {
        if(isPunching() && punchTimeCounter > Constants.PUNCH_BOX_DELAY && punchBox == null) {
            createPunchBox();
        }
        if(punchTimeCounter > Constants.PUNCH_LENGTH && punchBox != null) {
            endPunch();
        }
    }

    private void createPunchBox() {
        punchBox = new PunchBox(worldIn, enemyPlayer);
        int dir = getDirection();
        int offsetX = dir == 1 ? size.x + 1 : -Constants.PUNCH_SIZE.x;
        punchBox.pos.set(pos.copy().add(offsetX, size.y / 2 - Constants.PUNCH_SIZE.y / 2));
        punchBox.size.set(Constants.PUNCH_SIZE.copy());

        worldIn.addActor(punchBox);

        MessageManager.getInstance().dispatchMessage(0, this, enemyPlayer, MessageType.BOSS_PUNCH_BEGIN);
    }

    public Sound getGPHitGroundSound() {
        return gpHitGroundSound;
    }

    private void endPunch() {
        if(punchBox != null) {
            punchBox.remove();
            punchBox = null;
        }
    }

    public float getPercentCooldownForDash() {
        return dashTimeCounter / Constants.DASH_COOLDOWN;
    }

    private void beginPunch() {
        punchTimeCounter = 0;
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

    public float getPunchTimeCounter() {
        return punchTimeCounter;
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

    public float getTimeSinceHit() {
        return timeSinceHit;
    }

    public GameScreen getContainingScreen() {
        return containingScreen;
    }

    public boolean isDashing() {
        return dashTimeCounter < 0.2f;
    }
}