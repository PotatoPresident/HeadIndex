package us.potatoboy.headindex.gui;

import us.potatoboy.headindex.api.GeyserHeadDatabaseAPI;
import us.potatoboy.headindex.gui.HeadGuiBedrock;
import us.potatoboy.headindex.gui.HeadGui;

import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.loader.api.FabricLoader;

public class HeadGuiFactory {
   // Not meant to be instantiated
   private HeadGuiFactory() {
   }

   public static HeadGui makeHeadGui(ServerPlayer player) {
       boolean hasFloodgate = FabricLoader.getInstance().isModLoaded("floodgate");
       boolean isBedrockPlayer = false;
       if (hasFloodgate) {
           isBedrockPlayer = GeyserHeadDatabaseAPI.isActiveBedrockPlayer(player);
       }

       if (isBedrockPlayer) {
           return new HeadGuiBedrock(player);
       }
       return new HeadGui(player);
   }
}
