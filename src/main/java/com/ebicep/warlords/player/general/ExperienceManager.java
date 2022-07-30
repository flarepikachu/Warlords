package com.ebicep.warlords.player.general;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabaseSpecialization;
import com.ebicep.warlords.database.repositories.player.pojos.general.FutureMessage;
import com.ebicep.warlords.game.GameAddon;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.pve.rewards.LevelUpReward;
import com.ebicep.warlords.pve.rewards.RewardTypes;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.java.Pair;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ebicep.warlords.menu.Menu.*;

public class ExperienceManager {

    public static final Map<Integer, Long> LEVEL_TO_EXPERIENCE;
    public static final Map<Long, Integer> EXPERIENCE_TO_LEVEL;
    public static final DecimalFormat EXPERIENCE_DECIMAL_FORMAT = new DecimalFormat("#,###.#");
    public static final HashMap<UUID, LinkedHashMap<String, Long>> CACHED_PLAYER_EXP_SUMMARY = new HashMap<>();
    private static final Map<String, int[]> awardOrder = new LinkedHashMap<String, int[]>() {{
        put("wins", new int[]{1000, 750, 500});
        put("losses", new int[]{200, 150, 100});
        put("kills", new int[]{850, 600, 350});
        put("assists", new int[]{850, 600, 350});
        put("deaths", new int[]{200, 150, 100});
        put("dhp", new int[]{1000, 750, 500});
        put("dhp_per_game", new int[]{1000, 750, 500});
        put("damage", new int[]{850, 600, 350});
        put("healing", new int[]{850, 600, 350});
        put("absorbed", new int[]{850, 600, 350});
        put("flags_captured", new int[]{600, 400, 200});
        put("flags_returned", new int[]{600, 400, 200});
    }};
    public static final int LEVEL_TO_PRESTIGE = 100;
    public static final List<Pair<ChatColor, Color>> PRESTIGE_COLORS = Arrays.asList(
            new Pair<>(ChatColor.GRAY, Color.GRAY),//0
            new Pair<>(ChatColor.RED, Color.RED),//1
            new Pair<>(ChatColor.YELLOW, Color.YELLOW),//2
            new Pair<>(ChatColor.GREEN, Color.GREEN),//3
            new Pair<>(ChatColor.AQUA, Color.AQUA), //4
            new Pair<>(ChatColor.BLUE, Color.BLUE), //5
            new Pair<>(ChatColor.LIGHT_PURPLE, Color.FUCHSIA), //6
            new Pair<>(ChatColor.BLACK, Color.BLACK), //7
            new Pair<>(ChatColor.WHITE, Color.WHITE), //8
            new Pair<>(ChatColor.DARK_GRAY, Color.GRAY), //9
            new Pair<>(ChatColor.DARK_RED, Color.RED), //10
            new Pair<>(ChatColor.GOLD, Color.ORANGE), //11
            new Pair<>(ChatColor.DARK_AQUA, Color.AQUA), //12
            new Pair<>(ChatColor.DARK_BLUE, Color.BLUE), //13
            new Pair<>(ChatColor.DARK_PURPLE, Color.PURPLE) //13
    );

    static {
        //caching all levels/experience
        Map<Integer, Long> levelExperienceNew = new HashMap<>();
        Map<Long, Integer> experienceLevelNew = new HashMap<>();
        for (int i = 0; i < 501; i++) {
            long exp = (long) calculateExpFromLevel(i);
            levelExperienceNew.put(i, exp);
            experienceLevelNew.put(exp, i);
        }

        LEVEL_TO_EXPERIENCE = Collections.unmodifiableMap(levelExperienceNew);
        EXPERIENCE_TO_LEVEL = Collections.unmodifiableMap(experienceLevelNew);

        EXPERIENCE_DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
    }

    private static final HashMap<Classes, Pair<Integer, Integer>> CLASSES_MENU_LOCATION = new HashMap<Classes, Pair<Integer, Integer>>() {{
        put(Classes.MAGE, new Pair<>(2, 1));
        put(Classes.WARRIOR, new Pair<>(4, 1));
        put(Classes.PALADIN, new Pair<>(6, 1));
        put(Classes.SHAMAN, new Pair<>(3, 3));
        put(Classes.ROGUE, new Pair<>(5, 3));
    }};

