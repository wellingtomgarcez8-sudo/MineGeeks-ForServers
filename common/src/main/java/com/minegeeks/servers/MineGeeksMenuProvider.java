package com.minegeeks.servers;

import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;

public final class MineGeeksMenuProvider {
    private MineGeeksMenuProvider() {}

    public static SimpleMenuProvider create() {
        return new SimpleMenuProvider(
                (id, inventory, player) -> new MineGeeksMenu(id, inventory),
                Component.literal("MineGeeks For Servers"));
    }
}
