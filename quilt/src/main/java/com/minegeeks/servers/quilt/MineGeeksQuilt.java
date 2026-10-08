package com.minegeeks.servers.quilt;

import com.minegeeks.servers.MineGeeksCore;
import com.minegeeks.servers.MineGeeksMenuProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;

public final class MineGeeksQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || !(player instanceof net.minecraft.server.level.ServerPlayer sp)) {
                return ActionResult.PASS;
            }

            var stack = sp.getItemInHand(hand);

            if (MineGeeksCore.isCosmicStar(stack)) {
                if (sp.permissions().hasPermission(
                        net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
                    sp.openMenu(MineGeeksMenuProvider.create());
                }
                return ActionResult.SUCCESS_SERVER;
            }

            if (MineGeeksCore.isGenerated(stack)
                    && MineGeeksCore.power(stack, MineGeeksCore.WARDEN_KEY) > 0) {
                MineGeeksCore.wardenBurst(
                        (net.minecraft.server.level.ServerLevel) sp.level(), sp, stack);
                return ActionResult.SUCCESS_SERVER;
            }

            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(sp -> {
                    MineGeeksCore.maintainStar(sp);
                    MineGeeksCore.levitationTick(sp);
                }));

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof net.minecraft.world.entity.item.ItemEntity item)) return;

            if (MineGeeksCore.isCosmicStar(item.getItem())) {
                item.discard();
                return;
            }

            if (MineGeeksCore.isGenerated(item.getItem())
                    && MineGeeksCore.flag(item.getItem(), MineGeeksCore.UNBREAKABLE_KEY)) {
                item.setPermanentlyInvulnerable(true);
                item.setUnlimitedLifetime();
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof net.minecraft.server.level.ServerLevel level
                    && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                var stack = sp.getMainHandItem();
                if (MineGeeksCore.isGenerated(stack)
                        && (MineGeeksCore.power(stack, MineGeeksCore.SILK_KEY) > 0
                        || MineGeeksCore.power(stack, MineGeeksCore.FORTUNE_KEY) > 0
                        || MineGeeksCore.power(stack, MineGeeksCore.EFFICIENCY_KEY) > 0
                        || MineGeeksCore.flag(stack, MineGeeksCore.EARTH_EFFICIENCY_KEY))) {
                    MineGeeksCore.breakWithPowers(level, sp, pos, stack);
                    return false;
                }
            }
            return true;
        });
    }
}
