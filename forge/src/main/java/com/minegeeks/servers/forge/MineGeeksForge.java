package com.minegeeks.servers.forge;

import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.invoke.MethodHandles;

@Mod(MineGeeksForge.MODID)
public final class MineGeeksForge {
    public static final String MODID = "minegeeks_for_servers";

    public MineGeeksForge() {
        BusGroup.DEFAULT.register(MethodHandles.lookup(), MineGeeksForge.class);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent.Post event) {
        if (event.player() instanceof ServerPlayer player) {
            MineGeeksCore.maintainStar(player);
            MineGeeksCore.levitationTick(player);
        }
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item) {
            MineGeeksCore.protectGeneratedItemEntity(item);
        }
    }

    @SubscribeEvent
    public static boolean rightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        var stack = event.getItemStack();

        if (MineGeeksCore.isCosmicStar(stack)) {
            if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                return false;
            }

            player.openMenu(MineGeeksMenuProvider.create());
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        if (MineGeeksCore.isGenerated(stack)
                && MineGeeksCore.power(stack, MineGeeksCore.WARDEN_KEY) > 0) {
            MineGeeksCore.wardenBurst(
                    (net.minecraft.server.level.ServerLevel) player.level(),
                    player, stack);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public static boolean blockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer player)) {
            return false;
        }

        var tool = player.getMainHandItem();

        if (MineGeeksCore.isGenerated(tool)
                && (MineGeeksCore.power(tool, MineGeeksCore.SILK_KEY) > 0
                || MineGeeksCore.power(tool, MineGeeksCore.FORTUNE_KEY) > 0
                || MineGeeksCore.power(tool, MineGeeksCore.EFFICIENCY_KEY) > 0
                || MineGeeksCore.flag(tool, MineGeeksCore.EARTH_EFFICIENCY_KEY))) {
            MineGeeksCore.breakWithPowers(level, player, event.getPos(), tool);
            return true;
        }

        return false;
    }
}
