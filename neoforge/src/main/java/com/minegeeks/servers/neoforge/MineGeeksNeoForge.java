package com.minegeeks.servers.neoforge;

import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(MineGeeksNeoForge.MODID)
public final class MineGeeksNeoForge {
    public static final String MODID = "minegeeks_for_servers";

    public MineGeeksNeoForge() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MineGeeksCore.maintainStar(player);
            MineGeeksCore.levitationTick(player);
        }
    }

    @SubscribeEvent
    public void entityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item) {
            if (MineGeeksCore.isCosmicStar(item.getItem())) {
                item.discard();
                return;
            }
            if (MineGeeksCore.isGenerated(item.getItem())
                    && MineGeeksCore.flag(item.getItem(), MineGeeksCore.UNBREAKABLE_KEY)) {
                item.setPermanentlyInvulnerable(true);
                item.setUnlimitedLifetime();
            }
        }
    }

    @SubscribeEvent
    public void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) return;
        var stack = event.getItemStack();

        if (MineGeeksCore.isCosmicStar(stack)) {
            if (player.hasPermissions(2)) {
                player.openMenu(MineGeeksMenuProvider.create());
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
            return;
        }

        if (MineGeeksCore.isGenerated(stack)
                && MineGeeksCore.power(stack, MineGeeksCore.WARDEN_KEY) > 0) {
            MineGeeksCore.wardenBurst((net.minecraft.server.level.ServerLevel) player.level(), player, stack);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public void breakBlock(BreakBlockEvent event) {
        if (event.getLevel().isClientSide() || !(event.getPlayer() instanceof ServerPlayer player)) return;
        var stack = player.getMainHandItem();
        if (!MineGeeksCore.isGenerated(stack)) return;

        if (MineGeeksCore.power(stack, MineGeeksCore.SILK_KEY) > 0
                || MineGeeksCore.power(stack, MineGeeksCore.FORTUNE_KEY) > 0
                || MineGeeksCore.power(stack, MineGeeksCore.EFFICIENCY_KEY) > 0
                || MineGeeksCore.flag(stack, MineGeeksCore.EARTH_EFFICIENCY_KEY)) {
            MineGeeksCore.breakWithPowers((net.minecraft.server.level.ServerLevel) event.getLevel(),
                    player, event.getPos(), stack);
            event.setCanceled(true);
        }
    }
}
