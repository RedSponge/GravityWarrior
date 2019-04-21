package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.game.MessageType;
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

    private int wantedX;

    private PunchBox punchBox;
    private long punchStartTime;

    private EnemyPlayer enemyPlayer;

    public BossPlayer(PhysicsWorld worldIn) {
        super(worldIn);
        input = new SimpleInputTranslator();
        size.set(200, 200);
        pos.set((int) (Constants.GAME_WIDTH / 2 - size.x / 2), 100);

        vel = new Vector2(0, 0);
        wantedX = 0;
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

        if(!isPunching()) {
            vel.add(0, Constants.WORLD_GRAVITY);
            vel.x = Math.signum(wantedX - pos.x) * getSpeed();

            if(onGround && input.isJustJumping()) {
                vel.y = 200;
                onGround = false;
            }

            moveX(vel.x * delta, null);
            moveY(vel.y * delta, () -> {
                if (vel.y < 0) {
                    onGround = true;
                }
            });
        }
    }

    private boolean isPunching() {
        return punchBox != null;
    }

    private void processPunch() {
        if(GeneralUtils.secondsSince(punchStartTime) > Constants.PUNCH_LENGTH && punchBox != null) {
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
        return vel.x >= 0 ? 1 : -1;
    }

    private float getSpeed() {
        if(onGround) {
            return 30;
        }
        return 50;
    }

    public void setWantedX(int x) {
        this.wantedX = x - size.x / 2;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }
}