package com.ebicep.warlords.database.repositories.games.pojos.ctf;

import com.ebicep.jda.BotManager;
import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerResult;
import com.ebicep.warlords.events.game.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameAddon;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.WinAfterTimeoutOption;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.ChatColor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Document(collection = "Games_Information_CTF")
public class DatabaseGameCTF extends DatabaseGameBase {

    @Transient
    public static String lastWarlordsPlusString = "";
    @Field("time_left")
    protected int timeLeft;
    protected Team winner;
    @Field("blue_points")
    protected int bluePoints;
    @Field("red_points")
    protected int redPoints;
    protected Map<Team, List<DatabaseGamePlayerCTF>> players = new LinkedHashMap<>();

    public DatabaseGameCTF() {
    }

    public DatabaseGameCTF(@Nonnull Game game, @Nullable WarlordsGameTriggerWinEvent gameWinEvent, boolean counted) {
        super(game, counted);
        this.timeLeft = WinAfterTimeoutOption.getTimeRemaining(game).orElse(-1);
        this.winner = gameWinEvent == null || gameWinEvent.isCancelled() ? null : gameWinEvent.getDeclaredWinner();
        this.bluePoints = game.getPoints(Team.BLUE);
        this.redPoints = game.getPoints(Team.RED);
        game.warlordsPlayers().forEach(warlordsPlayer -> {
            this.players.computeIfAbsent(warlordsPlayer.getTeam(), team -> new ArrayList<>()).add(new DatabaseGamePlayerCTF(warlordsPlayer));
        });
    }

