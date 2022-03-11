package com.ebicep.warlords.menu.debugmenu;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.flags.GroundFlagLocation;
import com.ebicep.warlords.game.flags.PlayerFlagLocation;
import com.ebicep.warlords.game.flags.SpawnFlagLocation;
import com.ebicep.warlords.game.option.marker.DebugLocationMarker;
import com.ebicep.warlords.game.option.marker.FlagHolder;
import com.ebicep.warlords.game.option.marker.LobbyLocationMarker;
import com.ebicep.warlords.game.option.marker.MapSymmetryMarker;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.menu.StatusEffectCooldowns;
import com.ebicep.warlords.player.*;
import com.ebicep.warlords.player.cooldowns.AbstractCooldown;
import com.ebicep.warlords.util.ItemBuilder;
import com.ebicep.warlords.util.NumberFormat;
import com.ebicep.warlords.util.PlayerFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ebicep.warlords.menu.Menu.*;
import static com.ebicep.warlords.player.Classes.setSelectedBoost;
import static com.ebicep.warlords.util.Utils.woolSortedByColor;

public class DebugMenuPlayerOptions {
    public static void openPlayerMenu(Player player, WarlordsPlayer target) {
        if (target == null) return;
        String targetName = target.getName();
        Menu menu = new Menu("Player Options: " + targetName, 9 * 5);
        ItemStack[] firstRow = {
                new ItemBuilder(Material.EXP_BOTTLE)
                        .name(ChatColor.GREEN + "Energy")
                        .get(),
                new ItemBuilder(Material.INK_SACK, 1, (byte) 8)
                        .name(ChatColor.GREEN + "Cooldown")
                        .get(),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                        .name(ChatColor.GREEN + "Damage")
                        .get(),
                new ItemBuilder(Material.RABBIT_FOOT)
                        .name(ChatColor.GREEN + "Crits")
                        .get(),
                new ItemBuilder(Material.AIR)
                        .get(),
                new ItemBuilder(new Potion(PotionType.INSTANT_DAMAGE), 1, true)
                        .name(ChatColor.GREEN + "Kill")
                        .flags(ItemFlag.HIDE_POTION_EFFECTS)
                        .get(),
                new ItemBuilder(Material.WOOL, 1, (short) (Warlords.getPlayerSettings(player.getUniqueId()).getWantedTeam() == Team.BLUE ? 14 : 11))
                        .name(ChatColor.GREEN + "Swap to the " + (Warlords.getPlayerSettings(player.getUniqueId()).getWantedTeam() == Team.BLUE ? Team.RED.coloredPrefix() : Team.BLUE.coloredPrefix()) + ChatColor.GREEN + " team")
                        .get(),
        };
        ItemStack[] secondRow = {
                new ItemBuilder(Material.SUGAR)
                        .name(ChatColor.GREEN + "Modify Speed")
                        .get(),
                new ItemBuilder(new Potion(PotionType.INSTANT_HEAL), 1, true)
                        .name(ChatColor.GREEN + "Add Health")
                        .flags(ItemFlag.HIDE_POTION_EFFECTS)
                        .get(),
                new ItemBuilder(Material.DIAMOND_SWORD)
                        .name(ChatColor.GREEN + "Take Damage")
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .get(),
                new ItemBuilder(Material.BREWING_STAND_ITEM)
                        .name(ChatColor.GREEN + "Cooldowns")
                        .get(),
                new ItemBuilder(Material.EYE_OF_ENDER)
                        .name(ChatColor.GREEN + "Teleport To")
                        .get(),
                new ItemBuilder(Material.BANNER)
                        .name(ChatColor.GREEN + "Flag Options")
                        .get(),
                new ItemBuilder(Material.NETHER_STAR)
                        .name(ChatColor.GREEN + "Change Spec")
                        .get(),
        };
        for (int i = 0; i < firstRow.length; i++) {
            int index = i + 1;
            menu.setItem(index, 1, firstRow[i],
                    (n, e) -> {
                        switch (index) {
                            case 1:
                                Bukkit.getServer().dispatchCommand(player, "wl energy " + (target.isInfiniteEnergy() ? "enable" : "disable") + " " + targetName);
                                break;
                            case 2:
                                Bukkit.getServer().dispatchCommand(player, "wl cooldown " + (target.isDisableCooldowns() ? "enable" : "disable") + " " + targetName);
                                break;
                            case 3:
                                Bukkit.getServer().dispatchCommand(player, "wl damage " + (target.isTakeDamage() ? "disable" : "enable") + " " + targetName);
                                break;
                            case 4:
                                Bukkit.getServer().dispatchCommand(player, "wl crits " + (target.isCanCrit() ? "disable" : "enable") + " " + targetName);
                                break;
                            case 6:
                                Bukkit.getServer().dispatchCommand(player, "kill " + targetName);
                                break;
                            case 7:
                                Game game = target.getGame();
                                Team currentTeam = target.getTeam();
                                Team otherTeam = target.getTeam().enemy();
                                game.setPlayerTeam(player, otherTeam);
                                target.setTeam(otherTeam);

                                target.getGameState().updatePlayerName(target);
                                Warlords.getPlayerSettings(target.getUuid()).setWantedTeam(otherTeam);
                                LobbyLocationMarker randomLobbyLocation = LobbyLocationMarker.getRandomLobbyLocation(game, otherTeam);
                                if (randomLobbyLocation != null) {
                                    Location teleportDestination = MapSymmetryMarker.getSymmetry(game)
                                            .getOppositeLocation(game, currentTeam, otherTeam, target.getLocation(), randomLobbyLocation.getLocation());
                                    target.teleport(teleportDestination);
                                }
                                ArmorManager.resetArmor(Bukkit.getPlayer(target.getUuid()), Warlords.getPlayerSettings(target.getUuid()).getSelectedClass(), otherTeam);
                                player.sendMessage(ChatColor.RED + "DEV: " + currentTeam.teamColor() + target.getName() + "§a was swapped to the " + otherTeam.coloredPrefix() + " §ateam");
                                openPlayerMenu(player, target);
                                break;

                        }
                    }
            );
        }
        for (int i = 0; i < secondRow.length; i++) {
            int index = i + 1;
            menu.setItem(index, 2, secondRow[i],
                    (n, e) -> {
                        switch (index) {
                            case 1:
                                //TODO
                                break;
                            case 2:
                                openAmountMenu(player, target, "heal");
                                break;
                            case 3:
                                openAmountMenu(player, target, "takedamage");
                                break;
                            case 4:
                                openCooldownsMenu(player, target);
                                break;
                            case 5:
                                openTeleportLocations(player, target);
                                break;
                            case 6:
                                openFlagOptionMenu(player, target);
                                break;
                            case 7:
                                openSpecMenu(player, target);
                                break;
                        }
                    }
            );
        }
        menu.setItem(3, 4, MENU_BACK, (n, e) -> {
            if (player.getUniqueId() == target.getUuid()) {
                DebugMenu.openDebugMenu(player);
            } else {
                openTeamMenu(player);
            }
        });
        menu.setItem(4, 4, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openTeamMenu(Player player) {
        WarlordsPlayer warlordsPlayer = Warlords.getPlayer(player);
        if (warlordsPlayer == null) return;

        Menu menu = new Menu("Team Options", 9 * 6);
        //divider
        for (int i = 0; i < 5; i++) {
            menu.setItem(4, i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).name(" ").get(), (n, e) -> {
            });
        }
        //team info = color - other shit
        List<WarlordsPlayer> bluePlayers = new ArrayList<>();
        List<WarlordsPlayer> redPlayers = new ArrayList<>();
        PlayerFilter.playingGame(warlordsPlayer.getGame()).forEach((wp) -> {
            if (wp.getTeam() == Team.BLUE) {
                bluePlayers.add(wp);
            } else if (wp.getTeam() == Team.RED) {
                redPlayers.add(wp);
            }
        });
        ItemStack blueInfo = new ItemBuilder(Material.WOOL, 1, (byte) 11)
                .name(ChatColor.BLUE + "BLU")
                .lore(getTeamStatLore(bluePlayers))
                .get();
        ItemStack redInfo = new ItemBuilder(Material.WOOL, 1, (byte) 14)
                .name(ChatColor.RED + "RED")
                .lore(getTeamStatLore(redPlayers))
                .get();
        ItemStack killTeam = new ItemBuilder(Material.DIAMOND_SWORD)
                .name(ChatColor.RED + "Kill All")
                .lore(ChatColor.GRAY + "Kills all the players on the team")
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .get();
        menu.setItem(0, 0, blueInfo, (n, e) -> {
        });
        menu.setItem(3, 0, killTeam, (n, e) -> {
            bluePlayers.forEach(wp -> wp.addDamageInstance(wp, "", 69000, 69000, -1, 100, false));
        });
        menu.setItem(5, 0, redInfo, (n, e) -> {
        });
        menu.setItem(8, 0, killTeam, (n, e) -> {
            redPlayers.forEach(wp -> wp.addDamageInstance(wp, "", 69000, 69000, -1, 100, false));
        });

        //players
        addPlayersToMenu(menu, player, bluePlayers, true);
        addPlayersToMenu(menu, player, redPlayers, false);
        menu.setItem(3, 5, MENU_BACK, (n, e) -> DebugMenu.openDebugMenu(player));
        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    private static void addPlayersToMenu(Menu menu, Player player, List<WarlordsPlayer> warlordsPlayers, boolean blueTeam) {
        //flag player first
        warlordsPlayers.sort((wp1, wp2) -> {
            int wp1Flag = wp1.getCarriedFlag() != null ? 1 : 0;
            int wp2Flag = wp2.getCarriedFlag() != null ? 1 : 0;
            return wp2Flag - wp1Flag;
        });
        int y = 0;
        for (int i = 0; i < warlordsPlayers.size(); i++) {
            if (i % 4 == 0) {
                y++;
            }
            WarlordsPlayer wp = warlordsPlayers.get(i);
            List<String> lore = new ArrayList<>(Arrays.asList(getPlayerStatLore(wp)));
            lore.add("");
            if (player.getUniqueId() != wp.getUuid()) {
                lore.add(ChatColor.YELLOW.toString() + ChatColor.BOLD + "RIGHT-CLICK" + ChatColor.GREEN + " to " + ChatColor.YELLOW + "Teleport");
                lore.add(ChatColor.YELLOW.toString() + ChatColor.BOLD + "LEFT-CLICK" + ChatColor.GREEN + " to " + ChatColor.YELLOW + "Open Player Options");
            } else {
                lore.add(ChatColor.YELLOW.toString() + ChatColor.BOLD + "CLICK" + ChatColor.GREEN + " to " + ChatColor.YELLOW + "Open Player Options");
            }
            menu.setItem(i % 4 + (blueTeam ? 0 : 5), y,
                    new ItemBuilder(Warlords.getHead(wp.getUuid()))
                            .name((blueTeam ? ChatColor.BLUE : ChatColor.RED) + wp.getName() + (wp.getCarriedFlag() != null ? ChatColor.WHITE + " ⚑" : ""))
                            .lore(lore)
                            .get(),
                    (n, e) -> {
                        if (e.isRightClick() && player.getUniqueId() != wp.getUuid()) {
                            player.teleport(wp.getLocation());
                        } else {
                            openPlayerMenu(player, wp);
                        }
                    }
            );
        }
    }

    private static String[] getTeamStatLore(List<WarlordsPlayer> warlordsPlayers) {
        return new String[]{
                ChatColor.GREEN + "Kills" + ChatColor.GRAY + ": " + ChatColor.GOLD + warlordsPlayers.stream().mapToInt(e -> e.getMinuteStats().total().getKills()).sum(),
                ChatColor.GREEN + "Assists" + ChatColor.GRAY + ": " + ChatColor.GOLD + warlordsPlayers.stream().mapToInt(e -> e.getMinuteStats().total().getAssists()).sum(),
                ChatColor.GREEN + "Deaths" + ChatColor.GRAY + ": " + ChatColor.GOLD + warlordsPlayers.stream().mapToInt(e -> e.getMinuteStats().total().getDeaths()).sum(),
                ChatColor.GREEN + "Damage" + ChatColor.GRAY + ": " + ChatColor.RED + NumberFormat.addCommaAndRound((float) warlordsPlayers.stream().mapToDouble(e -> e.getMinuteStats().total().getDamage()).sum()),
                ChatColor.GREEN + "Healing" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + NumberFormat.addCommaAndRound((float) warlordsPlayers.stream().mapToDouble(e -> e.getMinuteStats().total().getHealing()).sum()),
                ChatColor.GREEN + "Absorbed" + ChatColor.GRAY + ": " + ChatColor.GOLD + NumberFormat.addCommaAndRound((float) warlordsPlayers.stream().mapToDouble(e -> e.getMinuteStats().total().getAbsorbed()).sum())
        };
    }

    private static String[] getPlayerStatLore(WarlordsPlayer wp) {
        return new String[]{
                ChatColor.GREEN + "Spec" + ChatColor.GRAY + ": " + ChatColor.GOLD + wp.getSpec().getClass().getSimpleName(),
                ChatColor.GREEN + "Health" + ChatColor.GRAY + ": " + ChatColor.RED + wp.getHealth(),
                ChatColor.GREEN + "Energy" + ChatColor.GRAY + ": " + ChatColor.YELLOW + (int) wp.getEnergy(),
                ChatColor.GREEN + "Kills" + ChatColor.GRAY + ": " + ChatColor.GOLD + wp.getMinuteStats().total().getKills(),
                ChatColor.GREEN + "Assists" + ChatColor.GRAY + ": " + ChatColor.GOLD + wp.getMinuteStats().total().getAssists(),
                ChatColor.GREEN + "Deaths" + ChatColor.GRAY + ": " + ChatColor.GOLD + wp.getMinuteStats().total().getDeaths(),
                ChatColor.GREEN + "Damage" + ChatColor.GRAY + ": " + ChatColor.RED + NumberFormat.addCommaAndRound(wp.getMinuteStats().total().getDamage()),
                ChatColor.GREEN + "Healing" + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + NumberFormat.addCommaAndRound(wp.getMinuteStats().total().getHealing()),
                ChatColor.GREEN + "Absorbed" + ChatColor.GRAY + ": " + ChatColor.GOLD + NumberFormat.addCommaAndRound(wp.getMinuteStats().total().getAbsorbed())
        };
    }

    public static void openAmountMenu(Player player, WarlordsPlayer target, String commandType) {
        String targetName = target != null ? target.getName() : "";
        String commandName = commandType.equals("heal") ? "Give Health" : "Take Damage";
        Menu menu = new Menu(commandName + ": " + (target != null ? targetName : player.getName()), 9 * 4);
        for (int i = 1; i <= 5; i++) {
            int amount = i * 1000;
            menu.setItem(i + 1, 1,
                    new ItemBuilder(woolSortedByColor[i - 1])
                            .name((commandType.equals("takedamage") ? ChatColor.RED.toString() : ChatColor.GREEN.toString()) + amount)
                            .get(),
                    (n, e) -> Bukkit.getServer().dispatchCommand(player, "wl " + commandType + " " + amount + " " + targetName)
            );
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openPlayerMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openCooldownsMenu(Player player, WarlordsPlayer target) {
        int menuY = Math.min(5 + StatusEffectCooldowns.values().length / 7, 6);
        Menu menu = new Menu("Cooldowns: " + target.getName(), 9 * menuY);
        //general options
        ItemStack[] generalOptionItems = {
                new ItemBuilder(Material.BEACON)
                        .name(ChatColor.AQUA + "Manage Cooldowns")
                        .get(),
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.AQUA + "Clear All Cooldowns")
                        .get(),
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.AQUA + "Clear All Buffs")
                        .get(),
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.AQUA + "Clear All Debuffs")
                        .get(),
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.AQUA + "Clear All Abilities")
                        .get(),
        };
        for (int i = 0; i < generalOptionItems.length; i++) {
            int finalI = i;
            menu.setItem(i + 1, 1, generalOptionItems[i], (n, e) -> {
                switch (finalI) {
                    case 0:
                        openCooldownManagerMenu(player, target);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.getOpenInventory().getTopInventory().getName().equals("Cooldown Manager: " + target.getName())) {
                                    openCooldownManagerMenu(player, target);
                                } else {
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(Warlords.getInstance(), 20, 20);
                        break;
                    case 1:
                        target.getCooldownManager().clearCooldowns();
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aCooldowns were cleared");
                        break;
                    case 2:
                        target.getCooldownManager().removeBuffCooldowns();
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aBuffs were cleared");
                        break;
                    case 3:
                        target.getCooldownManager().removeDebuffCooldowns();
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aDebuffs were cleared");
                        break;
                    case 4:
                        target.getCooldownManager().removeAbilityCooldowns();
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aAbility Cooldowns were cleared");
                        break;
                }
            });
        }

        //effects
        int yLevel = 1;
        for (int i = 0; i < StatusEffectCooldowns.values().length; i++) {
            if (i % 7 == 0) {
                yLevel++;
            }
            StatusEffectCooldowns cooldown = StatusEffectCooldowns.values()[i];
            menu.setItem((i % 7) + 1, yLevel,
                    new ItemBuilder(cooldown.itemStack)
                            .name(cooldown.color + cooldown.name)
                            .flags(ItemFlag.HIDE_ATTRIBUTES)
                            .get(),
                    (n, e) -> openStatusEffectTimeMenu(player, target, cooldown));
        }
        menu.setItem(3, menuY - 1, MENU_BACK, (n, e) -> openPlayerMenu(player, target));
        menu.setItem(4, menuY - 1, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openCooldownManagerMenu(Player player, WarlordsPlayer target) {
        //int menuY = Math.min(4 + target.getCooldownManager().getCooldowns().size() / 7, 6); Menu shift annoying
        Menu menu = new Menu("Cooldown Manager: " + target.getName(), 9 * 6);
        //general info
        menu.setItem(4, 0,
                new ItemBuilder(Warlords.getHead(player))
                        .name(ChatColor.GREEN + "Cooldown Stats")
                        .lore(ChatColor.GREEN + "Total Cooldowns: " + target.getCooldownManager().getTotalCooldowns(),
                                ChatColor.GREEN + "Active Cooldowns: " + target.getCooldownManager().getCooldowns().size()
                        )
                        .get(),
                (n, e) -> {

                }
        );
        //cooldowns
        int yLevel = 0;
//        List<AbstractCooldown> abstractCooldowns = new ArrayList<>(target.getCooldownManager().getCooldowns());
//        abstractCooldowns.sort(Comparator.comparing(AbstractCooldown::getTimeLeft));
//        for (int i = 0; i < abstractCooldowns.size(); i++) {
//            if (i % 7 == 0) {
//                yLevel++;
//                if (yLevel > 4) break;
//            }
//            AbstractCooldown abstractCooldown = abstractCooldowns.get(i);
//            menu.setItem((i % 7) + 1, yLevel,
//                    new ItemBuilder(woolSortedByColor[i % woolSortedByColor.length])
//                            .name(ChatColor.GOLD + abstractCooldown.getName())
//                            .lore(ChatColor.GREEN + "Time Left: " + ChatColor.GOLD + (Math.round(abstractCooldown.getTimeLeft() * 10) / 10.0) + "s",
//                                    ChatColor.GREEN + "From: " + abstractCooldown.getFrom().getColoredName()
//                            )
//                            .get(),
//                    (n, e) -> openCooldownMenu(player, target, abstractCooldown)
//            );
//        }
        menu.setItem(3, 5, MENU_BACK, (n, e) -> openCooldownsMenu(player, target));
        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openCooldownMenu(Player player, WarlordsPlayer target, AbstractCooldown abstractCooldown) {
        Menu menu = new Menu(abstractCooldown.getName() + ": " + target.getName(), 9 * 4);
        ItemStack[] cooldownOptions = {
                new ItemBuilder(Material.MILK_BUCKET)
                        .name(ChatColor.AQUA + "Remove")
                        .get(),
                new ItemBuilder(Material.REDSTONE)
                        .name(ChatColor.AQUA + "Add duration")
                        .get(),
        };
        for (int i = 0; i < cooldownOptions.length; i++) {
            int finalI = i;
            menu.setItem(i + 1, 1, cooldownOptions[i],
                    (n, e) -> {
                        if (target.getCooldownManager().getCooldowns().contains(abstractCooldown)) {
                            switch (finalI + 1) {
                                case 1:
                                    target.getCooldownManager().getCooldowns().remove(abstractCooldown);
                                    player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §a" + abstractCooldown.getName() + " was removed");
                                    openCooldownManagerMenu(player, target);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (player.getOpenInventory().getTopInventory().getName().equals("Cooldown Manager: " + target.getName())) {
                                                openCooldownManagerMenu(player, target);
                                            } else {
                                                this.cancel();
                                            }
                                        }
                                    }.runTaskTimer(Warlords.getInstance(), 20, 20);
                                    break;
                                case 2:
                                    openCooldownTimerMenu(player, target, abstractCooldown);
                                    break;
                            }
                        } else {
                            openCooldownsMenu(player, target);
                            player.sendMessage(ChatColor.RED + "DEV: §aThat cooldown no longer exists");
                        }
                    }
            );
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openCooldownsMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openCooldownTimerMenu(Player player, WarlordsPlayer target, AbstractCooldown abstractCooldown) {
        Menu menu = new Menu(abstractCooldown.getName() + "Duration: " + target.getName(), 9 * 4);
        int[] durations = {5, 15, 30, 60, 120, 300, 600};
        for (int i = 0; i < durations.length; i++) {
            int finalI = i;
            menu.setItem(i + 1, 1,
                    new ItemBuilder(woolSortedByColor[i + 5])
                            .name(ChatColor.GREEN.toString() + durations[i] + "s")
                            .get(),
                    (n, e) -> {
                        if (target.getCooldownManager().getCooldowns().contains(abstractCooldown)) {
//                            abstractCooldown.subtractTime(-durations[finalI]);
                            player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §a" + abstractCooldown.getName() + "'s duration was increased by " + durations[finalI] + " seconds");
                        } else {
                            openCooldownsMenu(player, target);
                            player.sendMessage(ChatColor.RED + "DEV: §aThat cooldown no longer exists");
                        }
                    }
            );
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openCooldownMenu(player, target, abstractCooldown));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openStatusEffectTimeMenu(Player player, WarlordsPlayer target, StatusEffectCooldowns cooldown) {
        Menu menu = new Menu("Cooldown Time: " + target.getName(), 9 * 4);
        int[] durations = {5, 15, 30, 60, 120, 300, 600};
        for (int i = 0; i < durations.length; i++) {
            int finalI = i;
            menu.setItem(i + 1, 1,
                    new ItemBuilder(woolSortedByColor[i + 5])
                            .name(ChatColor.GREEN.toString() + durations[i] + "s")
                            .get(),
                    (n, e) -> {
                        target.getCooldownManager().addRegularCooldown(cooldown.name, cooldown.actionBarName, cooldown.cooldownClass, cooldown.cooldownObject, target, cooldown.cooldownType, cooldownManager -> {
                        }, durations[finalI] * 20);
                        if (cooldown == StatusEffectCooldowns.SPEED) {
                            target.getSpeed().addSpeedModifier("Speed Powerup", 40, durations[finalI] * 20, "BASE");
                        }
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aRecieved " + durations[finalI] + " seconds of " + cooldown.name);
                    }
            );
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openCooldownsMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openTeleportLocations(Player player, WarlordsPlayer target) {
        Menu menu = new Menu("Teleport To: " + target.getName(), 9 * 5);

        Game game = target.getGame();
        int x = 0;
        int y = 0;
        for (DebugLocationMarker marker : game.getMarkers(DebugLocationMarker.class)) {
            menu.setItem(x, y, marker.getAsItem(), (n, e) -> {
                target.teleport(marker.getLocation());
                player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "§a was teleported to " + marker.getName());

            });

            x++;

            if (x > 8) {
                x = 0;
                y++;
            }
        }
        menu.setItem(3, 4, MENU_BACK, (n, e) -> openPlayerMenu(player, target));
        menu.setItem(4, 4, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openFlagOptionMenu(Player player, WarlordsPlayer target) {
        Menu menu = new Menu("Flag Options: " + target.getName(), 9 * 4);
        ItemStack[] flagOptions = {
                new ItemBuilder(Material.BANNER)
                        .name(ChatColor.GREEN + "Pick Up Flag")
                        .get(),
                new ItemBuilder(Material.BED)
                        .name(ChatColor.GREEN + "Return the Flag")
                        .get(),
                new ItemBuilder(Material.GRASS)
                        .name(ChatColor.GREEN + "Drop Flag")
                        .get(),
                new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .name(ChatColor.GREEN + "Set Multiplier")
                        .get(),
        };
        int row = 0;
        for (FlagHolder holder : target.getGame().getMarkers(FlagHolder.class)) {
            if (holder.getTeam() == target.getTeam()) {
                continue;
            }
            row++;
            for (int i = 0; i < flagOptions.length; i++) {
                int finalI = i;
                menu.setItem(i + 1, row, flagOptions[i],
                        (n, e) -> {
                            switch (finalI) {
                                case 0:
                                    if (target.getCarriedFlag() == holder.getInfo()) {
                                        player.sendMessage(ChatColor.RED + "DEV: §aThat player already has the flag");
                                    } else {
                                        FlagHolder.update(
                                                target.getGame(),
                                                info -> info.getFlag() instanceof PlayerFlagLocation && ((PlayerFlagLocation) info.getFlag()).getPlayer() == target ?
                                                        GroundFlagLocation.of(info.getFlag()) :
                                                        info == holder.getInfo() ?
                                                                PlayerFlagLocation.of(info.getFlag(), target) :
                                                                null
                                        );
                                    }
                                    break;
                                case 1:
                                    if (target.getCarriedFlag() == holder.getInfo()) {
                                        holder.getInfo().setFlag(new SpawnFlagLocation(holder.getInfo().getSpawnLocation(), null));
                                    } else {
                                        player.sendMessage(ChatColor.RED + "DEV: §aThat player does not have the flag");
                                    }
                                    break;
                                case 2:
                                    if (target.getCarriedFlag() == holder.getInfo()) {
                                        holder.getInfo().setFlag(GroundFlagLocation.of(holder.getFlag()));
                                    } else {
                                        player.sendMessage(ChatColor.RED + "DEV: §aThat player does not have the flag");
                                    }
                                    break;
                                case 3:
                                    if (target.getCarriedFlag() == holder.getInfo()) {
                                        openFlagMultiplierMenu(player, target);
                                    } else {
                                        player.sendMessage(ChatColor.RED + "DEV: §aThat player does not have the flag");
                                    }
                                    break;
                            }
                        }
                );
            }
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openPlayerMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openFlagMultiplierMenu(Player player, WarlordsPlayer target) {
        Menu menu = new Menu("Flag Multiplier: " + target.getName(), 9 * 4);
        int[] multipliers = {5, 10, 30, 60, 100, 150, 300};
        for (int i = 0; i < 7; i++) {
            int finalI = i;
            menu.setItem(i + 1, 1,
                    new ItemBuilder(woolSortedByColor[i + 5])
                            .name(ChatColor.GREEN.toString() + multipliers[i])
                            .get(),
                    (n, e) -> {
                        int amount = e.isLeftClick() ? multipliers[finalI] : -multipliers[finalI];
                        if (target.getCarriedFlag() != null) {
                            PlayerFlagLocation redFlag = ((PlayerFlagLocation) target.getCarriedFlag().getFlag());
                            if (redFlag.getPickUpTicks() + (60 * amount) < 0) {
                                amount = -redFlag.getPickUpTicks() / 60;
                            }
                            redFlag.addPickUpTicks(60 * amount);
                            player.sendMessage(ChatColor.RED + "DEV: §aThe blue flag carrier gained " + amount + "%");
                        }
                    }
            );
        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openFlagOptionMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openSpecMenu(Player player, WarlordsPlayer target) {
        Menu menu = new Menu("Spec Menu: " + target.getName(), 9 * 6);
        ClassesGroup[] values = ClassesGroup.values();
        for (int i = 0; i < values.length; i++) {
            ClassesGroup group = values[i];
            menu.setItem(2, i,
                    new ItemBuilder(group.item)
                            .name(ChatColor.GREEN + group.name)
                            .get(),
                    (n, e) -> {
                    });
            List<Classes> classes = group.subclasses;
            for (int j = 0; j < classes.size(); j++) {
                int finalJ = j;
                ItemBuilder spec = new ItemBuilder(classes.get(j).specType.itemStack).name(ChatColor.GREEN + classes.get(j).name);
                if (target.getSpecClass() == classes.get(j)) {
                    spec.enchant(Enchantment.OXYGEN, 1);
                    spec.flags(ItemFlag.HIDE_ENCHANTS);
                }
                menu.setItem(4 + j, i, spec.get(),
                        (n, e) -> openSkillBoostMenu(player, target, classes.get(finalJ))
                );
            }
        }
        menu.setItem(3, 5, MENU_BACK, (n, e) -> openPlayerMenu(player, target));
        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openSkillBoostMenu(Player player, WarlordsPlayer target, Classes selectedClass) {
        Menu menu = new Menu("Skill Boost: " + target.getName(), 9 * 4);
        List<ClassesSkillBoosts> values = selectedClass.skillBoosts;
        for (int i = 0; i < values.size(); i++) {
            ClassesSkillBoosts skillBoost = values.get(i);
            menu.setItem(
                    i + 2,
                    1,
                    new ItemBuilder(selectedClass.specType.itemStack)
                            .name(ChatColor.RED + skillBoost.name + " (" + selectedClass.name + ")")
                            .lore(skillBoost.description,
                                    "",
                                    ChatColor.YELLOW + "Click to select!"
                            ).get(),
                    (n, e) -> {
                        setSelectedBoost(Bukkit.getPlayer(target.getUuid()), skillBoost);
                        target.setSpec(selectedClass.create.get(), skillBoost);
                        target.getGameState().updatePlayerName(target);
                        player.sendMessage(ChatColor.RED + "DEV: " + target.getColoredName() + "'s §aspec was changed to " + selectedClass.name);
                        openSpecMenu(player, target);
                    }
            );

        }
        menu.setItem(3, 3, MENU_BACK, (n, e) -> openSpecMenu(player, target));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }
}