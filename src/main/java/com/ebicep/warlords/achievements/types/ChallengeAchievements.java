package com.ebicep.warlords.achievements.types;

import com.ebicep.warlords.achievements.Achievement;
import com.ebicep.warlords.events.WarlordsDamageHealingFinalEvent;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.player.Classes;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.ChatUtils;
import com.ebicep.warlords.util.WordWrap;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum ChallengeAchievements implements Achievement {

    REJUVENATION("Rejuvenation",
            "Heal your flag carrier from below 1k health to their maximum health capacity or above in 3 seconds.",
            GameMode.CAPTURE_THE_FLAG,
            null,
            warlordsPlayer -> {
                List<WarlordsDamageHealingFinalEvent> events = warlordsPlayer.getSecondStats().getEventsAsAttackerFromLastSecond(3);
                WarlordsPlayer carrier = null;
                int below1000Index = -1;
                int fullHealthIndex = -1;
                for (int i = 0; i < events.size(); i++) {
                    WarlordsDamageHealingFinalEvent event = events.get(i);
                    if (event.isHasFlag() && event.getInitialHealth() <= 1000) {
                        below1000Index = i;
                        carrier = event.getPlayer(); //carrier must be the same person, no repicks
                        break;
                    }
                }
                if (below1000Index != -1) {
                    for (int i = below1000Index; i < events.size(); i++) {
                        WarlordsDamageHealingFinalEvent event = events.get(i);
                        if (event.getPlayer() == carrier && event.isHasFlag() && event.getFinalHealth() >= carrier.getMaxHealth()) {
                            fullHealthIndex = i;
                            break;
                        }
                    }
                } else {
                    return false;
                }
                if (fullHealthIndex != -1) {
                    List<WarlordsDamageHealingFinalEvent> healingEvents = new ArrayList<>();
                    for (int i = below1000Index; i <= fullHealthIndex; i++) {
                        healingEvents.add(events.get(i));
                    }
                    healingEvents.stream()
                            .filter(WarlordsDamageHealingFinalEvent::isHealingInstance)
                            .map(WarlordsDamageHealingFinalEvent::getValue)
                            .mapToDouble(Float::doubleValue)
                            .forEach(System.out::println);
                    return (float) healingEvents.stream()
                            .filter(WarlordsDamageHealingFinalEvent::isHealingInstance)
                            .map(WarlordsDamageHealingFinalEvent::getValue)
                            .mapToDouble(Float::doubleValue)
                            .sum() >= 2000;
                } else {
                    return false;
                }
            },
            true),

    BLITZKRIEG("Blitzkrieg",
            "Kill the enemy flag carrier within 2 seconds.",
            GameMode.CAPTURE_THE_FLAG,
            null,
            warlordsPlayer -> {
                List<WarlordsDamageHealingFinalEvent> events = warlordsPlayer.getSecondStats().getEventsAsAttackerFromLastSecond(2);
                int indexCarrierFull = -1;
                int indexCarrierDead = -1;
                for (int i = 0; i < events.size(); i++) {
                    WarlordsDamageHealingFinalEvent event = events.get(i);
                    if (event.isHasFlag() || event.getValue() >= event.getInitialHealth()) { //for one shots
                        if (event.getInitialHealth() >= event.getPlayer().getMaxHealth()) {
                            indexCarrierFull = i;
                            break;
                        }
                    }
                }
                if (indexCarrierFull != -1) {
                    for (int i = indexCarrierFull; i < events.size(); i++) {
                        WarlordsDamageHealingFinalEvent event = events.get(i);
                        if (event.isDead()) {
                            indexCarrierDead = i;
                            break;
                        }
                    }
                } else {
                    return false;
                }
                if (indexCarrierDead != -1) {
                    List<WarlordsDamageHealingFinalEvent> damageEvents = new ArrayList<>();
                    for (int i = indexCarrierFull; i <= indexCarrierDead; i++) {
                        damageEvents.add(events.get(i));
                    }
                    for (WarlordsDamageHealingFinalEvent damageEvent : damageEvents) {
                        System.out.println(damageEvent);
                    }
                    return (float) damageEvents.stream()
                            .filter(WarlordsDamageHealingFinalEvent::isDamageInstance)
                            .map(WarlordsDamageHealingFinalEvent::getValue)
                            .mapToDouble(Float::doubleValue)
                            .sum() >= 2000;
                } else {
                    return false;
                }
            },
            true),

    SNIPE_SHOT("Snipe Shot",
            "Kill the enemy flag carrier while being at least 30 blocks away from them.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.PYROMANCER,
            warlordsPlayer -> {
                WarlordsDamageHealingFinalEvent lastEvent = warlordsPlayer.getSecondStats().getLastEventsAsAttacker(1, 1).get(0);
                return lastEvent.isDead() && lastEvent.isHasFlag() && lastEvent.getPlayer().getLocation().distanceSquared(lastEvent.getAttacker().getLocation()) > 900;
            },
            false),

    DUCK_TANK("Duck Tank",
            "Tank 9000 damage without losing health while holding the flag.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.CRYOMANCER,
            warlordsPlayer -> {
                List<WarlordsDamageHealingFinalEvent> events = warlordsPlayer.getSecondStats().getEventsAsSelfFromLastSecond(10).stream()
                        .filter(WarlordsDamageHealingFinalEvent::isDamageInstance)
                        .collect(Collectors.toList());
                if (events.isEmpty()) return false;
                int lastHealth = events.get(0).getFinalHealth();
                float totalAbsorbed = 0;
                for (WarlordsDamageHealingFinalEvent event : events) {
                    if (event.isHasFlag() && lastHealth == event.getFinalHealth()) {
                        totalAbsorbed += event.getValue();
                        if (totalAbsorbed >= 9000) {
                            return true;
                        }
                    } else {
                        lastHealth = event.getFinalHealth();
                        totalAbsorbed = 0;
                    }
                }
                return false;
            },
            false),
    CLERICAL_PRODIGY("Clerical Prodigy",
            "Heal your carrier for over 80k damage within a game.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.AQUAMANCER,
            warlordsPlayer -> warlordsPlayer.getMinuteStats().total().getHealingOnCarrier() >= 80000,
            false),
    ASSASSINATE("Assassinate",
            "Land 7 critical hits on the enemy carrier in a row.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.ASSASSIN,
            warlordsPlayer -> {
                List<WarlordsDamageHealingFinalEvent> events = warlordsPlayer.getSecondStats().getEventsAsAttackerFromLastSecond(10);
                int critsOnCarrier = 0;
                for (WarlordsDamageHealingFinalEvent event : events) {
                    if (event.isCrit() && event.isHasFlag()) {
                        critsOnCarrier++;
                        if (critsOnCarrier == 7) {
                            return true;
                        }
                    } else {
                        critsOnCarrier = 0;
                    }
                }
                return false;
            },
            false),
    SILENCE_PEON("Silence, peon!",
            "Kill the enemy flag carrier (that you silenced) while the silence duration is still up. ",
            GameMode.CAPTURE_THE_FLAG,
            Classes.VINDICATOR,
            warlordsPlayer -> false,
            false),
    LYCHEESIS("Lycheesis",
            "Generate over 3k healing by inflicting one instance of LEECH on the enemy flag carrier.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.APOTHECARY,
            warlordsPlayer -> false,
            false),
    EXTENDED_COMBAT("Extended Combat",
            "Stay in combat for over 40 seconds and deal 10k damage to the enemy carrier. ",
            GameMode.CAPTURE_THE_FLAG,
            Classes.BERSERKER,
            warlordsPlayer -> false,
            false),
    SPLIT_SECOND("Split Second",
            "Prevent over 2k damage dealt to the flag carrier within 1s of the ability activating.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.DEFENDER,
            warlordsPlayer -> false,
            false),
    ORBIFICATOR("Orbificator",
            "Return the flag while being popped from your Undying Army.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.REVENANT,
            warlordsPlayer -> false,
            false),
    REVENGE_BLAST("Revenge Blast",
            "Kill 3 enemies within 5s of your flag carrier dying. ",
            GameMode.CAPTURE_THE_FLAG,
            Classes.AVENGER,
            warlordsPlayer -> false,
            false),
    HOUR_OF_RECKONING("Hour of Reckoning",
            "Kill the enemy carrier while 4 or more allies are affected by your Inspiring Presence.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.CRUSADER,
            warlordsPlayer -> false,
            false),
    TALENT_SHREDDER("Talent Shredder",
            "Deal 3k damage to the enemy carrier while they have an active shield/damage reduction.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.PROTECTOR,
            warlordsPlayer -> false,
            false),
    ROADBLOCK("Roadblock?!",
            "Proc your Capacitor Totem three (or more) times after your carrier passes through the totem.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.THUNDERLORD,
            warlordsPlayer -> false,
            false),
    PERSISTENT_THREAT("Persistent Threat",
            "Proc soulbinding healing/cooldown reduction 10 times on the enemy carrier within 20 seconds.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.SPIRITGUARD,
            warlordsPlayer -> false,
            false),
    WHERE_ARE_YOU_GOING("Where are you going?",
            "Kill the enemy flag carrier after landing 5 or more abilities on them.",
            GameMode.CAPTURE_THE_FLAG,
            Classes.EARTHWARDEN,
            warlordsPlayer -> false,
            false),

    ;

    public String name;
    public String description;
    public GameMode gameMode;
    public Classes spec;
    public Predicate<WarlordsPlayer> warlordsPlayerPredicate;
    public boolean checkTeammates;

    ChallengeAchievements(String name, String description, GameMode gameMode, Classes spec, Predicate<WarlordsPlayer> warlordsPlayerPredicate, boolean checkTeammates) {
        this.name = name;
        this.description = description;
        this.gameMode = gameMode;
        this.spec = spec;
        this.warlordsPlayerPredicate = warlordsPlayerPredicate;
        this.checkTeammates = checkTeammates;
    }

    public static void checkForAchievement(WarlordsPlayer player, ChallengeAchievements achievement) {
        if (achievement.warlordsPlayerPredicate.test(player)) {
            if (achievement.checkTeammates) {
                ChallengeAchievements.checkTeammatesForSameAchievement(player, achievement);
            } else {
                //if(!player.hasAchievement(achievement)) {
                player.unlockAchievement(achievement);
                //}
            }
        }
    }

    public static void checkTeammatesForSameAchievement(WarlordsPlayer player, ChallengeAchievements achievement) {
        player.getGame().warlordsPlayers()
                .filter(warlordsPlayer -> warlordsPlayer.getTeam() == player.getTeam())
                //.filter(warlordsPlayer -> !warlordsPlayer.hasAchievement(achievement))
                .filter(warlordsPlayer -> achievement.warlordsPlayerPredicate.test(warlordsPlayer))
                .forEachOrdered(warlordsPlayer -> warlordsPlayer.unlockAchievement(achievement));
    }

    @Override
    public void sendAchievementUnlockMessage(Player player) {
        TextComponent message = new TextComponent(ChatColor.GREEN + ">>  Achievement Unlocked: " + ChatColor.GOLD + name + ChatColor.GREEN + "  <<");
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(WordWrap.wrapWithNewlineWithColor(description, 200, ChatColor.GREEN)).create()));
        ChatUtils.sendMessageToPlayer(player, Collections.singletonList(message), ChatColor.GREEN, true);
    }

    @Override
    public void sendAchievementUnlockMessageToOthers(WarlordsPlayer warlordsPlayer) {
        TextComponent message = new TextComponent(ChatColor.GREEN + ">>  " + ChatColor.AQUA + warlordsPlayer.getName() + ChatColor.GREEN + " unlocked: " + ChatColor.GOLD + name + ChatColor.GREEN + "  <<");
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(WordWrap.wrapWithNewlineWithColor(description, 200, ChatColor.GREEN)).create()));
        warlordsPlayer.getGame().warlordsPlayers()
                //.filter(wp -> wp.getTeam() == warlordsPlayer.getTeam())
                .filter(wp -> wp != warlordsPlayer)
                .filter(wp -> wp.getEntity() instanceof Player)
                .map(wp -> (Player) wp.getEntity())
                .forEachOrdered(player -> ChatUtils.sendMessageToPlayer(player, Collections.singletonList(message), ChatColor.GREEN, true));
    }

    public static class ChallengeAchievementRecord extends AbstractAchievementRecord<ChallengeAchievements> {

        public ChallengeAchievementRecord() {
        }

        public ChallengeAchievementRecord(ChallengeAchievements achievement) {
            super(achievement);
        }

        public ChallengeAchievementRecord(ChallengeAchievements achievement, Date date) {
            super(achievement, date);
        }

        @Override
        public String getName() {
            return getAchievement().name;
        }

        @Override
        public String getDescription() {
            return getAchievement().description;
        }

        @Override
        public GameMode getGameMode() {
            return getAchievement().gameMode;
        }

        @Override
        public ChallengeAchievements[] getAchievements() {
            return ChallengeAchievements.values();
        }

    }
}