package com.minegeeks.servers.forge;
import com.minegeeks.servers.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
@Mod(MineGeeksForge.MODID)
public final class MineGeeksForge{
 public static final String MODID="minegeeks_for_servers";
 public MineGeeksForge(){MinecraftForge.EVENT_BUS.register(this);}
 @SubscribeEvent public void tick(TickEvent.PlayerTickEvent e){if(e.phase!=TickEvent.Phase.END||!(e.player instanceof net.minecraft.server.level.ServerPlayer sp))return;MineGeeksCore.maintainStar(sp);MineGeeksCore.levitationTick(sp);}
 @SubscribeEvent public void right(PlayerInteractEvent.RightClickItem e){if(e.getLevel().isClientSide()||!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp))return;var s=e.getItemStack();if(MineGeeksCore.isCosmicStar(s)){if(sp.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))sp.openMenu(new MineGeeksMenuProvider());e.setCanceled(true);return;}if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst((net.minecraft.server.level.ServerLevel)sp.level(),sp,s);e.setCanceled(true);}}
 @SubscribeEvent public void entityJoin(EntityJoinLevelEvent e){if(e.getLevel().isClientSide())return;if(e.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item&&MineGeeksCore.isGenerated(item.getItem())&&MineGeeksCore.flag(item.getItem(),MineGeeksCore.UNBREAKABLE_KEY))item.setPermanentlyInvulnerable(true);}
 @SubscribeEvent public void block(BlockEvent.BreakEvent e){if(e.getLevel().isClientSide()||!(e.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp))return;var s=sp.getMainHandItem();if(MineGeeksCore.isGenerated(s)&&(MineGeeksCore.power(s,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(s,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(s,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers((net.minecraft.server.level.ServerLevel)e.getLevel(),sp,e.getPos(),s);e.setCanceled(true);}}
}
