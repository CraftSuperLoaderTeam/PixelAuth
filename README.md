# PixelAuth

Easy account verification server for Minecraft
<br>
适用于Minecraft的简易账号验证服务器

## Warn

* The server does not have the logic to handle any Minecraft in-game.
* The server is not compatible with Bukkit-based plugins
* Players who enter the server will be kicked immediately, but you can implement features such as account verification before the player is kicked out

## Network requests

This server performs network requests to:

* https://api.mojang.com - Check if the player has a Microsoft account
* https://sessionserver.mojang.com - verify if the player is the owner of that account

## Update

* `0.0.1` Network Framework
* `0.0.2` Player online mode