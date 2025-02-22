package com.ebicep.warlords.game.option;

import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.marker.TeamMarker;
import com.ebicep.warlords.game.option.marker.scoreboard.ScoreboardHandler;
import com.ebicep.warlords.game.option.marker.scoreboard.SimpleScoreboardHandler;
import com.ebicep.warlords.game.state.EndState;
import com.ebicep.warlords.game.state.PlayingState;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.poll.polls.GamePoll;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.chat.ChatUtils;
import com.ebicep.warlords.util.warlords.GameRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ImposterModeOption implements Option {

    public static int NUMBER_OF_IMPOSTERS_PER_TEAM = 1;
    private final HashMap<Team, List<UUID>> imposters = new HashMap<>();
    private final HashMap<Team, List<UUID>> voters = new HashMap<>();
    private Game game;
    private GamePoll poll;
    private boolean forceEnd = false;


    @Override
    public void register(@Nonnull Game game) {
        this.game = game;

        for (Team team : TeamMarker.getTeams(game)) {
            imposters.put(team, new ArrayList<>());
        }

        game.registerGameMarker(ScoreboardHandler.class,
                new SimpleScoreboardHandler(Integer.MAX_VALUE - 15, "imposter") {
                    @Override
                    public List<String> computeLines(@Nullable WarlordsPlayer warlordsPlayer) {
                        if (warlordsPlayer == null) {
                            return Collections.singletonList("");
                        }
                        if (imposters.get(warlordsPlayer.getTeam()).isEmpty()) {
                            return Collections.singletonList("");
                        }
                        if (imposters.entrySet()
                                .stream()
                                .anyMatch(teamListEntry -> teamListEntry.getValue()
                                        .contains(warlordsPlayer.getUuid()))) {
                            return Collections.singletonList(ChatColor.WHITE + "Role: " + ChatColor.RED + "Imposter");
                        }

                        return Collections.singletonList(ChatColor.WHITE + "Role: " + ChatColor.GREEN + "Innocent");
                    }

                    @Override
                    public boolean emptyLinesBetween() {
                        return false;
                    }
                }
        );
    }


    @Override
    public void start(@Nonnull Game game) {
        new GameRunnable(game) {

            int counter = 0;

            @Override
            public void run() {
                if (counter == 4) {
                    assignImposters(game);
                }
                game.onlinePlayersWithoutSpectators().forEach(playerTeamEntry -> {
                    Player player = playerTeamEntry.getKey();
                    Team team = playerTeamEntry.getValue();
                    String title = "";
                    switch (counter) {
                        case 0:
                            title = ChatColor.GREEN + "3";
                            break;
                        case 1:
                            title = ChatColor.YELLOW + "2";
                            break;
                        case 2:
                            title = ChatColor.RED + "1";
                            break;
                        case 3:
                            title = ChatColor.YELLOW + "You are...";
                            break;
                        case 4:
                            List<UUID> imposterUUIDs = imposters.get(team);
                            if (imposterUUIDs.contains(player.getUniqueId())) {
                                title = ChatColor.RED + "The IMPOSTER";
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.RED + "You are the IMPOSTER",
                                        ChatColor.BLUE,
                                        true
                                );
                                if (imposterUUIDs.size() > 1) {
                                    List<UUID> otherImposters = new ArrayList<>(imposterUUIDs);
                                    otherImposters.remove(player.getUniqueId());
                                    ChatUtils.sendMessageToPlayer(player,
                                            ChatColor.GRAY + "Other imposters: " + ChatColor.RED + otherImposters.stream()
                                                    .map(Bukkit::getPlayer)
                                                    .filter(Objects::nonNull)
                                                    .map(Player::getName)
                                                    .collect(Collectors.joining(ChatColor.GRAY + ", " + ChatColor.RED)),
                                            ChatColor.BLUE,
                                            true
                                    );
                                }
                            } else {
                                title = ChatColor.GREEN + "INNOCENT";
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.GREEN + "You are INNOCENT",
                                        ChatColor.BLUE,
                                        true
                                );
                            }
                            break;
                    }
                    PacketUtils.sendTitle(player, title, "", 0, 100, 40);
                });
                counter++;
                if (counter == 5) {
                    this.cancel();
                }
            }
        }.runTaskTimer(20 * 12, 20);
    }

    @Override
    public void onGameEnding(@Nonnull Game game) {
        if (!forceEnd) {
            game.getState(EndState.class).ifPresent(endState -> {
                Team winner = endState.getWinEvent().getDeclaredWinner();
                if (winner != null) {
                    game.onlinePlayersWithoutSpectators().forEach(playerTeamEntry -> {
                        Player player = playerTeamEntry.getKey();
                        Team team = playerTeamEntry.getValue();
                        //winners - imposter lose
                        //losers - imposter win
                        if (team == winner) {
                            if (imposters.get(team).stream().anyMatch(uuid -> uuid.equals(player.getUniqueId()))) {
                                PacketUtils.sendTitle(player, ChatColor.RED + "YOU LOST!", "", 0, 300, 40);
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.RED + "You lost!",
                                        ChatColor.BLUE,
                                        true
                                );
                            } else {
                                PacketUtils.sendTitle(player, ChatColor.GREEN + "YOU WON!", "", 0, 300, 40);
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.GREEN + "You won!",
                                        ChatColor.BLUE,
                                        true
                                );
                            }
                        } else {
                            boolean isAnImposterOnOtherTeam = false;
                            for (Map.Entry<Team, List<UUID>> teamListEntry : imposters.entrySet().stream()
                                    .filter(teamListEntry -> teamListEntry.getKey() != winner)
                                    .collect(Collectors.toList())
                            ) {
                                for (UUID uuid : teamListEntry.getValue()) {
                                    if (uuid.equals(player.getUniqueId())) {
                                        isAnImposterOnOtherTeam = true;
                                        break;
                                    }
                                }
                            }
                            if (isAnImposterOnOtherTeam) {
                                PacketUtils.sendTitle(player, ChatColor.GREEN + "YOU WON!", "", 0, 300, 40);
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.GREEN + "You won!",
                                        ChatColor.BLUE,
                                        true
                                );
                            } else {
                                PacketUtils.sendTitle(player, ChatColor.RED + "YOU LOST!", "", 0, 300, 40);
                                ChatUtils.sendMessageToPlayer(player,
                                        ChatColor.RED + "You lost!",
                                        ChatColor.BLUE,
                                        true
                                );
                            }
                        }
                        sendImpostorResult(player);
                    });
                }
            });
        }
    }

    private void sendImpostorResult(Player player) {
        StringBuilder message = new StringBuilder();
        imposters.forEach((team, warlordsPlayers) -> {
            if (warlordsPlayers.size() == 1) {
                message.append(ChatColor.GREEN)
                        .append("The ")
                        .append(team.teamColor)
                        .append(team.name)
                        .append(ChatColor.GREEN)
                        .append(" imposter was ")
                        .append(ChatColor.AQUA)
                        .append(warlordsPlayers.stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .collect(Collectors.joining(", ")))
                        .append("\n");
            } else if (warlordsPlayers.size() > 1) {
                message.append(ChatColor.GREEN)
                        .append("The ")
                        .append(team.teamColor)
                        .append(team.name)
                        .append(ChatColor.GREEN)
                        .append(" imposters were ")
                        .append(ChatColor.AQUA)
                        .append(warlordsPlayers.stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .collect(Collectors.joining(", ")))
                        .append("\n");
            }
        });
        message.setLength(message.length() - 1);
        ChatUtils.sendMessageToPlayer(
                player,
                message.toString(),
                ChatColor.BLUE, true
        );
    }

    public void assignImposters(Game game) {
        for (Team team : TeamMarker.getTeams(game)) {
            List<WarlordsPlayer> teamPlayers = game.warlordsPlayers()
                    .filter(warlordsPlayer -> warlordsPlayer.getTeam() == team)
                    .collect(Collectors.toList());
            if (teamPlayers.size() == 0) {
                continue;
            }
            Collections.shuffle(teamPlayers);
            for (int i = 0; i < NUMBER_OF_IMPOSTERS_PER_TEAM; i++) {
                imposters.get(team).add(teamPlayers.get(i).getUuid());
            }
        }
        System.out.println(" --- Assigned Imposters --- ");
        for (Team team : TeamMarker.getTeams(game)) {
            System.out.println(team.name + " - " + imposters.get(team)
                    .stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                    .collect(Collectors.joining(", ")));
        }
    }

    public void sendPoll(Team team) {
        poll = new GamePoll.Builder(game)
                .setQuestion("Who is the most SUS on your team?")
                .setTimeLeft(60)
                .setOptions(game.offlinePlayersWithoutSpectators()
                        .filter(uuidTeamEntry -> uuidTeamEntry.getValue() == team)
                        .map(offlinePlayerTeamEntry -> offlinePlayerTeamEntry.getKey().getName())
                        .collect(Collectors.toList()))
                .setExcludedPlayers(game.offlinePlayersWithoutSpectators()
                        .filter(uuidTeamEntry -> uuidTeamEntry.getValue() != team)
                        .map(offlinePlayerTeamEntry -> offlinePlayerTeamEntry.getKey()
                                .getUniqueId())
                        .collect(Collectors.toList()))
                .setRunnableAfterPollEnded(p -> {
                    forceEnd = true;
                    int mostVotes = Collections.max(p.getOptionsWithVotes().entrySet(),
                            Comparator.comparingInt(Map.Entry::getValue)
                    ).getValue();
                    List<WarlordsPlayer> votedOut = p.getOptionsWithVotes().entrySet().stream()
                            .filter(stringIntegerEntry -> stringIntegerEntry.getValue() == mostVotes)
                            .map(Map.Entry::getKey)
                            .map(s -> game.warlordsPlayers()
                                    .filter(warlordsPlayer -> warlordsPlayer.getName()
                                            .equals(s))
                                    .findFirst()
                                    .get())
                            .collect(Collectors.toList());
                    //If multiple top votes and one is imposter then voted wrong
                    boolean votedCorrectly = votedOut.size() == 1 && imposters.get(team)
                            .stream()
                            .anyMatch(uuid -> uuid == votedOut.get(0).getUuid());

                    new GameRunnable(game, true) {
                        int counter = 0;

                        @Override
                        public void run() {
                            String title = "";
                            String subtitle = "";
                            switch (counter) {
                                case 0:
                                case 1:
                                    title = team.teamColor + team.name + " voted...";
                                    break;
                                case 2:
                                case 3:
                                    if (votedCorrectly) {
                                        title = ChatColor.GREEN + "Correctly!";
                                    } else {
                                        title = ChatColor.RED + "Incorrectly!";
                                    }
                                    subtitle = team.teamColor + Bukkit.getOfflinePlayer(imposters.get(team).get(0))
                                            .getName() + ChatColor.YELLOW + " was the imposter";
                                    break;
                            }
                            counter++;
                            if (counter < 6) {
                                sendTitle(title, subtitle);
                            } else if (counter == 6) {
                                game.onlinePlayersWithoutSpectators()
                                        .forEach(playerTeamEntry -> {
                                            Player player = playerTeamEntry.getKey();
                                            player.removePotionEffect(PotionEffectType.BLINDNESS);
                                            showWinLossMessage(team,
                                                    player,
                                                    votedCorrectly,
                                                    playerTeamEntry.getValue() == team
                                            );
                                        });
                            } else if (counter == 9) {
                                game.removeFrozenCause(team.teamColor + team.name + ChatColor.GREEN + " is voting!");
                                int scoreNeededToEndGame = game.getOptions()
                                        .stream()
                                        .filter(e -> e instanceof WinByPointsOption)
                                        .mapToInt(e -> ((WinByPointsOption) e).getPointLimit())
                                        .sorted()
                                        .findFirst()
                                        .orElse(Integer.MAX_VALUE);
                                if (votedCorrectly) {
                                    game.setPoints(team, scoreNeededToEndGame);
                                } else {
                                    //team with most points win, excluding voting team
                                    game.setPoints(TeamMarker.getTeams(game).stream()
                                                    .filter(t -> t != team)
                                                    .min(Comparator.comparingInt(o -> game.getPoints(o)))
                                                    .get(),
                                            scoreNeededToEndGame
                                    );
                                }
                                ((PlayingState) game.getState()).skipTimer();
                                game.onlinePlayers()
                                        .forEach(playerTeamEntry -> {
                                            Player player = playerTeamEntry.getKey();
                                            sendImpostorResult(player);
                                        });
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(10, 20);
                }).get();
    }

    private void sendTitle(String title, String subtitle) {
        game.onlinePlayers()
                .forEach(playerTeamEntry -> PacketUtils.sendTitle(playerTeamEntry.getKey(),
                        title,
                        subtitle,
                        0,
                        150,
                        40
                ));
    }

    private void showWinLossMessage(Team team, Player player, boolean votedCorrectly, boolean sameTeam) {
        if (sameTeam) {
            //win if
            //voted imposter out and player isnt the imposter
            //or didnt vote imposter and player is the imposter
            if (
                    (votedCorrectly && imposters.get(team)
                            .stream()
                            .noneMatch(uuid -> uuid.equals(player.getUniqueId()))) ||
                            (!votedCorrectly && imposters.get(team)
                                    .stream()
                                    .anyMatch(uuid -> uuid.equals(player.getUniqueId())))
            ) {
                PacketUtils.sendTitle(player, ChatColor.GREEN + "YOU WON!", "", 0, 300, 40);
                ChatUtils.sendMessageToPlayer(player, ChatColor.GREEN + "You won!", ChatColor.BLUE, true);
            } else {
                PacketUtils.sendTitle(player, ChatColor.RED + "YOU LOST!", "", 0, 300, 40);
                ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "You lost!", ChatColor.BLUE, true);
            }
        } else {
            //win if
            //other team votes wrong imposter
            //or other team votes the right imposter then the imposter wins
            boolean isAnImposterOnOtherTeam = false;
            for (Map.Entry<Team, List<UUID>> teamListEntry : imposters.entrySet().stream()
                    .filter(teamListEntry -> teamListEntry.getKey() != team)
                    .collect(Collectors.toList())
            ) {
                for (UUID uuid : teamListEntry.getValue()) {
                    if (uuid.equals(player.getUniqueId())) {
                        isAnImposterOnOtherTeam = true;
                        break;
                    }
                }
            }
            if (votedCorrectly && !isAnImposterOnOtherTeam) {
                PacketUtils.sendTitle(player, ChatColor.RED + "YOU LOST!", "", 0, 300, 40);
                ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "You lost!", ChatColor.BLUE, true);
            } else {
                PacketUtils.sendTitle(player, ChatColor.GREEN + "YOU WON!", "", 0, 300, 40);
                ChatUtils.sendMessageToPlayer(player, ChatColor.GREEN + "You won!", ChatColor.BLUE, true);
            }
        }
    }

    public int getNumberOfImpostersPerTeam() {
        return NUMBER_OF_IMPOSTERS_PER_TEAM;
    }

    public HashMap<Team, List<UUID>> getImposters() {
        return imposters;
    }

    public HashMap<Team, List<UUID>> getVoters() {
        return voters;
    }

    public GamePoll getPoll() {
        return poll;
    }

    public void setPoll(GamePoll poll) {
        this.poll = poll;
    }
}
