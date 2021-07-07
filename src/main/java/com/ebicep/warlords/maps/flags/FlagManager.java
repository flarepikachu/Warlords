package com.ebicep.warlords.maps.flags;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.events.WarlordsDeathEvent;
import com.ebicep.warlords.maps.Team;
import com.ebicep.warlords.maps.state.PlayingState;
import com.ebicep.warlords.player.WarlordsPlayer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlagManager implements Listener {

    private final FlagInfo red;
    private final FlagInfo blue;

    private final FlagRenderer redRenderer;
    private final FlagRenderer blueRenderer;

    private final BukkitTask task;
    final PlayingState gameState;

    @Nonnull
    public FlagInfo getRed() {
        return red;
    }

    @Nonnull
    public FlagInfo getBlue() {
        return blue;
    }

    @Nonnull
    public FlagInfo get(@Nonnull Team team) {
        return team == Team.RED ? this.red : this.blue;
    }

    public FlagManager(PlayingState gameState, Location redFlagRespawn, Location blueFlagRespawn) {
        this.red = new FlagInfo(Team.RED, redFlagRespawn, this);
        this.blue = new FlagInfo(Team.BLUE, blueFlagRespawn, this);

        this.redRenderer = new FlagRenderer(red);
        this.blueRenderer = new FlagRenderer(blue);

        final Warlords plugin = Warlords.getInstance();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1, 1);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.gameState = gameState;
    }

    public void checkScore(Team team) {
        if (
                this.get(team).getFlag() instanceof SpawnFlagLocation &&
                        this.get(team.enemy()).getFlag() instanceof PlayerFlagLocation &&
                        this.get(team.enemy()).getFlag().getLocation().distanceSquared(this.get(team).getSpawnLocation()) < 2 * 2
        ) {
            for (Team t : Team.values()) {
                FlagInfo info = get(t);
                info.setFlag(new WaitingFlagLocation(info.getSpawnLocation(), t != team));
            }
        }
    }

    public void tick() {
        checkScore(Team.RED);
        checkScore(Team.BLUE);

        this.red.update();
        this.blue.update();
        this.redRenderer.checkRender();
        this.blueRenderer.checkRender();

    }

    @EventHandler
    public void onPlayerDeath(WarlordsDeathEvent event) {
        dropFlag(event.getPlayer());
    }

    public boolean dropFlag(Player player) {
        return dropFlag(Warlords.getPlayer(player));
    }

    public boolean dropFlag(@Nullable WarlordsPlayer player) {
        if (player == null) {
            return false;
        }
        FlagInfo info = get(player.getTeam().enemy());
        if (info.getFlag() instanceof PlayerFlagLocation) {
            PlayerFlagLocation playerFlagLocation = (PlayerFlagLocation) info.getFlag();
            if (playerFlagLocation.getPlayer() == player) {
                info.setFlag(new GroundFlagLocation(player.getLocation(), playerFlagLocation.getPickUpTicks()));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        dropFlag(event.getPlayer());
    }

    @EventHandler
    public void onArmorStandBreak(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();

        if (entity instanceof ArmorStand && entity.getCustomName() != null && entity.getCustomName().contains("FLAG")) {
            event.setCancelled(true);
            Team standTeam = (Team) entity.getMetadata("TEAM").stream().map(MetadataValue::value).filter(v -> v instanceof Team).findAny().orElse(null);
            if (standTeam == null) {
                return;
            }
            Player player = (Player) event.getDamager();
            WarlordsPlayer wp = Warlords.getPlayer(player);
            if (wp == null) {
                return;
            }
            FlagInfo info = get(standTeam);
            Team team = wp.getTeam();

            if (info.getFlag() instanceof GroundFlagLocation) {
                GroundFlagLocation groundFlagLocation = (GroundFlagLocation) info.getFlag();
                if (team == info.getTeam()) {
                    // Return flag
                    info.setFlag(new SpawnFlagLocation(info.getSpawnLocation(), wp.getName()));
                } else {
                    // Steal flag
                    info.setFlag(new PlayerFlagLocation(wp, groundFlagLocation.getDamageTimer()));
                    if (player.getVehicle() != null) {
                        player.getVehicle().remove();
                    }
                }
            } else if (info.getFlag() instanceof SpawnFlagLocation) {
                if (team == info.getTeam()) {
                    // Nothing
                    player.sendMessage("§cYou can't steal your own team's flag!");
                } else {
                    // Steal flag
                    info.setFlag(new PlayerFlagLocation(wp, 0));
                }
            }
        }
    }

    public void stop() {
        this.blueRenderer.reset();
        this.redRenderer.reset();
        this.task.cancel();
        HandlerList.unregisterAll(this);
    }


}