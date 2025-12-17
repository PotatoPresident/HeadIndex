# HeadIndex
Head Index is an easy-to-use server-side head database mod for the Fabric Loader. 
Head Index provides easy access to thousands of heads provided by minecraft-heads.com. 
Unlike a plugin, Head Index can also be used in singleplayer without the need for a server. 
New heads are added all the time and will be automatically added whenever you restart the server without needing to update. 
Head Index also supports setting an item, xp, or economy (Common Economy API) cost for survival servers.

Head Index is powered by [Minecraft Heads](https://minecraft-heads.com).
To use the mod, you must enter a free [license key](https://minecraft-heads.com/wiki/minecraft-heads/api-v2-for-users) into the config file.

# Usage
### Commands

- `/head` - Opens head GUI

- `/head menu` - Also opens head GUI

- `/head search <search>` - Opens search menu

- `/head player <playername>` - Get a specific player head

### Config
`config/head-index.json`
```json
{
  "_licenseComment": "Enter your license key below. Register here: https://minecraft-heads.com/wiki/minecraft-heads/api-v2-for-users",
  "license": "LICENSE_HERE",
  "_permissionComment": "The default permission level for the commands. Set to 0 to allow all players access",
  "permissionLevel": 2,
  "_economyComment": "The type of economy to use. Set to FREE to disable economy, ITEM to use an item, TAG to use a tag, ECONOMY to use an economy currency, LEVEL to use minecraft levels, or LEVELPOINTS to use level points",
  "economyType": "FREE",
  "_costComment": "The identifier for the item, tag or currency to use for the cost, only needed if economyType is set to ITEM, TAG, or ECONOMY",
  "costType": "minecraft:diamond",
  "_costAmountComment": "The amount of the item, currency or level to use for the cost",
  "costAmount": 1
}
```

### Permissions - Compatible with LuckPerms, PlayerRoles, or any other farbic permission manager
You can adjust the default permission level in the config file to allow all players access without a permission manager.

`headindex.menu` - Grants /head and /head menu - Defaults to level 2 OP

`headindex.search` - Grants /head search and button in GUI - Defaults to level 2 OP

`headindex.playername` - Grants custom heads in GUI - Defaults to level 2 OP
