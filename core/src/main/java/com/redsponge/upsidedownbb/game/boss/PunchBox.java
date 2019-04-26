package com.redsponge.upsidedownbb.game.boss;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.redsponge.upsidedownbb.game.MessageType;
import com.redsponge.upsidedownbb.game.enemy.EnemyPlayer;
import com.redsponge.upsidedownbb.physics.IUpdated;
import com.redsponge.upsidedownbb.physics.PActor;
import com.redsponge.upsidedownbb.physics.PhysicsWorld;
import com.redsponge.upsidedownbb.utils.GeneralUtils;

public class PunchBox extends PActor implements IUpdated, Telegraph {

    private EnemyPlayer toHit;

    public PunchBox(PhysicsWorld worldIn, EnemyPlayer toHit) {
        super(worldIn);
        this.toHit = toHit;
    }

    @Override
    public void update(float delta) {
        if(GeneralUtils.rectanglesIntersect(pos, size, toHit.pos, toHit.size)) {
            MessageManager.getInstance().dispatchMessage(0, this, toHit, MessageType.PLAYER_HIT);
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        return false;
    }

    @Override
    protected void remove() {
        super.remove();
    }
}
