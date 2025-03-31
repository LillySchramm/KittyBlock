# KittyBlock

Reducing the amount of data known server scanners can gather.

## Features

- **Obfuscation**: KittyBlock obfuscates the query data visible to the scanner, making it look like a default vanilla server ("A Minecraft Server", 20 Max Players, No Online Players, ...).
- **Preconfigured**: KittyBlock is preconfigured to use the [KittyScan Blocklist](https://github.com/LillySchramm/KittyScanBlocklist) by default, which is a blocklist, gathered by the KittyScan Project, of known scanners and bots. You can add your own blocklist or use the default one.
- **Wide Compatibility**: KittyBlock is compatible with all Minecraft Spigot/Paper versions from `1.12.0` to `1.21.4`.

### Installation / Setup

#### Spigot/Paper

> The Plugin needs `ProtocolLib` to work.

1. Download the latest version of [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) and place it in your `plugins` folder.
2. Download the latest version of [KittyBlock](https://github.com/LillySchramm/KittyBlock/releases) and place it in your `plugins` folder.
3. Restart your server.
4. (Optional) Configure the plugin in `plugins/KittyBlock/config.yml`.
5. Done!

```yaml
blocklists: # List of blocklists to use for obfuscation, should not be empty, preconfigured to use the KittyScan Blocklist /24 by default
  - url: https://raw.githubusercontent.com/LillySchramm/KittyScanBlocklist/refs/heads/main/ips-24.txt # URL to the blocklist (text file, one IP per line, without CIDR notation)
    subnet: 24 # CIDR notation of the blocklist
    refetchMinutes: 60 # How often to refetch the blocklist, in minutes (60 = every hour, 1440 = every day, ...)
debug: false # Whether to enable debug mode, should only be enabled for debug purposes (true = enabled, false = disabled)
reportIntervalMinutes: 60 # How often to report the statistics in the server logs, in minutes (60 = every hour, 1440 = every day, ...)
```