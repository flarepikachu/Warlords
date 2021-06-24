package com.ebicep.warlords.util;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.WarlordsPlayer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.bukkit.entity.LivingEntity;

public class Utils {

    public static boolean getLookingAt(LivingEntity player, LivingEntity player1) {
        Location eye = player.getEyeLocation().subtract(player.getLocation().getDirection().multiply(4));
        eye.setY(eye.getY() + 0.7);
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        float dot = (float) toEntity.normalize().dot(eye.getDirection());
        return dot > 0.975D;
    }

    //15 blocks = 6.6
    //10 blocks = 8
    //7 blocks = 10
    //5 blocks = 28
    //2 blocks = WIDE
    public static boolean getLookingAtChain(LivingEntity player, LivingEntity player1) {
        Location eye = player.getEyeLocation().subtract(player.getLocation().getDirection().multiply(4));
        eye.setY(eye.getY() + 0.7);
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        float dot = (float) toEntity.normalize().dot(eye.getDirection());
        return dot > 0.965D + (player.getLocation().distanceSquared(player1.getLocation()) / 10000);
    }

    public static boolean getLookingAtWave(LivingEntity player, LivingEntity player1) {
        Location eye = player.getEyeLocation();
        eye.setY(eye.getY() + 0.7);
        eye.setPitch(0);
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        float dot = (float) toEntity.normalize().dot(eye.getDirection());
        return dot > 0.935D;
    }

    public static boolean hasLineOfSight(LivingEntity player, LivingEntity player2) {
        return player.hasLineOfSight(player2);
    }

    @Nullable
    public static ArmorStand getTotemDownAndClose(WarlordsPlayer warlordsPlayer, Entity searchNearby) {
        for (Entity entity : searchNearby.getNearbyEntities(5, 3, 5)) {
            if (entity instanceof ArmorStand && entity.hasMetadata("Capacitor Totem - " + warlordsPlayer.getName())) {
                return (ArmorStand)entity;
            }
        }
        return null;
    }

    public static boolean totemDownAndClose(WarlordsPlayer warlordsPlayer, Entity searchNearby) {
        for (Entity entity : searchNearby.getNearbyEntities(5, 3, 5)) {
            if (entity instanceof ArmorStand && entity.hasMetadata("Capacitor Totem - " + warlordsPlayer.getName())) {
                return true;
            }
        }
        return false;
    }

