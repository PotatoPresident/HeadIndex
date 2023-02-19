# HeadIndex
Head Index is an easy to user server-side head database mod for the Fabric Loader. 
Head Index provides easy access to over 36,000 heads provided by minecraft-heads.com. 
Unlike a plugin, Head Index can also be used in singleplayer without the need for a server. 
New heads are added all the time and will be automatically added whenever you restart the server without needing to update. 
Head Index also supports setting an item or economy (Common Economy API) cost for survival servers.


# Usage
### Commands

`/head` - Opens head GUI

`/head menu` - Also opens head GUI

`/head search` <search> - Opens search menu

### Config
`config/head-index.json`
```json5
{
  "permissionLevel": 2, // The default permission level for the commands. Set to 0 to allow all players access
  "economyType": "FREE", // The type of economy to use. Set to FREE to disable economy, ITEM to use an item, or ECONOMY to use an economy currency
  "costType": "minecraft:diamond", // The identifier for the item or currency to use for the cost
  "costAmount": 1 // The amount of the item or currency to use for the cost
}
```

### Permissions - Compatible with LuckPerms, PlayerRoles or any other farbic permission manager
You can adjust the default permission level in the config file to allow all players access without a permission manager.

`headindex.menu` - Grants /head and /head menu - Defaults to level 2 OP

`headindex.search` - Grants /head search and button in GUI - Defaults to level 2 OP

`headindex.playername` - Grants custom heads in GUI - Defaults to level 2 OP
