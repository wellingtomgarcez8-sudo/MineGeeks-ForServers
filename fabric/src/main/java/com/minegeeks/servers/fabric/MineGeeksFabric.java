package com.minegeeks.servers.fabric;
import com.minegeeks.servers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.util.ActionResult;
public final class MineGeeksFabric implements ModInitializer{
 public void onInitialize(){
  UseItemCallback.EVENT.register((p,w,h)->{if(w.isClientSide()||!(p instanceof net.minecraft.server.level.ServerPlayer sp))return ActionResult.PASS;var s=sp.getItemInHand(h);if(MineGeeksCore.isCosmicStar(s)){if(sp.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))sp.openMenu(MineGeeksMenuProvider.create());return ActionResult.SUCCESS_SERVER;}if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst((net.minecraft.server.level.ServerLevel)sp.level(),sp,s);return ActionResult.SUCCESS_SERVER;}return ActionResult.PASS;});
  ServerTickEvents.END_SERVER_TICK.register(server->server.getPlayerList().getPlayers().forEach(sp->{MineGeeksCore.maintainStar(sp);MineGeeksCore.levitationTick(sp);}));
  ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{
   if(entity instanceof net.minecraft.world.entity.item.ItemEntity item){
    if(MineGeeksCore.isCosmicStar(item.getItem())) { item.discard(); return; }
    if(MineGeeksCore.isGenerated(item.getItem())&&MineGeeksCore.flag(item.getItem(),MineGeeksCore.UNBREAKABLE_KEY)){
     item.setPermanentlyInvulnerable(true);
     item.setUnlimitedLifetime();
    }
   }
  });
  PlayerBlockBreakEvents.BEFORE.register((w,p,pos,state,be)->{if(w instanceof net.minecraft.server.level.ServerLevel level&&p instanceof net.minecraft.server.level.ServerPlayer sp){var s=sp.getMainHandItem();if(MineGeeksCore.isGenerated(s)&&(MineGeeksCore.power(s,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(s,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers(level,sp,pos,s);return false;}}return true;});
 }
}