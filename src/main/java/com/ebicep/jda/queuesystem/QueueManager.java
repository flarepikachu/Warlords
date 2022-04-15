package com.ebicep.jda.queuesystem;

import com.ebicep.jda.BotManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class QueueManager {

    public static final List<UUID> queue = new ArrayList<>();
    public static final List<FutureQueuePlayer> futureQueue = new ArrayList<>();
    public static Message queueMessage = null;

    public static void sendNewQueue() {
        try {
            if (queueMessage != null) {
                queueMessage.delete().queue();
            }
        } catch (Exception e) {
            System.out.println("[QueueManager] Error while deleting queue message");
        }
        BotManager.getTextChannelCompsByName("waiting").ifPresent(textChannel -> {
            textChannel.sendMessageEmbeds(QueueManager.getQueueDiscord()).queueAfter(1, TimeUnit.SECONDS, message -> queueMessage = message);
        });
    }

    public static String getQueue() {
        StringBuilder stringBuilder = new StringBuilder(ChatColor.GREEN + "Queue -\n");
        for (int i = 0; i < queue.size(); i++) {
            UUID uuid = queue.get(i);
            stringBuilder.append("    ").append(ChatColor.YELLOW).append(i + 1).append(". ").append(ChatColor.AQUA).append(Bukkit.getOfflinePlayer(uuid).getName()).append("\n");
        }
        if (!futureQueue.isEmpty()) {
            stringBuilder.append("\n");
            stringBuilder.append(ChatColor.GREEN).append("Future Queue -\n");
            futureQueue.forEach(futureQueuePlayer -> {
                stringBuilder.append("    ").append(ChatColor.YELLOW).append("- ").append(ChatColor.AQUA).append(Bukkit.getOfflinePlayer(futureQueuePlayer.getUuid()).getName()).append(ChatColor.GRAY).append(" (").append(futureQueuePlayer.getTimeString()).append(")").append("\n");
            });
        }
        return stringBuilder.toString();
    }

    public static MessageEmbed getQueueDiscord() {
        StringBuilder queue = new StringBuilder();
        for (int i = 0; i < QueueManager.queue.size(); i++) {
            UUID uuid = QueueManager.queue.get(i);
            queue.append("    ").append(i + 1).append(". ").append(Bukkit.getOfflinePlayer(uuid).getName()).append("\n");
        }
        StringBuilder futureQueue = new StringBuilder();
        QueueManager.futureQueue.forEach(futureQueuePlayer -> {
            futureQueue.append("    ").append("- ").append(Bukkit.getOfflinePlayer(futureQueuePlayer.getUuid()).getName()).append(" (").append(futureQueuePlayer.getTimeString()).append(")").append("\n");
        });

        return new EmbedBuilder()
                .setColor(3066993)
                .setTimestamp(new Date().toInstant())
                .addField("Current Queue", queue.toString(), true)
                .addField("\u200B", "\u200B", true)
                .addField("Future Queue", futureQueue.toString(), true)
                .setFooter("Usage: /queue")
                .build();
    }

    public static void addPlayerToQueue(String name, boolean atBeginning) {
        addPlayerToQueue(Bukkit.getOfflinePlayer(name).getUniqueId(), atBeginning);
    }

    public static void addPlayerToQueue(UUID uuid, boolean atBeginning) {
        queue.remove(uuid);
        if (atBeginning) {
            queue.add(0, uuid);
        } else {
            queue.add(uuid);
        }
    }

    public static void removePlayerFromQueue(String name) {
        removePlayerFromQueue(Bukkit.getOfflinePlayer(name).getUniqueId());
    }

    public static void removePlayerFromQueue(UUID uuid) {
        queue.remove(uuid);
        removePlayerFromFutureQueue(uuid);
    }

    public static void addPlayerToFutureQueue(String name, String timeString, BukkitTask task) {
        addPlayerToFutureQueue(Bukkit.getOfflinePlayer(name).getUniqueId(), timeString, task);
    }

    public static void addPlayerToFutureQueue(UUID uuid, String timeString, BukkitTask task) {
        if (futureQueue.stream().noneMatch(futureQueuePlayer -> futureQueuePlayer.getUuid().equals(uuid))) {
            futureQueue.add(new FutureQueuePlayer(uuid, timeString, task));
        }
    }

    public static void removePlayerFromFutureQueue(String name) {
        removePlayerFromFutureQueue(Bukkit.getOfflinePlayer(name).getUniqueId());
    }

    public static void removePlayerFromFutureQueue(UUID uuid) {
        futureQueue.removeIf(futureQueuePlayer -> {
            if (futureQueuePlayer.getUuid().equals(uuid)) {
                futureQueuePlayer.getTask().cancel();
                return true;
            }
            return false;
        });
    }


    static public class FutureQueuePlayer {
        private final UUID uuid;
        private final String timeString;
        private final BukkitTask task;

        public FutureQueuePlayer(UUID uuid, String timeString, BukkitTask task) {
            this.uuid = uuid;
            this.timeString = timeString;
            this.task = task;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getTimeString() {
            return timeString;
        }

        public BukkitTask getTask() {
            return task;
        }
    }
}
