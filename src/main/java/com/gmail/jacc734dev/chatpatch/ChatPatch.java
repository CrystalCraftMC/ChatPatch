/*
Copyright 2018 Jacc734 <Jacc734.dev@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.gmail.jacc734dev.chatpatch;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.List;
/**
 * ChatPatch.java
 * <p>
 * This class is the core, and handles the censoring of messages for players who opt for it.
 * The plugin takes out going packets instead of incoming packets so each player can specify if they want to be censored.
 * </p>
 * @author Jacc734.dev@gmail.com
 * @version 2018.02.04.v1
 */

public class ChatPatch extends JavaPlugin implements Listener 
{

	private JSONParser parser;//TODO future work, incase usernames aren't checked or parsing method changes for CrystalCraft
	private ProtocolManager protocolManager;
	private PluginManager pluginManager;
	private final CensorListener censorListener = new CensorListener(this);

	public final HashMap<Player, Boolean> censorMailList = new HashMap<Player, Boolean>();
	public final List<String> censorID = getConfig().getStringList("censored");

	public void onEnable() {
		this.saveDefaultConfig();

		// Initialize Managers
		protocolManager = ProtocolLibrary.getProtocolManager();
		pluginManager = getServer().getPluginManager();
		parser = new JSONParser();
		
		// Register Listeners
		pluginManager.registerEvents(censorListener, this);
		censorPackets();

		// Check for censor users, theta(NumOnline*NumCensorUsers) isn't that bad given onEable()
		// runs once
		for (Player p : getServer().getOnlinePlayers()) {
			String uuid = p.getUniqueId().toString();
			if (censorID.contains(uuid)) {
				censorMailList.put(p, true);
			}
		}

		// CommandExecutor
		getCommand("chatpatch").setExecutor(new ChatPatchCommand(this));

		getLogger().info("Enabled on Server.");

	}

	public void onDisable() {
		getConfig().set("censored", censorID);
		this.saveConfig();
		getLogger().info("Disabled on Server.");

	}

	/*
	 * Adds the packet listener. Once a packet enters, it is checked for CHAT
	 * type. Then the player the packet will be sent to is checked if he opt'd
	 * for censorship, if no packet sends. If yes, any words in config are
	 * replaced with '.'.
	 */
	private void censorPackets() 
	{
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) 
		{
					@Override
					public void onPacketSending(PacketEvent event)
					{
						//getLogger().info("DEBUG: Play.Server.CHAT  - onSend");
						if (event.getPlayer() == null) 
						{
							return;
						}
						//getLogger().info("DEBUG: censorMailList.contains" + censorMailList.containsKey(event.getPlayer()));
						if (censorMailList.containsKey(event.getPlayer())) 
						{
							boolean mkNewPacket = false;
							String msg = null;
							//Packet to String
							try 
							{
								 msg = event.getPacket().getChatComponents().read(0).getJson();
							} catch (FieldAccessException e) 
							{
								getLogger().info("DEBUG: FieldAccessException");
								return;
							}
							if(msg == null) return;
							
							//Censoring
							//getLogger().info("DEBUG: String-msg (before censor) " +msg);
							msg = msg.toLowerCase();
							List<String> filter = getConfig().getStringList("filter");
							for (String s : filter) 
							{
								if (msg.contains(s)) 
								{
									msg = msg.replaceAll(s, ".");
									mkNewPacket = true;//now we know we need to make a new packet
								}
							}		
							if(mkNewPacket) 
							{
								WrappedChatComponent tmp = WrappedChatComponent.fromJson(msg);
								event.getPacket().getChatComponents().write(0, tmp);
							}
						}
					}
				});
	}

}