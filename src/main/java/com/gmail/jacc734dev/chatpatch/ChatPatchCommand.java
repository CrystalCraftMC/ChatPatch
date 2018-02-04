package com.gmail.jacc734dev.chatpatch;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * ChatPatchCommand.java
 * This handles the commands.

 * @version 2018.02.04.v1
 */

public class ChatPatchCommand implements CommandExecutor {

	private final ChatPatch plugin;

	public ChatPatchCommand(ChatPatch instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if(args.length != 1) return false;
		if (args[0].equalsIgnoreCase("save") && sender.hasPermission("chatpatch.save")) {
			plugin.getConfig().set("censored", plugin.censorID);
			plugin.saveConfig();
			plugin.reloadConfig();
			plugin.getLogger().info("Saved.");
			return true;
		} else if (args[0].equalsIgnoreCase("defaults") && sender.hasPermission("chatpatch.defaults")) {
		    File configFile = new File(plugin.getDataFolder(), "config.yml");
	        plugin.saveResource("config.yml", true);
			plugin.getLogger().info("Defaults restored.");
		    return true;
		}

		if (!(sender instanceof Player)) return false; //these commands are for players only
		Player p = (Player) sender;
		String uuid = p.getUniqueId().toString();

		if (args[0].equalsIgnoreCase("on") && p.hasPermission("chatpatch.on")) {
			plugin.censorID.add(uuid);
			plugin.censorMailList.put(p, true);
			p.sendMessage("ChatPatch Enabled.");
			return true;
		} else if (args[0].equalsIgnoreCase("off") && p.hasPermission("chatpatch.off")) {
			plugin.censorID.remove(uuid);//no checks needed, returns null if non-existing
			plugin.censorMailList.remove(p);
			p.sendMessage("ChatPatch Disabled.");
			return true;
		}

		return false;
	}
}
