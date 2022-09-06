package com.ebicep.warlords.guilds.upgrades;

import com.ebicep.warlords.events.player.WarlordsPlayerGiveExperienceEvent;
import com.ebicep.warlords.events.player.pve.WarlordsPlayerAddCurrencyEvent;
import com.ebicep.warlords.events.player.pve.WarlordsPlayerDropWeaponEvent;
import com.ebicep.warlords.events.player.pve.WarlordsPlayerGiveRespawnEvent;
import com.ebicep.warlords.game.Game;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.UnaryOperator;

public enum GuildUpgrades {

    //TEMPORARY UPGRADES
    COINS_BOOST(
            "Coins Boost",
            Material.GOLD_INGOT,
            false,
            start -> start.plus(24, ChronoUnit.HOURS)
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return tier == 9 ? 4 : 1 + .25 * tier;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {

        }
    },
    INSIGNIA_BOOST(
            "Insignia Boost",
            Material.NETHER_STAR,
            false,
            start -> start.plus(24, ChronoUnit.HOURS)
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return 1 + .05 * tier;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {
            game.registerEvents(new Listener() {

                @EventHandler
                public void onEvent(WarlordsPlayerAddCurrencyEvent event) {
                    if (!validUUIDs.contains(event.getPlayer().getUuid())) {
                        return;
                    }
                    event.getCurrencyToAdd().set((int) (event.getCurrencyToAdd().get() * getValueFromTier(tier)));
                }

            });
        }
    },
    RESPAWN_TIME_REDUCTION(
            "Respawn Time Reduction",
            Material.WATCH,
            false,
            start -> start.plus(24, ChronoUnit.HOURS)
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return tier == 9 ? -10 : -tier;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {
            game.registerEvents(new Listener() {

                @EventHandler
                public void onEvent(WarlordsPlayerGiveRespawnEvent event) {
                    if (!validUUIDs.contains(event.getPlayer().getUuid())) {
                        return;
                    }
                    event.getRespawnTimer().set((int) (event.getRespawnTimer().get() + getValueFromTier(tier)));
                }

            });
        }
    },
    WEAPON_DROP_RATE(
            "Weapon Drop Rate",
            Material.WOOD_AXE,
            false,
            start -> start.plus(24, ChronoUnit.HOURS)
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return 1 + (tier == 9 ? 100 : 10 * tier) * .01;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {
            game.registerEvents(new Listener() {

                @EventHandler
                public void onEvent(WarlordsPlayerDropWeaponEvent event) {
                    if (!validUUIDs.contains(event.getPlayer().getUuid())) {
                        return;
                    }
                    event.getDropRate().set(event.getDropRate().get() * getValueFromTier(tier));
                }

            });
        }
    },

    //PERMANENT UPGRADES
    PLAYER_EXP_BONUS(
            "Player EXP Bonus",
            Material.EXP_BOTTLE,
            true,
            null
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return 1 + .05 * tier;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {
            game.registerEvents(new Listener() {

                @EventHandler
                public void onEvent(WarlordsPlayerGiveExperienceEvent event) {
                    if (!validUUIDs.contains(event.getPlayer().getUuid())) {
                        return;
                    }
                    event.getExperienceSummary().replaceAll((key, value) -> (long) (value * getValueFromTier(tier)));
                }

            });
        }
    },
    GUILD_COIN_CONVERSION_RATE(
            "Guild Coin Conversion Rate",
            Material.GOLD_NUGGET,
            true,
            null
    ) {
        @Override
        public double getValueFromTier(int tier) {
            return (tier == 9 ? 5 : 2 + .25 * tier) * .01;
        }

        @Override
        public void onGame(Game game, HashSet<UUID> validUUIDs, int tier) {

        }
    },

    ;

    public static final GuildUpgrades[] VALUES = values();
    public final String name;
    public final Material material;
    public final boolean isPermanent;
    public final UnaryOperator<Instant> expirationDate;

    GuildUpgrades(String name, Material material, boolean isPermanent, UnaryOperator<Instant> expirationDate) {
        this.name = name;
        this.material = material;
        this.isPermanent = isPermanent;
        this.expirationDate = expirationDate;
    }

    public abstract double getValueFromTier(int tier);

    /**
     * @param game       the game to modify - main purpose is adding listeners
     * @param validUUIDs uuids that are allowed to get the effect of this upgrade (since listener will affect all players we need to filter out players which guilds have this upgrade)
     * @param tier
     */
    public abstract void onGame(Game game, HashSet<UUID> validUUIDs, int tier);

    public GuildUpgrade createUpgrade(int tier) {
        return new GuildUpgrade(this, tier);
    }

    public long getCost(int tier) {
        if (isPermanent) {
            switch (tier) {
                case 1:
                    return 250000;
                case 2:
                    return 500000;
                case 3:
                    return 1000000;
                case 4:
                    return 2000000;
                case 5:
                    return 3000000;
                case 6:
                    return 4500000;
                case 7:
                    return 6750000;
                case 8:
                    return 10125000;
                case 9:
                    return 15187500;
                default:
                    return Long.MAX_VALUE;
            }
        } else {
            switch (tier) {
                case 1:
                    return 10000;
                case 2:
                    return 20000;
                case 3:
                    return 40000;
                case 4:
                    return 80000;
                case 5:
                    return 120000;
                case 6:
                    return 180000;
                case 7:
                    return 270000;
                case 8:
                    return 405000;
                case 9:
                    return 607500;
                default:
                    return Long.MAX_VALUE;
            }
        }
    }

}