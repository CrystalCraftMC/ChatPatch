
package com.gmail.jacc734dev.chatpatch;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * CensorListener.java
 * 
 * @version 2018.02.04.v1
 */
public class CensorListener implements Listener {

	private final ChatPatch plugin;

	public CensorListener(ChatPatch instance) {
		plugin = instance;
	}

	@EventHandler
	public void pLogin(PlayerJoinEvent event) {
		String uuid = event.getPlayer().getUniqueId().toString();
		if (plugin.censorID.contains(uuid)) {
			plugin.censorMailList.put(event.getPlayer(), true);
		}
	}

	@EventHandler
	public void pQuit(PlayerQuitEvent event) {
		plugin.censorMailList.remove(event.getPlayer());// returns null if player doesn't exists
	}
}