    public static void openLevelingRewardsMenu(Player player) {
        Menu menu = new Menu("Rewards Menu", 9 * 6);

        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());

        for (Classes value : Classes.values()) {
            Pair<Integer, Integer> menuLocation = CLASSES_MENU_LOCATION.get(value);

            List<String> specLore = new ArrayList<>();
            for (Specializations spec : value.subclasses) {
                int prestige = databasePlayer.getSpec(spec).getPrestige();
                int level = getLevelFromExp(databasePlayer.getSpec(spec).getExperience());
                long experience = databasePlayer.getSpec(spec).getExperience();

                specLore.add(ChatColor.GOLD + spec.name + ChatColor.DARK_GRAY + " [" + ChatColor.GRAY + getLevelString(level) + ChatColor.DARK_GRAY + "] " + getPrestigeLevelString(prestige));
                specLore.add(getProgressStringWithPrestige(experience, level + 1, prestige));
                specLore.add("");
            }

            menu.setItem(
                    menuLocation.getA(),
                    menuLocation.getB(),
                    new ItemBuilder(value.item)
                            .name(ChatColor.GREEN + value.name)
                            .lore(specLore)
                            .get(),
                    (m, e) -> openLevelingRewardsMenuForClass(player, value)
            );
        }

        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openLevelingRewardsMenuForClass(Player player, Classes classes) {
        Menu menu = new Menu(classes.name, 9 * 4);

        Specializations selectedSpec = Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec();
        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());

        List<Specializations> values = classes.subclasses;
        for (int i = 0; i < values.size(); i++) {
            Specializations spec = values.get(i);
            int prestige = databasePlayer.getSpec(spec).getPrestige();
            int level = getLevelFromExp(databasePlayer.getSpec(spec).getExperience());
            long experience = databasePlayer.getSpec(spec).getExperience();

            menu.setItem(
                    9 / 2 - values.size() / 2 + i * 2 - 1,
                    1,
                    new ItemBuilder(spec.specType.itemStack)
                            .name(ChatColor.GREEN + spec.name + " " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Lv" + getLevelString(level) + ChatColor.DARK_GRAY + "] " + getPrestigeLevelString(prestige))
                            .lore(getProgressStringWithPrestige(experience, level + 1, prestige))
                            .get(),
                    (m, e) -> openLevelingRewardsMenuForSpec(player, spec, 1, databasePlayer.getSpec(spec).getPrestige())
            );
        }

