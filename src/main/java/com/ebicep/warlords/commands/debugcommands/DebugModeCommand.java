package com.ebicep.warlords.commands.debugcommands;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.commands.BaseCommand;
import com.ebicep.warlords.player.WarlordsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DebugModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        WarlordsPlayer warlordsPlayer = BaseCommand.requireWarlordsPlayer(sender);

        if(warlordsPlayer != null) {
            warlordsPlayer.setInfiniteEnergy(true);
            warlordsPlayer.setDisableCooldowns(true);
            warlordsPlayer.setTakeDamage(false);
            warlordsPlayer.sendMessage(ChatColor.GREEN + "You now have infinite energy, no cooldowns, and take no damage!");
        } else {
            sender.sendMessage(ChatColor.RED + "You must be in game to use this command!");
        }

        return true;
    }

    public void register(Warlords instance) {
        instance.getCommand("debugmode").setExecutor(this);
    }
}