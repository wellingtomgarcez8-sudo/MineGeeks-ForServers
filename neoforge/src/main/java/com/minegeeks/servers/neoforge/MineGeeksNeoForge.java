package com.minegeeks.servers.neoforge;

import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
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

    @net.neoforged.bus.api.SubscribeEvent
    public void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MineGeeksCore.maintainStar(player);
            MineGeeksCore.levitationTick(player);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void entityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item) {
            MineGeeksCore.protectGeneratedItemEntity(item);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        var stack = event.getItemStack();

        if (MineGeeksCore.isCosmicStar(stack)) {
            if (!player.permissions().hasPermission(
                    net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
                return;
            }

            player.openMenu(MineGeeksMenuProvider.create());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (MineGeeksCore.isGenerated(stack)
                && MineGeeksCore.power(stack, MineGeeksCore.WARDEN_KEY) > 0) {
            MineGeeksCore.wardenBurst(
                    (net.minecraft.server.level.ServerLevel) player.level(),
                    player, stack);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void breakBlock(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()
                || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        var tool = player.getMainHandItem();

        if (MineGeeksCore.isGenerated(tool)
                && (MineGeeksCore.power(tool, MineGeeksCore.SILK_KEY) > 0
                || MineGeeksCore.power(tool, MineGeeksCore.FORTUNE_KEY) > 0
                || MineGeeksCore.power(tool, MineGeeksCore.EFFICIENCY_KEY) > 0
                || MineGeeksCore.flag(tool, MineGeeksCore.EARTH_EFFICIENCY_KEY))) {
            MineGeeksCore.breakWithPowers(
                    (net.minecraft.server.level.ServerLevel) event.getLevel(),
                    player,
                    event.getPos(),
                    tool);
            event.setCanceled(true);
        }
    }
}
