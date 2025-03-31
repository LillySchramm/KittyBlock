package com.kittyscan.kittyblockspigot;

import inet.ipaddr.IPAddressString;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Blocklist {
    private final String url;
    private final int subnet;
    private final int refetchMinutes;

    private final JavaPlugin plugin;

    private List<IPAddressString> ips = new ArrayList<>();

    public Blocklist(String url, int subnet, int refetchMinutes, JavaPlugin plugin) {
        this.url = url;
        this.subnet = subnet;
        this.refetchMinutes = refetchMinutes;
        this.plugin = plugin;

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateIps, 0, (long) refetchMinutes * 60 * 20);
    }

    public boolean isIpBlocked(String ip) {
        IPAddressString ipAddress = new IPAddressString(ip);
        return ips.stream().anyMatch(ipAddress::contains);
    }

    private void updateIps() {
        try {
            List<String> ips = fetchIps();
            this.ips = ips.stream().map(s -> new IPAddressString(s + "/" + this.subnet)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> fetchIps() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        con.getInputStream()
                )
        );

        List<String> ips = new ArrayList<>();

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            ips.add(inputLine);
        }
        in.close();

        return ips;
    }
}
