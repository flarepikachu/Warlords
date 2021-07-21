package com.ebicep.customentities;

import com.ebicep.warlords.player.WarlordsPlayer;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomHorse extends EntityHorse {

    private WarlordsPlayer owner;
    private float speed = .308f;

    public CustomHorse(World world, WarlordsPlayer owner) {
        super(world);
        this.owner = owner;
    }

    public void spawn() {
        Player player = (Player) owner.getEntity();
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.setTamed(true);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.setOwner(player);
        horse.setJumpStrength(0);
        horse.setVariant(Horse.Variant.HORSE);
        horse.setAdult();
        ((EntityLiving) ((CraftEntity) horse).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        horse.setPassenger(player);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
