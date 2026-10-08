package com.minegeeks.servers.fabric;
import com.minegeeks.servers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;
public final class MineGeeksFabric implements ModInitializer{
 public void onInitialize(){
  UseItemCallback.EVENT.register((p,w,h)->{if(w.isClientSide()||!(p instanceof net.minecraft.server.level.ServerPlayer sp))return ActionResult.PASS;var s=sp.getItemInHand(h);
   if(MineGeeksCore.isCosmicStar(s)){if(sp.hasPermissions(2))sp.openMenu(MineGeeksMenuProvider.create());return ActionResult.SUCCESS_SERVER;}
   if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst(sp.serverLevel(),sp,s);return ActionResult.SUCCESS_SERVER;}return ActionResult.PASS;});
  ServerTickEvents.END_SERVER_TICK.register(s->s.getPlayerList().getPlayers().forEach(p->{MineGeeksCore.maintainStar(p);MineGeeksCore.levitationTick(p);MineGeeksCore.protectGeneratedItemEntities(p.serverLevel());}));
  PlayerBlockBreakEvents.BEFORE.register((w,p,pos,state,be)->{if(w instanceof net.minecraft.server.level.ServerLevel l&&p instanceof net.minecraft.server.level.ServerPlayer sp){var t=sp.getMainHandItem();if(MineGeeksCore.isGenerated(t)&&(MineGeeksCore.power(t,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(t,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(t,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(t,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers(l,sp,pos,t);return false;}}return true;});
 }
}
