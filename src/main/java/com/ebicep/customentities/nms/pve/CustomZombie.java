package com.ebicep.customentities.nms.pve;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class CustomZombie extends EntityZombie implements CustomEntity {

    public CustomZombie(World world) {
        super(world);
        setBaby(false);
    }


    @Override
    public void spawn(Location location) {
        setPosition(location.getX(), location.getY(), location.getZ());
        getBukkitEntity().setCustomNameVisible(true);

        ((CraftWorld) location.getWorld()).getHandle().addEntity(this);

    }

    @Override
    public EntityInsentient get() {
        return this;
    }

}