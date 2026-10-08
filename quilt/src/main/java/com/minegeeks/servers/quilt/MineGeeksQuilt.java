package com.minegeeks.servers.quilt;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import com.minegeeks.servers.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.util.ActionResult;
public final class MineGeeksQuilt implements ModInitializer{
 public void onInitialize(ModContainer mod){
  UseItemCallback.EVENT.register((p,w,h)->{if(w.isClientSide()||!(p instanceof net.minecraft.server.level.ServerPlayer sp))return ActionResult.PASS;var s=sp.getItemInHand(h);if(MineGeeksCore.isCosmicStar(s)){if(sp.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))sp.openMenu(new MineGeeksMenuProvider());return ActionResult.SUCCESS_SERVER;}if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst((net.minecraft.server.level.ServerLevel)sp.level(),sp,s);return ActionResult.SUCCESS_SERVER;}return ActionResult.PASS;});
  ServerTickEvents.END_SERVER_TICK.register(server->server.getPlayerList().getPlayers().forEach(sp->{MineGeeksCore.maintainStar(sp);MineGeeksCore.levitationTick(sp);}));
  ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{if(entity instanceof net.minecraft.world.entity.item.ItemEntity item&&MineGeeksCore.isGenerated(item.getItem())&&MineGeeksCore.flag(item.getItem(),MineGeeksCore.UNBREAKABLE_KEY))item.setPermanentlyInvulnerable(true);});
  PlayerBlockBreakEvents.BEFORE.register((w,p,pos,state,be)->{if(w instanceof net.minecraft.server.level.ServerLevel level&&p instanceof net.minecraft.server.level.ServerPlayer sp){var s=sp.getMainHandItem();if(MineGeeksCore.isGenerated(s)&&(MineGeeksCore.power(s,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(s,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers(level,sp,pos,s);return false;}}return true;});
 }
}