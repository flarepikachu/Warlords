package com.ebicep.warlords.party;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StreamCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!sender.isOp()) {
            sender.sendMessage("§cYou do not have permission to do that.");
            return true;
        }

        if (Warlords.partyManager.inAParty(((Player) sender).getUniqueId())) {
            Party.sendMessageToPlayer((Player) sender, ChatColor.RED + "You are already in a party", true, true);
            return true;
        }

        if (args.length == 0) {
            Party party = new Party(((Player) sender).getUniqueId(), true);
            Warlords.partyManager.getParties().add(party);
            party.sendMessageToAllPartyPlayers(ChatColor.GREEN + "You created a public party! Players can join with\n" +
                    ChatColor.GOLD + ChatColor.BOLD + "/party join " + sender.getName(),
                    true,
                    true);
        }

        return true;
    }

    public void register(Warlords instance) {
        instance.getCommand("stream").setExecutor(this);
    }

}
