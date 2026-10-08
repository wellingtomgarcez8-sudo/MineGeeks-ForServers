package com.minegeeks.servers.forge;
import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod(MineGeeksForge.MODID)
public final class MineGeeksForge{
 public static final String MODID="minegeeks_for_servers";
 public MineGeeksForge(){MinecraftForge.EVENT_BUS.register(this);}
 @SubscribeEvent public void onPlayerTick(TickEvent.PlayerTickEvent.Post e){if(e.player() instanceof net.minecraft.server.level.ServerPlayer p){MineGeeksCore.maintainStar(p);MineGeeksCore.levitationTick(p);}}
 @SubscribeEvent public void onItemUse(PlayerInteractEvent.RightClickItem e){if(e.getLevel().isClientSide()||!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p))return;var s=e.getItemStack();if(MineGeeksCore.isCosmicStar(s)){if(p.hasPermissions(2))p.openMenu(MineGeeksMenuProvider.create());e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);return;}if(MineGeeksCore.isGenerated(s)&&MineGeeksCore.power(s,MineGeeksCore.WARDEN_KEY)>0){MineGeeksCore.wardenBurst(p.serverLevel(),p,s);e.setCancellationResult(InteractionResult.SUCCESS);e.setCanceled(true);}}
 @SubscribeEvent public void onBlockBreak(BlockEvent.BreakEvent e){if(e.getLevel().isClientSide()||!(e.getPlayer() instanceof net.minecraft.server.level.ServerPlayer p))return;var t=p.getMainHandItem();if(MineGeeksCore.isGenerated(t)&&(MineGeeksCore.power(t,MineGeeksCore.SILK_KEY)>0||MineGeeksCore.power(t,MineGeeksCore.FORTUNE_KEY)>0||MineGeeksCore.power(t,MineGeeksCore.EFFICIENCY_KEY)>0||MineGeeksCore.flag(t,MineGeeksCore.EARTH_EFFICIENCY_KEY))){MineGeeksCore.breakWithPowers((net.minecraft.server.level.ServerLevel)e.getLevel(),p,e.getPos(),t);e.setCanceled(true);}}
 @SubscribeEvent public void onEntityJoin(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide()&&e.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity i&&MineGeeksCore.isGenerated(i.getItem())&&MineGeeksCore.flag(i.getItem(),MineGeeksCore.UNBREAKABLE_KEY))i.setPermanentlyInvulnerable(true);}
}