    public static class ArmorStandComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity a, Entity b) {
            return a instanceof ArmorStand && b instanceof ArmorStand ? 0 : a instanceof ArmorStand ? -1 : b instanceof ArmorStand ? 1 : 0;
        }
    }

    private static final Location LOCATION_CONTAINER = new Location(null, 0, 0, 0);
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemiesSorted(WarlordsPlayer entity, double x, double y, double z, Entity player) {
        return filterOnlyEnemiesSorted(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemiesSorted(Entity entity, double x, double y, double z, Entity player) {
        return filterOnlyEnemiesSorted(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemiesSorted(Location loc, double x, double y, double z, Entity player) {
        return filterOnlyEnemies(loc.getWorld().getNearbyEntities(loc, x, y, z), player)
            .sorted(sortClosestBy(WarlordsPlayer::getLocation, loc));
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemies(Entity entity, double x, double y, double z, Entity player) {
        return filterOnlyEnemies(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemies(Location loc, double x, double y, double z, Entity player) {
        return filterOnlyEnemies(loc.getWorld().getNearbyEntities(loc, x, y, z), player)
            .filter(radiusAround(WarlordsPlayer::getLocation, loc, x, y, z));
    }
    /**
     * Map the list of entities to valid WarlordPlayer who are part off the enemy of the team of the player
     * @param entities
     * @param player
     * @return
     */
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyEnemies(Collection<Entity> entities, Entity player) {
        WarlordsPlayer wp = Warlords.getPlayer(player);
        return wp == null ? Stream.empty() : entities.stream()
            .map(e -> Warlords.getPlayer(e))
            .filter(filterOnlyEnemies(wp));
    }
    public static Predicate<WarlordsPlayer> filterOnlyEnemies(@Nullable WarlordsPlayer wp) {
        return wp == null ? (player) -> false : wp::isEnemy;
    }


    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammatesSorted(WarlordsPlayer entity, double x, double y, double z, Entity player) {
        return filterOnlyTeammatesSorted(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammatesSorted(Entity entity, double x, double y, double z, Entity player) {
        return filterOnlyTeammatesSorted(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammatesSorted(Location loc, double x, double y, double z, Entity player) {
        return filterOnlyTeammates(loc.getWorld().getNearbyEntities(loc, x, y, z), player)
            .sorted(sortClosestBy(WarlordsPlayer::getLocation, loc));
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammates(Entity entity, double x, double y, double z, Entity player) {
        return filterOnlyTeammates(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammates(WarlordsPlayer entity, double x, double y, double z, Entity player) {
        return filterOnlyTeammates(entity.getLocation(LOCATION_CONTAINER), x, y, z, player);
    }
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammates(Location loc, double x, double y, double z, Entity player) {
        return filterOnlyTeammates(loc.getWorld().getNearbyEntities(loc, x, y, z), player)
            .filter(radiusAround(WarlordsPlayer::getLocation, loc, x, y, z));
    }
    /**
     * Map the list of entities to valid WarlordPlayer who are part on the team of the player
     * @param entities
     * @param player
     * @return
     */
    @Deprecated
    public static Stream<WarlordsPlayer> filterOnlyTeammates(Collection<Entity> entities, Entity player) {
        WarlordsPlayer wp = Warlords.getPlayer(player);
        return wp == null ? Stream.empty() : entities.stream()
            .map(e -> Warlords.getPlayer(e))
            .filter(filterOnlyTeammates(wp));
    }
    public static Predicate<WarlordsPlayer> filterOnlyTeammates(@Nullable WarlordsPlayer wp) {
        return wp == null ? (player) -> false : wp::isTeammate;
    }

    // Old methods:
    @Deprecated
    public static List<Entity> filterOutTeammates(Collection<Entity> entities, Player player) {
        WarlordsPlayer wp = Warlords.getPlayer(player);
        return wp == null ? Collections.emptyList() : entities.stream()
            .filter(e -> wp.isEnemy(e))
            .collect(Collectors.toList());
    }
    @Deprecated
    public static List<Entity> filterTeammates(Collection<Entity> entities, Player player) {
        WarlordsPlayer wp = Warlords.getPlayer(player);
        return wp == null ? Collections.emptyList() : entities.stream()
            .filter(e -> wp.isTeammate(e))
            .collect(Collectors.toList());
    }

    private static final Location LOCATION_CACHE_SORT = new Location(null, 0, 0, 0);
    public static Comparator<Entity> sortClosestBy(Location loc) {
        return sortClosestBy(Entity::getLocation, loc);
    }
    public static <T> Comparator<T> sortClosestBy(BiConsumer<T, Location> map, Location loc) {
        return Comparator.comparing(e -> {
            map.accept(e, LOCATION_CACHE_SORT);
            return LOCATION_CACHE_SORT.distanceSquared(loc);
        });
    }

    private static final Location LOCATION_CACHE_DISTANCE = new Location(null, 0, 0, 0);
    public static Predicate<Entity> radiusAround(Location loc, double radius) {
        return radiusAround(loc, radius, radius, radius);
    }
    public static Predicate<Entity> radiusAround(Location loc, double x, double y, double z) {
        return radiusAround(Entity::getLocation, loc, x, y, z);
    }
    public static <T> Predicate<T> radiusAround(BiConsumer<T, Location> map, Location loc, double radius) {
        return radiusAround(map, loc, radius, radius, radius);
    }
    public static <T> Predicate<T> radiusAround(BiConsumer<T, Location> map, Location loc, double x, double y, double z) {
        return entity -> {
            map.accept(entity, LOCATION_CACHE_DISTANCE);
            double xDif = (loc.getX() - LOCATION_CACHE_DISTANCE.getX()) * 2 / x;
            double yDif = (loc.getY() - LOCATION_CACHE_DISTANCE.getY()) * 2 / y;
            double zDif = (loc.getZ() - LOCATION_CACHE_DISTANCE.getZ()) * 2 / z;
            return xDif * xDif + yDif * yDif + zDif * zDif <= 1;
        };
    }

    public static Vector getRightDirection(Location location) {
        Vector direction = location.getDirection().normalize();
        return new Vector(-direction.getZ(), 0.0, direction.getX()).normalize();
    }

    public static Vector getLeftDirection(Location location) {
        Vector direction = location.getDirection().normalize();
        return new Vector(direction.getZ(), 0.0, -direction.getX()).normalize();
    }

    public static double getDistance(WarlordsPlayer e, double accuracy) {
        return getDistance(e.getLocation(), accuracy);
    }
    public static double getDistance(Entity e, double accuracy) {
        return getDistance(e.getLocation(), accuracy);
    }
    public static double getDistance(Location original, double accuracy) {
        Location loc = original.clone(); // Using .clone so you aren't messing with the direct location object from the entity
        double distance = 0; // Shouldn't start at -2 unless you're wanting the eye height from the ground (I don't know why you'd want that)
        for (double i = loc.getY(); i >= original.getY() - 2; i -= accuracy) {
            loc.setY(i);
            distance += accuracy;
            if (loc.getBlock().getType().isSolid()) // Makes a little more sense than checking if it's air
                break;
        }
        return distance;
    }

    public static boolean tunnelUnder(Player p) {
        Location location = p.getLocation().clone();
        for (int i = 0; i < 15; i++) {
            location.add(0, -1, 0);
            p.sendMessage("" + p.getWorld().getBlockAt(location).getType());
            if (p.getWorld().getBlockAt(location).getType() == Material.AIR) {
                return true;
            }
        }
        return false;
    }

    private final static int CENTER_PX = 154;


    public static void sendMessage(Player player, boolean centered, String message) {
        if(centered) {
            Utils.sendCenteredMessage(player, message);
        } else {
            player.sendMessage(message);
        }
    }
    public static void sendCenteredMessage(Player player, String message) {
        if (message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
    }

    public static void sendCenteredHoverableMessage(Player player, List<TextComponent> textComponents) {
        if (textComponents == null || textComponents.size() == 0) ;
        String message = "";
        for (TextComponent textComponent : textComponents) {
            message += textComponent.getText();
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        ComponentBuilder componentBuilder = new ComponentBuilder(sb.toString());
        for (TextComponent textComponent : textComponents) {
            componentBuilder.append(textComponent.getText());
            componentBuilder.event(textComponent.getHoverEvent());
        }
        player.spigot().sendMessage(componentBuilder.create());
    }

    public static String addCommaAndRound(float amount) {
        amount = Math.round(amount);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String output = formatter.format(amount);
        return output;
    }

    /**
     * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string
     * for sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
     *
     * @param itemStack the item to convert
     * @return the Json string representation of the item
     */
    public static String convertItemStackToJsonRegular(ItemStack itemStack) {
        // First we convert the item stack into an NMS itemstack
        net.minecraft.server.v1_8_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.server.v1_8_R3.NBTTagCompound compound = new NBTTagCompound();
        nmsItemStack.save(compound);
        return compound.toString();
    }
}
