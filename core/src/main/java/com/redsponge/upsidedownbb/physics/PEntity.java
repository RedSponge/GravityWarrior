package com.redsponge.upsidedownbb.physics;

import com.redsponge.upsidedownbb.utils.IntVector2;

/**
 * An object in a {@link PhysicsWorld}. has size and position.
 */
public class PEntity {

    public IntVector2 pos, size;
    protected PhysicsWorld worldIn;

    /**
     * If true, the world object will be removed from the {@link PhysicsWorld} on the next tick
     */
    private boolean removed;

    public PEntity(PhysicsWorld worldIn) {
        this.worldIn = worldIn;
        this.pos = new IntVector2();
        this.size = new IntVector2();
    }

    protected void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }
}
