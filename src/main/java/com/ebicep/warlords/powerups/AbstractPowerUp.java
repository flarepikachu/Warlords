package com.ebicep.warlords.powerups;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractPowerUp {

    protected Location location;
    protected ArmorStand powerUp;
    //protected EntityArmorStand powerUp;
    protected int duration;
    protected int cooldown;
    protected int maxCooldown;
    protected int timeToSpawn;

    public AbstractPowerUp(Location location, int duration, int cooldown, int timeToSpawn) {
        this.location = location;
        this.duration = duration;
        this.cooldown = 0;
        this.maxCooldown = cooldown;
        this.timeToSpawn = timeToSpawn;
    }

    public void spawn() {
        powerUp = location.getWorld().spawn(location.clone().add(0, -1.5, 0), ArmorStand.class);
        if (this instanceof DamagePowerUp) {
            powerUp.setCustomName("§c§lDAMAGE");
            powerUp.setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
        } else if (this instanceof EnergyPowerUp) {
            powerUp.setCustomName("§6§lENERGY");
            powerUp.setHelmet(new ItemStack(Material.WOOL, 1, (short) 1));
        } else if (this instanceof HealingPowerUp) {
            powerUp.setCustomName("§a§lHEALING");
            powerUp.setHelmet(new ItemStack(Material.WOOL, 1, (short) 5));
        } else if (this instanceof SpeedPowerUp) {
            powerUp.setCustomName("§b§lSPEED");
            powerUp.setHelmet(new ItemStack(Material.WOOL, 1, (short) 4));
        }
        powerUp.setGravity(false);
        powerUp.setVisible(false);
        powerUp.setCustomNameVisible(true);

        for (Player player1 : powerUp.getWorld().getPlayers()) {
            player1.playSound(powerUp.getLocation(), "ctf.powerup.spawn", 2, 1);
        }

        //TODO packets - energy vs damage display differently for every player
//        WorldServer s = ((CraftWorld) location.getWorld()).getHandle();
//        powerUp = new EntityArmorStand(s);
//
//        powerUp.setLocation(location.getX(), location.getY() - 1.5, location.getZ(), 0, 0);
//        powerUp.setCustomNameVisible(true);
//        powerUp.setGravity(false);
//        powerUp.setInvisible(true);
//
//        for (WarlordsPlayer value : Warlords.getPlayers().values()) {
//            if(value.isEnergyPowerup()) {
//                powerUp.setCustomName("ENERGY");
//                PacketPlayOutSpawnEntityLiving armorStand = new PacketPlayOutSpawnEntityLiving(powerUp);
//                PacketPlayOutEntityEquipment wool = new PacketPlayOutEntityEquipment(powerUp.getId(), 4, CraftItemStack.asNMSCopy(new ItemStack(Material.WOOL, 1, (short) 4)));
//
//                ((CraftPlayer) value.getPlayer()).getHandle().playerConnection.sendPacket(armorStand);
//                ((CraftPlayer) value.getPlayer()).getHandle().playerConnection.sendPacket(wool);
//
//            } else {
//                powerUp.setCustomName("DAMAGE");
//
//                PacketPlayOutSpawnEntityLiving armorStand = new PacketPlayOutSpawnEntityLiving(powerUp);
//                PacketPlayOutEntityEquipment wool = new PacketPlayOutEntityEquipment(powerUp.getId(), 4, CraftItemStack.asNMSCopy(new ItemStack(Material.WOOL, 1, (short) 14)));
//
//                ((CraftPlayer) value.getPlayer()).getHandle().playerConnection.sendPacket(armorStand);
//                ((CraftPlayer) value.getPlayer()).getHandle().playerConnection.sendPacket(wool);
//
//            }
//        }

    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArmorStand getPowerUp() {
        return powerUp;
    }

    public void setPowerUp(ArmorStand powerUp) {
        this.powerUp = powerUp;
    }

//    public EntityArmorStand getPowerUp() {
//        return powerUp;
//    }
//
//    public void setPowerUp(EntityArmorStand powerUp) {
//        this.powerUp = powerUp;
//    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getMaxCooldown() {
        return maxCooldown;
    }

    public void setMaxCooldown(int maxCooldown) {
        this.maxCooldown = maxCooldown;
    }
}