        menu.setItem(3, 3, MENU_BACK, (m, e) -> openLevelingRewardsMenu(player));
        menu.setItem(4, 3, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    private static final int LEVELS_PER_PAGE = 25;

    public static void openLevelingRewardsMenuForSpec(Player player, Specializations spec, int page, int selectedPrestige) {
        Menu menu = new Menu(spec.name, 9 * 6);

        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
        DatabaseSpecialization databaseSpecialization = databasePlayer.getSpec(spec);
        int currentPrestige = databasePlayer.getSpec(spec).getPrestige();
        int level = getLevelFromExp(databasePlayer.getSpec(spec).getExperience());
        long experience = databasePlayer.getSpec(spec).getExperience();

        menu.setItem(
                4,
                0,
                new ItemBuilder(spec.specType.itemStack)
                        .name(ChatColor.GREEN + spec.name + " " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Lv" + getLevelString(level) + ChatColor.DARK_GRAY + "] " + getPrestigeLevelString(currentPrestige))
                        .lore(getProgressStringWithPrestige(experience, level + 1, currentPrestige))
                        .get(),
                (m, e) -> {
                }
        );

        for (int i = 0; i <= LEVELS_PER_PAGE; i++) {
            int section = i * page;
            if (section == 0) {
                continue;
            }
            int column = (i - 1) % 9;
            int row = (i - 1) / 9 + 1;

            if (i >= 19) {
                column++;
            }

            int menuLevel = i + ((page - 1) * LEVELS_PER_PAGE);
            Pair<RewardTypes, Float> rewardForLevel = LevelUpReward.getRewardForLevel(menuLevel);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Reward: " + ChatColor.GOLD + rewardForLevel.getA().name);
            lore.add(ChatColor.GRAY + "Amount: " + ChatColor.GOLD + NumberFormat.formatOptionalHundredths(rewardForLevel.getB()));
            lore.add("");
            AtomicBoolean claimed = new AtomicBoolean(false);
            boolean currentPrestigeSelected = selectedPrestige != currentPrestige;
            if (menuLevel <= level || currentPrestigeSelected) {
                claimed.set(databaseSpecialization.hasLevelUpReward(menuLevel, selectedPrestige));
                if (claimed.get()) {
                    lore.add(ChatColor.GREEN + "Claimed!");
                } else {
                    lore.add(ChatColor.YELLOW + "Click to claim!");
                }
            } else {
                lore.add(ChatColor.RED + "You can't claim this yet!");
            }
            menu.setItem(
                    column,
                    row,
                    new ItemBuilder(Material.STAINED_GLASS_PANE, 1, menuLevel <= level || currentPrestigeSelected ? claimed.get() ? (short) 5 : (short) 4 : (short) 15)
                            .name((menuLevel <= level ? ChatColor.GREEN : ChatColor.RED) + "Level Reward " + menuLevel)
                            .lore(lore)
                            .get(),
                    (m, e) -> {
                        if (menuLevel <= level || currentPrestigeSelected) {
                            if (claimed.get()) {
                                player.sendMessage(ChatColor.RED + "You already claimed this reward!");
                            } else {
                                rewardForLevel.getA().biConsumer.accept(databasePlayer, rewardForLevel.getB());
                                databaseSpecialization.addLevelUpReward(new LevelUpReward(rewardForLevel.getA(), rewardForLevel.getB(), menuLevel, selectedPrestige));
                                player.sendMessage(ChatColor.GREEN + "You claimed the reward for level " + menuLevel + "!");
                                DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                                openLevelingRewardsMenuForSpec(player, spec, page, selectedPrestige);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You can't claim this reward yet!");
                        }
                    }
            );
        }

        if (currentPrestige != 0) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.HOPPER)
                    .name(ChatColor.GREEN + "Click to Cycle Between Prestige Rewards");
            List<String> lore = new ArrayList<>();
            for (int i = 0; i <= currentPrestige; i++) {
                lore.add((i == selectedPrestige ? ChatColor.AQUA : ChatColor.GRAY) + "Prestige " + i);
            }
            itemBuilder.lore(lore);
            menu.setItem(5, 5,
                    itemBuilder.get(),
                    (m, e) -> {
                        if (selectedPrestige == currentPrestige) {
                            openLevelingRewardsMenuForSpec(player, spec, page, 0);
                        } else {
                            openLevelingRewardsMenuForSpec(player, spec, page, selectedPrestige + 1);
                        }
                    });
        }

        if (page - 1 > 0) {
            menu.setItem(
                    0,
                    3,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "Previous Page")
                            .lore(ChatColor.YELLOW + "Page " + (page - 1))
                            .get(),
                    (m, e) -> openLevelingRewardsMenuForSpec(player, spec, page - 1, selectedPrestige)
            );
        }
        if (page + 1 < 5) {
            menu.setItem(
                    8,
                    3,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "Next Page")
                            .lore(ChatColor.YELLOW + "Page " + (page + 1))
                            .get(),
                    (m, e) -> openLevelingRewardsMenuForSpec(player, spec, page + 1, selectedPrestige)
            );

        }


        menu.setItem(3, 5, MENU_BACK, (m, e) -> openLevelingRewardsMenuForClass(player, Specializations.getClass(spec)));
        menu.setItem(4, 5, MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void awardWeeklyExperience(Document weeklyDocument) {
        if (DatabaseManager.playerService == null) {
            System.out.println("WARNING - Could not give weekly experience bonus - playerService is null");
            return;
        }

        HashMap<String, AwardSummary> playerAwardSummary = new HashMap<>();

        awardOrder.forEach((key, rewards) -> {
            String name = weeklyDocument.getEmbedded(Arrays.asList(key, "name"), String.class);
            List<Document> top = weeklyDocument.getEmbedded(Arrays.asList(key, "top"), new ArrayList<>());
            for (int i = 0; i < top.size(); i++) {
                Document topDocument = top.get(i);
                String[] uuids = topDocument.getString("uuids").split(",");
                for (String uuid : uuids) {
                    int experienceGain = rewards[i];
                    playerAwardSummary.putIfAbsent(uuid, new AwardSummary());
                    AwardSummary awardSummary = playerAwardSummary.get(uuid);
                    awardSummary.getMessages().add(ChatColor.YELLOW + "#" + (i + 1) + ". " + ChatColor.AQUA + name + ChatColor.WHITE + ": " + ChatColor.DARK_GRAY + "+" + ChatColor.DARK_AQUA + experienceGain + ChatColor.GOLD + " Universal Experience");
                    awardSummary.addTotalExperienceGain(experienceGain);
                }
            }
        });

        System.out.println("---------------------------------------------------");
        System.out.println("Giving players weekly experience bonuses");
        System.out.println("---------------------------------------------------");
        playerAwardSummary.forEach((s, awardSummary) -> {
            long totalExperienceGain = awardSummary.getTotalExperienceGain();

            awardSummary.addMessage(ChatColor.GOLD + "Total Experience Gain" + ChatColor.WHITE + ": " + ChatColor.DARK_GRAY + "+" + ChatColor.DARK_AQUA + totalExperienceGain);
            awardSummary.addMessage(ChatColor.BLUE + "---------------------------------------------------");

            Warlords.newChain()
                    .asyncFirst(() -> DatabaseManager.playerService.findByUUID(UUID.fromString(s)))
                    .syncLast(databasePlayer -> {
                        databasePlayer.setExperience(databasePlayer.getExperience() + totalExperienceGain);
                        databasePlayer.getFutureMessages().add(new FutureMessage(awardSummary.getMessages(), true));
                        DatabaseManager.queueUpdatePlayerAsync(databasePlayer);
                    }).execute();
        });
    }

    static class AwardSummary {
        List<String> messages = new ArrayList<>();
        long totalExperienceGain = 0L;

        public AwardSummary() {
            messages.add(ChatColor.BLUE + "---------------------------------------------------");
            messages.add(ChatColor.GREEN + "Weekly Experience Bonus\n ");
        }

        public List<String> getMessages() {
            return messages;
        }

        public long getTotalExperienceGain() {
            return totalExperienceGain;
        }

        public void addMessage(String message) {
            messages.add(message);
        }

        public void addTotalExperienceGain(long amount) {
            totalExperienceGain += amount;
        }
    }

    public static LinkedHashMap<String, Long> getExpFromGameStats(WarlordsEntity warlordsPlayer, boolean recalculate) {
        if (!recalculate && CACHED_PLAYER_EXP_SUMMARY.containsKey(warlordsPlayer.getUuid()) && CACHED_PLAYER_EXP_SUMMARY.get(warlordsPlayer.getUuid()) != null) {
            return CACHED_PLAYER_EXP_SUMMARY.get(warlordsPlayer.getUuid());
        }

        boolean isCompGame = warlordsPlayer.getGame().getAddons().contains(GameAddon.PRIVATE_GAME);
        float multiplier = 1;
        //pubs
        if (!isCompGame) {
            multiplier *= .1;
        }
        //duels
        if (warlordsPlayer.getGame().getGameMode() == GameMode.DUEL) {
            multiplier *= .1;
        }

        // TODO add check here for game ending in a draw
        boolean won = warlordsPlayer.getGame().getPoints(warlordsPlayer.getTeam()) > warlordsPlayer.getGame().getPoints(warlordsPlayer.getTeam().enemy());
        long winLossExp = won ? 500 : 250;
        long kaExp = 5L * (warlordsPlayer.getMinuteStats().total().getKills() + warlordsPlayer.getMinuteStats().total().getAssists());

        double damageMultiplier;
        double healingMultiplier;
        double absorbedMultiplier;
        Specializations specializations = warlordsPlayer.getSpecClass();
        if (specializations.specType == SpecType.DAMAGE) {
            damageMultiplier = .80;
            healingMultiplier = .10;
            absorbedMultiplier = .10;
        } else if (specializations.specType == SpecType.HEALER) {
            damageMultiplier = .275;
            healingMultiplier = .65;
            absorbedMultiplier = .75;
        } else { //tank
            damageMultiplier = .575;
            healingMultiplier = .1;
            absorbedMultiplier = .325;
        }
        double calculatedDHP = warlordsPlayer.getMinuteStats().total().getDamage() * damageMultiplier + warlordsPlayer.getMinuteStats().total().getHealing() * healingMultiplier + warlordsPlayer.getMinuteStats().total().getAbsorbed() * absorbedMultiplier;
        long dhpExp = (long) (calculatedDHP / 500L);
        long flagCapExp = warlordsPlayer.getFlagsCaptured() * 150L;
        long flagRetExp = warlordsPlayer.getFlagsReturned() * 50L;

        LinkedHashMap<String, Long> expGain = new LinkedHashMap<>();
        expGain.put(won ? "Win" : "Loss", (long) (winLossExp * multiplier));
        if (kaExp != 0) {
            expGain.put("Kills/Assists", (long) (kaExp * multiplier));
        }
        if (dhpExp != 0) {
            expGain.put("DHP", (long) (dhpExp * multiplier));
        }
        if (flagCapExp != 0) {
            expGain.put("Flags Captured", (long) (flagCapExp * multiplier));
        }
        if (flagRetExp != 0) {
            expGain.put("Flags Returned", (long) (flagRetExp * multiplier));
        }

        try {
            DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(warlordsPlayer.getUuid(), PlayersCollections.DAILY);
            if (databasePlayer != null) {
                if (isCompGame) {
                    switch (databasePlayer.getCompStats().getPlays()) {
                        case 0:
                            expGain.put("First Game of the Day", 500L);
                            break;
                        case 1:
                            expGain.put("Second Game of the Day", 250L);
                            break;
                        case 2:
                            expGain.put("Third Game of the Day", 100L);
                            break;
                    }
                } else {
                    switch (databasePlayer.getPubStats().getPlays()) {
                        case 0:
                            expGain.put("First Game of the Day", 50L);
                            break;
                        case 1:
                            expGain.put("Second Game of the Day", 25L);
                            break;
                        case 2:
                            expGain.put("Third Game of the Day", 10L);
                            break;
                    }
                }
            } else {
                System.out.println("Could not find player: " + warlordsPlayer.getName() + " during experience calculation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        CACHED_PLAYER_EXP_SUMMARY.put(warlordsPlayer.getUuid(), expGain);
        return expGain;
    }

    public static long getSpecExpFromSummary(LinkedHashMap<String, Long> expSummary) {
        return expSummary.values().stream().mapToLong(Long::longValue).sum()
                - expSummary.getOrDefault("First Game of the Day", 0L)
                - expSummary.getOrDefault("Second Game of the Day", 0L)
                - expSummary.getOrDefault("Third Game of the Day", 0L);
    }

    public static long getExperienceForClass(UUID uuid, Classes classes) {
        if (DatabaseManager.playerService == null) return 0;
        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(uuid);
        return databasePlayer == null ? 0L : databasePlayer.getClass(classes).getExperience();
    }

    public static int getLevelForClass(UUID uuid, Classes classes) {
        return (int) calculateLevelFromExp(getExperienceForClass(uuid, classes));
    }

    public static long getExperienceForSpec(UUID uuid, Specializations spec) {
        return getExperienceFromSpec(uuid, spec);
    }

    public static int getLevelForSpec(UUID uuid, Specializations spec) {
        return (int) calculateLevelFromExp(getExperienceFromSpec(uuid, spec));
    }

    public static int getLevelFromExp(long experience) {
        return (int) calculateLevelFromExp(experience);
    }

    public static long getUniversalLevel(UUID uuid) {
        if (DatabaseManager.playerService == null) return 0;
        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(uuid);
        return databasePlayer == null ? 0L : databasePlayer.getExperience();
    }

    private static long getExperienceFromSpec(UUID uuid, Specializations specializations) {
        if (DatabaseManager.playerService == null) return 0;
        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(uuid);
        return databasePlayer == null ? 0L : databasePlayer.getSpec(specializations).getExperience();
    }

    public static String getLevelString(int level) {
        return level < 10 ? "0" + level : String.valueOf(level);
    }

    public static String getProgressString(long currentExperience, int nextLevel) {
        String progress = ChatColor.GRAY + "Progress to Level " + nextLevel + ": " + ChatColor.YELLOW;
        return getProgressString(currentExperience, nextLevel, progress);
    }

    public static String getProgressStringWithPrestige(long currentExperience, int nextLevel, int currentPrestige) {
        String progress = nextLevel == 100 ?
                ChatColor.GRAY + "Progress to " + PRESTIGE_COLORS.get(currentPrestige + 1).getA() + "PRESTIGE" + ChatColor.GRAY + ": " + ChatColor.YELLOW :
                ChatColor.GRAY + "Progress to Level " + nextLevel + ": " + ChatColor.YELLOW;
        return getProgressString(currentExperience, nextLevel, progress);
    }

    private static String getProgressString(long currentExperience, int nextLevel, String progress) {
        long experience = currentExperience - LEVEL_TO_EXPERIENCE.get(nextLevel - 1);
        long experienceNeeded = LEVEL_TO_EXPERIENCE.get(nextLevel) - LEVEL_TO_EXPERIENCE.get(nextLevel - 1);
        double progressPercentage = (double) experience / experienceNeeded * 100;

        progress += NumberFormat.formatOptionalTenths(progressPercentage) + "%\n" + ChatColor.GREEN;
        int greenBars = (int) Math.round(progressPercentage * 20 / 100);
        for (int i = 0; i < greenBars; i++) {
            progress += "-";
        }
        progress += ChatColor.WHITE;
        for (int i = greenBars; i < 20; i++) {
            progress += "-";
        }
        progress += " " + ChatColor.YELLOW + EXPERIENCE_DECIMAL_FORMAT.format(experience) + ChatColor.GOLD + "/" + ChatColor.YELLOW + NumberFormat.getSimplifiedNumber(experienceNeeded);

        return progress;
    }

    public static String getPrestigeLevelString(UUID uuid, Specializations spec) {
        if (DatabaseManager.playerService == null) return PRESTIGE_COLORS.get(0).getA() + "[-]";
        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(uuid);
        if (databasePlayer == null) return PRESTIGE_COLORS.get(0).getA() + "[-]";
        int prestigeLevel = databasePlayer.getSpec(spec).getPrestige();
        return ChatColor.DARK_GRAY + "[" + PRESTIGE_COLORS.get(prestigeLevel).getA() + prestigeLevel + ChatColor.DARK_GRAY + "]";
    }

    public static String getPrestigeLevelString(int prestigeLevel) {
        return ChatColor.DARK_GRAY + "[" + PRESTIGE_COLORS.get(prestigeLevel).getA() + prestigeLevel + ChatColor.DARK_GRAY + "]";
    }

    public static double calculateLevelFromExp(long exp) {
        return Math.sqrt(exp / 25.0);
    }

    public static double calculateExpFromLevel(int level) {
        return Math.pow(level, 2) * 25;
    }

    public static void giveExperienceBar(Player player) {
        //long experience = warlordsPlayersDatabase.getCollection("Players_Information_Test").find().filter(eq("uuid", player.getUniqueId().toString())).first().getLong("experience");
        long experience = getUniversalLevel(player.getUniqueId());
        int level = (int) calculateLevelFromExp(experience);
        player.setLevel(level);
        player.setExp((float) (experience - LEVEL_TO_EXPERIENCE.get(level)) / (LEVEL_TO_EXPERIENCE.get(level + 1) - LEVEL_TO_EXPERIENCE.get(level)));
    }

    public static void giveLevelUpMessage(Player player, long expBefore, long expAfter) {
        int levelBefore = (int) calculateLevelFromExp(expBefore);
        int levelAfter = (int) calculateLevelFromExp(expAfter);
        if (levelBefore != levelAfter) {
            ChatUtils.sendMessage(player, true, ChatColor.GREEN.toString() + ChatColor.BOLD + ChatColor.MAGIC + "   " + ChatColor.AQUA + ChatColor.BOLD + " LEVEL UP! " + ChatColor.DARK_GRAY + ChatColor.BOLD + "[" + ChatColor.GRAY + ChatColor.BOLD + levelBefore + ChatColor.DARK_GRAY + ChatColor.BOLD + "]" + ChatColor.GREEN + ChatColor.BOLD + " > " + ChatColor.DARK_GRAY + ChatColor.BOLD + "[" + ChatColor.GRAY + ChatColor.BOLD + levelAfter + ChatColor.DARK_GRAY + ChatColor.BOLD + "] " + ChatColor.GREEN + ChatColor.MAGIC + ChatColor.BOLD + "   ");
        }
    }
}