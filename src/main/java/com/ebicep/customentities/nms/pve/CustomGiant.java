package com.ebicep.customentities.nms.pve;

import net.minecraft.server.v1_8_R3.EntityGiantZombie;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class CustomGiant extends EntityGiantZombie implements CustomEntity<CustomGiant> {

    public CustomGiant(World world) {
        super(world);
    }

    public CustomGiant(org.bukkit.World world) {
        this(((CraftWorld) world).getHandle());
    }

    @Override
    public CustomGiant get() {
        return this;
    }

}
