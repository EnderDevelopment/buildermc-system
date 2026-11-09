# Buildermc System Minecraft Plugin

Buildermc System is a Minecraft plugin for Paper servers that provides spawn management, player vanish, player reports, temporary player bans, and temporary IP bans through simple moderation and server-management commands.

## Install

1. Make sure your server runs Paper 1.21.11 or a compatible 1.21 server.
2. Make sure the server uses Java 21 or newer.
3. Place the Buildermc System plugin JAR file in the server's `plugins` folder.
4. Start or restart the server.
5. Set the server spawn with `/setspawn`.

## Use

### Spawn Commands

`/setspawn`

Sets the server spawn at your current location. Requires the `buildermc.admin` permission.

`/spawn`

Teleports you to the configured server spawn. The spawn must be set first.

### Vanish

`/vanish`

Toggles your visibility to other players. Requires the `buildermc.vanish` permission.

Players with the `buildermc.vanish` permission can see vanished players. Vanish status is stored in memory and is reset when the plugin or server restarts.

### Reports

`/report <player> <reason>`

Sends a player report to online staff members with the `buildermc.report.notify` permission and records the report in the server log.

### Temporary Bans

`/tempban <player> <duration> [reason]`

Temporarily bans a player by name. Requires the `buildermc.tempban` permission.

`/tempbanip <player> <duration> [reason]`

Temporarily bans the IP address of an online player. Requires the `buildermc.tempbanip` permission.

Supported duration formats include:

- `30s` for seconds
- `15m` for minutes
- `2h` for hours
- `7d` for days

Default permissions are assigned to server operators unless changed in your permissions manager.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/becae189-0e46-4ff0-93fa-0323748f4d73?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=becae189-0e46-4ff0-93fa-0323748f4d73