    public static String getWarlordsPlusEndGameStats(Game game) {
        StringBuilder output = new StringBuilder("Winners:");
        int bluePoints = game.getPoints(Team.BLUE);
        int redPoints = game.getPoints(Team.RED);
        if (bluePoints > redPoints) {
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.BLUE)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
            output.setLength(output.length() - 1);
            output.append("Losers:");
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.RED)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
        } else if (redPoints > bluePoints) {
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.RED)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
            output.setLength(output.length() - 1);
            output.append("Losers:");
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.BLUE)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
        } else {
            output.setLength(0);
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.BLUE)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
            for (WarlordsEntity player : PlayerFilter.playingGame(game).matchingTeam(Team.RED)) {
                output.append(player.getUuid().toString().replace("-", ""))
                        .append("[")
                        .append(player.getMinuteStats().total().getKills())
                        .append(":")
                        .append(player.getMinuteStats().total().getDeaths())
                        .append("],");
            }
        }
        output.setLength(output.length() - 1);
        if (BotManager.numberOfMessagesSentLast30Sec > 15) {
            if (BotManager.numberOfMessagesSentLast30Sec < 20) {
                BotManager.getTextChannelCompsByName("games-backlog")
                        .ifPresent(textChannel -> textChannel.sendMessage("SOMETHING BROKEN DETECTED <@239929120035700737> <@253971614998331393>").queue());
            }
        } else {
            if (game.getAddons().contains(GameAddon.PRIVATE_GAME)) {
                BotManager.getTextChannelCompsByName("games-backlog").ifPresent(textChannel -> textChannel.sendMessage(output.toString()).queue());
            }
        }
        lastWarlordsPlusString = output.toString();
        return output.toString();
    }

    public static String getLastWarlordsPlusString() {
        return lastWarlordsPlusString;
    }

    @Override
    public String toString() {
        return "DatabaseGameCTF{" +
                "id='" + id + '\'' +
                ", exactDate=" + exactDate +
                ", date='" + date + '\'' +
                ", map=" + map +
                ", gameMode=" + gameMode +
                ", gameAddons=" + gameAddons +
                ", counted=" + counted +
                ", timeLeft=" + timeLeft +
                ", winner=" + winner +
                ", bluePoints=" + bluePoints +
                ", redPoints=" + redPoints +
                ", players=" + players +
                '}';
    }

    @Override
    public void updatePlayerStatsFromGame(DatabaseGameBase databaseGame, int multiplier) {
        for (List<DatabaseGamePlayerCTF> gamePlayerCTFList : players.values()) {
            for (DatabaseGamePlayerCTF gamePlayerCTF : gamePlayerCTFList) {
                DatabaseGameBase.updatePlayerStatsFromTeam(databaseGame, gamePlayerCTF, multiplier);
            }
        }
    }

    @Override
    public DatabaseGamePlayerResult getPlayerGameResult(DatabaseGamePlayerBase player) {
        assert player instanceof DatabaseGamePlayerCTF;

        if (winner == null) {
            return DatabaseGamePlayerResult.DRAW;
        }
        for (Map.Entry<Team, List<DatabaseGamePlayerCTF>> teamListEntry : players.entrySet()) {
            if (teamListEntry.getValue().contains(player)) {
                return teamListEntry.getKey() == winner ? DatabaseGamePlayerResult.WON : DatabaseGamePlayerResult.LOST;
            }
        }
        return DatabaseGamePlayerResult.NONE;
    }

    @Override
    public void createHolograms() {
        List<Hologram> holograms = new ArrayList<>();

        //readding game holograms
        Hologram lastGameStats = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.LAST_GAME_STATS_LOCATION);
        holograms.add(lastGameStats);
        lastGameStats.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Last " + (isPrivate() ? "Comp" : "Pub") + " Game Stats");

        Hologram topDamage = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_DAMAGE_LOCATION);
        holograms.add(topDamage);
        topDamage.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top Damage");

        Hologram topHealing = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_HEALING_LOCATION);
        holograms.add(topHealing);
        topHealing.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top Healing");

        Hologram topAbsorbed = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_ABSORBED_LOCATION);
        holograms.add(topAbsorbed);
        topAbsorbed.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top Absorbed");

        Hologram topDHPPerMinute = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_DHP_PER_MINUTE_LOCATION);
        holograms.add(topDHPPerMinute);
        topDHPPerMinute.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top DHP per Minute");

        Hologram topDamageOnCarrier = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_DAMAGE_ON_CARRIER_LOCATION);
        holograms.add(topDamageOnCarrier);
        topDamageOnCarrier.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top Damage On Carrier");

        Hologram topHealingOnCarrier = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.TOP_HEALING_ON_CARRIER_LOCATION);
        holograms.add(topHealingOnCarrier);
        topHealingOnCarrier.getLines().appendText(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top Healing On Carrier");

        //last game stats
        int minutes = (15 - (int) Math.round(timeLeft / 60.0)) == 0 ? 1 : 15 - (int) Math.round(timeLeft / 60.0);
        lastGameStats.getLines().appendText(ChatColor.GRAY + date);
        lastGameStats.getLines()
                .appendText(ChatColor.GREEN + map.getMapName() + ChatColor.GRAY + "  -  " + ChatColor.GREEN + timeLeft / 60 + ":" + timeLeft % 60 + (timeLeft % 60 < 10 ? "0" : ""));
        lastGameStats.getLines().appendText(ChatColor.BLUE.toString() + bluePoints + ChatColor.GRAY + "  -  " + ChatColor.RED + redPoints);


        List<DatabaseGamePlayerCTF> allPlayers = players.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        HashMap<DatabaseGamePlayerCTF, ChatColor> playerColor = new HashMap<>();
        for (Map.Entry<Team, List<DatabaseGamePlayerCTF>> teamListEntry : players.entrySet()) {
            for (DatabaseGamePlayerCTF gamePlayerCTF : teamListEntry.getValue()) {
                playerColor.put(gamePlayerCTF, teamListEntry.getKey().teamColor);
            }
        }
        List<String> players = new ArrayList<>();

        for (String s : Utils.specsOrdered) {
            StringBuilder playerSpecs = new StringBuilder(ChatColor.AQUA + s).append(": ");
            final boolean[] add = {false};
            allPlayers.stream()
                    .filter(o -> o.getSpec().name.equalsIgnoreCase(s))
                    .forEach(p -> {
                        playerSpecs.append(playerColor.getOrDefault(p, ChatColor.WHITE))
                                .append(p.getName())
                                .append(p.getKDAString())
                                .append(ChatColor.GRAY)
                                .append(", ");
                        add[0] = true;
                    });
            if (add[0]) {
                playerSpecs.setLength(playerSpecs.length() - 2);
                players.add(playerSpecs.toString());
            }
        }
        players.forEach(s -> lastGameStats.getLines().appendText(s));

        //top dmg/healing/absorbed + dhp per game + dmg/heal on carrier
        List<String> topDamagePlayers = new ArrayList<>();
        List<String> topHealingPlayers = new ArrayList<>();
        List<String> topAbsorbedPlayers = new ArrayList<>();
        List<String> topDHPPerGamePlayers = new ArrayList<>();
        List<String> topDamageOnCarrierPlayers = new ArrayList<>();
        List<String> topHealingOnCarrierPlayers = new ArrayList<>();

        Map<ChatColor, Long> totalDamage = new HashMap<>();
        Map<ChatColor, Long> totalHealing = new HashMap<>();
        Map<ChatColor, Long> totalAbsorbed = new HashMap<>();

        allPlayers.stream()
                .sorted(Comparator.comparingLong(DatabaseGamePlayerCTF::getTotalDamage).reversed())
                .forEach(databaseGamePlayer -> {
                    totalDamage.merge(playerColor.get(databaseGamePlayer), databaseGamePlayer.getTotalDamage(), Long::sum);
                    topDamagePlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalDamage()));
                });

        allPlayers.stream()
                .sorted(Comparator.comparingLong(DatabaseGamePlayerCTF::getTotalHealing).reversed())
                .forEach(databaseGamePlayer -> {
                    totalHealing.merge(playerColor.get(databaseGamePlayer), databaseGamePlayer.getTotalHealing(), Long::sum);
                    topHealingPlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalHealing()));
                });

        allPlayers.stream()
                .sorted(Comparator.comparingLong(DatabaseGamePlayerCTF::getTotalAbsorbed).reversed())
                .forEach(databaseGamePlayer -> {
                    totalAbsorbed.merge(playerColor.get(databaseGamePlayer), databaseGamePlayer.getTotalAbsorbed(), Long::sum);
                    topAbsorbedPlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalAbsorbed()));
                });

        allPlayers.stream()
                .sorted((o1, o2) -> {
                    Long p1DHPPerGame = o1.getTotalDHP() / minutes;
                    Long p2DHPPerGame = o2.getTotalDHP() / minutes;
                    return p2DHPPerGame.compareTo(p1DHPPerGame);
                }).forEach(databaseGamePlayer -> {
                    topDHPPerGamePlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalDHP() / minutes));
                });

        allPlayers.stream()
                .sorted(Comparator.comparingLong(DatabaseGamePlayerCTF::getTotalDamageOnCarrier).reversed())
                .forEach(databaseGamePlayer -> {
                    topDamageOnCarrierPlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalDamageOnCarrier()));
                });

        allPlayers.stream()
                .sorted(Comparator.comparingLong(DatabaseGamePlayerCTF::getTotalHealingOnCarrier).reversed())
                .forEach(databaseGamePlayer -> {
                    topHealingOnCarrierPlayers.add(playerColor.get(databaseGamePlayer) + databaseGamePlayer.getName() + ": " +
                            ChatColor.YELLOW + NumberFormat.addCommaAndRound(databaseGamePlayer.getTotalHealingOnCarrier()));
                });

        appendTeamDHP(topDamage, totalDamage);
        appendTeamDHP(topHealing, totalHealing);
        appendTeamDHP(topAbsorbed, totalAbsorbed);

        topDamagePlayers.forEach(s -> topDamage.getLines().appendText(s));
        topHealingPlayers.forEach(s -> topHealing.getLines().appendText(s));
        topAbsorbedPlayers.forEach(s -> topAbsorbed.getLines().appendText(s));
        topDHPPerGamePlayers.forEach(s -> topDHPPerMinute.getLines().appendText(s));
        topDamageOnCarrierPlayers.forEach(s -> topDamageOnCarrier.getLines().appendText(s));
        topHealingOnCarrierPlayers.forEach(s -> topHealingOnCarrier.getLines().appendText(s));

        //setting visibility to none
        holograms.forEach(hologram -> {
            hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        });

        this.holograms = holograms;
    }

    @Override
    public String getGameLabel() {
        return ChatColor.GRAY + date + ChatColor.DARK_GRAY + " - " +
                ChatColor.GREEN + map + ChatColor.DARK_GRAY + " - " +
                ChatColor.GRAY + "(" + ChatColor.BLUE + bluePoints + ChatColor.GRAY + ":" + ChatColor.RED + redPoints + ChatColor.GRAY + ")" + ChatColor.DARK_GRAY + " - " + ChatColor.DARK_PURPLE + isCounted();
    }

    @Override
    public List<String> getExtraLore() {
        return Arrays.asList(
                ChatColor.GRAY + "Time Left: " + ChatColor.GREEN + Utils.formatTimeLeft(timeLeft),
                ChatColor.GRAY + "Winner: " + winner.teamColor + winner.name,
                ChatColor.GRAY + "Blue Points: " + ChatColor.BLUE + bluePoints,
                ChatColor.GRAY + "Red Points: " + ChatColor.RED + redPoints,
                ChatColor.GRAY + "Players: " + ChatColor.YELLOW + players.values().stream().mapToLong(Collection::size).sum()
        );
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public Team getWinner() {
        return winner;
    }

    public void setWinner(Team winner) {
        this.winner = winner;
    }

    public int getBluePoints() {
        return bluePoints;
    }

    public void setBluePoints(int bluePoints) {
        this.bluePoints = bluePoints;
    }

    public int getRedPoints() {
        return redPoints;
    }

    public void setRedPoints(int redPoints) {
        this.redPoints = redPoints;
    }

    public Map<Team, List<DatabaseGamePlayerCTF>> getPlayers() {
        return players;
    }
}
