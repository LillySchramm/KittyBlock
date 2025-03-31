package com.kittyscan.kittyblockspigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.util.ArrayList;
import java.util.List;

public final class KittyBlockSpigot extends JavaPlugin {

    private List<Blocklist> blocklists = new ArrayList<>();
    private boolean debug = false;

    private int obfuscatedResponses = 0;
    private int obfuscatedResponsesCurrent = 0;

    private int reportIntervalMinutes;

    @Override
    public void onEnable() {
        int pluginId = 25306;
        Metrics metrics = new Metrics(this, pluginId);

        this.saveDefaultConfig();

        this.getServer().getPluginManager().registerEvents(
                new Listener() {
                    @EventHandler
                    public void onServerListPing(ServerListPingEvent event) {
                        String ip = event.getAddress().getHostAddress();

                        if (isIpBlocked(ip)) {
                            event.setMaxPlayers(20);
                            event.setMotd("A Minecraft Server");
                            event.setServerIcon(null);
                        }
                    }
                },
                this
        );

        this.debug = this.getConfig().getBoolean("debug");
        this.reportIntervalMinutes = this.getConfig().getInt("reportIntervalMinutes");
        this.getConfig().getMapList("blocklists").forEach((entry) -> {
            String url = (String) entry.get("url");
            int subnet = ((Number) entry.get("subnet")).intValue();
            int refetchMinutes = ((Number) entry.get("refetchMinutes")).intValue();

            this.blocklists.add(new Blocklist(url, subnet, refetchMinutes, this));
        });
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketAdapter adapter = new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handlePacket(event);
            }
        };
        protocolManager.addPacketListener(adapter);

        this.getServer().getScheduler().runTaskTimer(this, () -> {
            getLogger().info("Obfuscated " + this.obfuscatedResponsesCurrent + " responses in the last " + this.reportIntervalMinutes + " minutes");
            this.obfuscatedResponsesCurrent = 0;


            getLogger().info("Total obfuscated responses: " + this.obfuscatedResponses);
        }, 0, (long) this.reportIntervalMinutes * 20 * 60);
    }

    private void handlePacket(PacketEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();

        if (!isIpBlocked(ip)) {
            return;
        }


        WrappedServerPing content  = event.getPacket().getServerPings().read(0);
        content.setPlayersOnline(0);

        try {
            content.setEnforceSecureChat(true);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // Ignore
            // Errors on pre 1.19.1 servers
        }

        content.setPlayers(new ArrayList<>());
        content.setBukkitPlayers(new ArrayList<>());

        String[] version = content.getVersionName().split(" ");
        content.setVersionName(version[version.length-1]);

        event.getPacket().getServerPings().write(0, content);


        if (this.debug) {
            getLogger().info("Blocked packet from " + ip);
        }

        this.obfuscatedResponsesCurrent++;
        this.obfuscatedResponses++;
    }

    private boolean isIpBlocked(String ip) {
        return blocklists.stream().anyMatch(blocklist -> blocklist.isIpBlocked(ip));
    }

    @Override
    public void onDisable() {}
}
