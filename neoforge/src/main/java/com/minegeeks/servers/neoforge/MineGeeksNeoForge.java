package com.minegeeks.servers.neoforge;
import com.minegeeks.servers.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
@Mod(MineGeeksNeoForge.MODID)
public final class MineGeeksNeoForge{
 public static final String MODID="minegeeks_for_servers";
 public MineGeeksNeoForge(){NeoForge.EVENT_BUS.register(this);}
 @net.neoforged.bus.api.SubscribeEvent public void tick(PlayerTickEvent.Post e){if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp){MineGeeksCore.maintainStar(sp);MineGeeksCore.levitationTick(sp);}}
 @net.neoforged.bus.api.SubscribeEvent public void right(PlayerInteractEvent.RightClickItem e){if(e.getLevel().isClientSide()||!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp))return;var s=e.getItemStack();if(MineGeeksCore.isCosmicStar(s)){if(sp.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))sp.openMenu(new MineGeeksMenuProvider());e.setCanceled(true);return;}if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst(sp.serverLevel(),sp,s);e.setCanceled(true);}}
 @net.neoforged.bus.api.SubscribeEvent public void block(BlockEvent.BreakEvent e){if(e.getLevel().isClientSide()||!(e.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp))return;var s=sp.getMainHandItem();if(MineGeeksCore.isGenerated(s)&&(MineGeeksCore.power(s,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(s,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers((net.minecraft.server.level.ServerLevel)e.getLevel(),sp,e.getPos(),s);e.setCanceled(true);}}
}
