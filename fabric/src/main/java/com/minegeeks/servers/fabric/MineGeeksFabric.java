package com.minegeeks.servers.fabric;

import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;

public final class MineGeeksFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MineGeeksCore.initialize();
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            var stack = serverPlayer.getItemInHand(hand);

            if (MineGeeksCore.isCosmicStar(stack)) {
                if (serverPlayer.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    serverPlayer.openMenu(MineGeeksMenuProvider.create());
                }
                return InteractionResult.SUCCESS;
            }

            if (MineGeeksCore.isGenerated(stack)
                    && MineGeeksCore.power(stack, MineGeeksCore.WARDEN_KEY) > 0) {
                MineGeeksCore.wardenBurst(
                        (net.minecraft.server.level.ServerLevel) serverPlayer.level(),
                        serverPlayer, stack);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(player -> {
                    MineGeeksCore.maintainStar(player);
                    MineGeeksCore.levitationTick(player);
                }));

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
                MineGeeksCore.protectGeneratedItemEntity(item);
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof net.minecraft.server.level.ServerLevel level)
                    || !(player instanceof ServerPlayer serverPlayer)) {
                return true;
            }

            var tool = serverPlayer.getMainHandItem();

            if (MineGeeksCore.isGenerated(tool)
                    && (MineGeeksCore.power(tool, MineGeeksCore.SILK_KEY) > 0
                    || MineGeeksCore.power(tool, MineGeeksCore.FORTUNE_KEY) > 0
                    || MineGeeksCore.power(tool, MineGeeksCore.EFFICIENCY_KEY) > 0
                    || MineGeeksCore.flag(tool, MineGeeksCore.EARTH_EFFICIENCY_KEY))) {
                MineGeeksCore.breakWithPowers(level, serverPlayer, pos, tool);
                return false;
            }

            return true;
        });
    }
}
