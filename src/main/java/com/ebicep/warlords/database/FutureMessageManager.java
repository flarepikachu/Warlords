package com.ebicep.warlords.database;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.util.Utils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.*;

public class FutureMessageManager implements Listener {

    public static MongoCollection<Document> futureMessages;

    public static void init() {
        futureMessages = DatabaseManager.warlordsPlayersDatabase.getCollection("Future_Messages");
    }

    public static void addNewFutureMessageDocument(UUID uuid, boolean centered, String... messages) {
        Document previousDocument = getPlayerDocument(uuid);
        if (previousDocument != null) {
            futureMessages.updateOne(eq("uuid", uuid.toString()), Updates.pushEach("messages", Arrays.stream(messages).collect(Collectors.toList())));
        } else {
            futureMessages.insertOne(new Document("uuid", uuid.toString())
                    .append("centered", centered)
                    .append("messages", Arrays.stream(messages).collect(Collectors.toList())));
        }
    }

    public static void editFutureMessage(UUID uuid, boolean centered, String... newMessages) {
        if (getPlayerDocument(uuid) != null) {
            futureMessages.updateOne(eq("uuid", uuid.toString()),
                    new Document("$set", new Document("centered", centered)
                            .append("messages", Arrays.stream(newMessages).collect(Collectors.toList())))
            );
        }
    }

    public static Document getPlayerDocument(UUID uuid) {
        return futureMessages.find().filter(eq("uuid", uuid.toString())).first();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Document playerDocument = getPlayerDocument(player.getUniqueId());
        if (playerDocument != null) {
            boolean centered = playerDocument.getBoolean("centered");
            Warlords.newChain()
                    .delay(20)
                    .sync(() -> {
                        List<String> messages = playerDocument.getList("messages", String.class);
                        messages.forEach(m -> {
                            if(centered) {
                                Utils.sendCenteredMessage(player, m);
                            } else {
                                player.sendMessage(m);
                            }
                        });
                    })
                    .async(() -> futureMessages.deleteOne(eq("uuid", player.getUniqueId().toString())))
                    .execute();

        }
    }
}
