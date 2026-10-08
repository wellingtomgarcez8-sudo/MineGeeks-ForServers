package com.minegeeks.servers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public final class MineGeeksMenu extends ChestMenu {
    private static final int SIZE = 54;
    private static final int INPUT = 13;
    private final SimpleContainer container;

    public MineGeeksMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SIZE));
    }

    public MineGeeksMenu(int id, Inventory inventory, SimpleContainer container) {
        super(MenuType.GENERIC_9x6, id, inventory, container, 6);
        this.container = container;
        refresh();
    }

    private ItemStack button(Item item, String title, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(title));

        List<Component> lines = new ArrayList<>();
        for (String line : lore) lines.add(Component.literal(line));
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return stack;
    }

    private void refresh() {
        for (int i = 0; i < SIZE; i++) {
            if (i != INPUT) {
                container.setItem(i, button(Items.STAINED_GLASS_PANE.gray(), " "));
            }
        }

        ItemStack draft = container.getItem(INPUT);

        container.setItem(4, button(Items.NETHER_STAR,
                "GERADOR MineGeeks", "Coloque um item no slot central."));

        container.setItem(45, button(Items.NETHER_STAR,
                "Silk Touch",
                "Nível: " + MineGeeksCore.power(draft, MineGeeksCore.SILK_KEY),
                "0/1"));

        container.setItem(46, button(Items.EMERALD,
                "Fortuna",
                "Nível: " + MineGeeksCore.power(draft, MineGeeksCore.FORTUNE_KEY),
                "0-10"));

        container.setItem(47, button(Items.IRON_PICKAXE,
                "Eficiência",
                "Nível: " + MineGeeksCore.power(draft, MineGeeksCore.EFFICIENCY_KEY),
                "0-10"));

        container.setItem(48, button(Items.BEDROCK,
                "INQUEBRÁVEL",
                "Ativo: " + MineGeeksCore.flag(draft, MineGeeksCore.UNBREAKABLE_KEY),
                "Proteção total do item."));

        container.setItem(49, button(Items.GRASS_BLOCK,
                "Eficiência em Terra/Grama",
                "Ativo: " + MineGeeksCore.flag(draft, MineGeeksCore.EARTH_EFFICIENCY_KEY),
                "Terra, grama, areia, cascalho."));

        container.setItem(50, button(Items.SCULK_SHRIEKER,
                "Rajada de Warden",
                "Nível: " + MineGeeksCore.power(draft, MineGeeksCore.WARDEN_KEY),
                "Qualquer portador pode usar."));

        container.setItem(51, button(Items.FEATHER,
                "Pena Cósmica",
                "Ativo: " + MineGeeksCore.flag(draft, MineGeeksCore.FEATHER_KEY),
                "0,3 bloco/s, sem poção."));

        container.setItem(52, button(Items.REDSTONE,
                "LIMPAR",
                "Remove todos os poderes MineGeeks."));

        container.setItem(53, button(Items.END_CRYSTAL,
                "GERAR",
                "Baú remoto + shulker preta + coordenada."));
    }

    private void action(int slot, Player player) {
        ItemStack draft = container.getItem(INPUT);
        if (draft.isEmpty()) return;

        switch (slot) {
            case 45 -> MineGeeksCore.toggleLevel(draft, MineGeeksCore.SILK_KEY, 1);
            case 46 -> MineGeeksCore.toggleLevel(draft, MineGeeksCore.FORTUNE_KEY, 10);
            case 47 -> MineGeeksCore.toggleLevel(draft, MineGeeksCore.EFFICIENCY_KEY, 10);
            case 48 -> MineGeeksCore.toggleFlag(draft, MineGeeksCore.UNBREAKABLE_KEY);
            case 49 -> MineGeeksCore.toggleFlag(draft, MineGeeksCore.EARTH_EFFICIENCY_KEY);
            case 50 -> MineGeeksCore.toggleLevel(draft, MineGeeksCore.WARDEN_KEY, 10);
            case 51 -> MineGeeksCore.toggleFlag(draft, MineGeeksCore.FEATHER_KEY);
            case 52 -> {
                MineGeeksCore.setCustomData(draft, new net.minecraft.nbt.CompoundTag());
                draft.remove(DataComponents.CUSTOM_NAME);
                draft.remove(DataComponents.LORE);
            }
            case 53 -> generate(player);
        }

        refresh();
        broadcastChanges();
    }

    private void generate(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack draft = container.getItem(INPUT);
        if (draft.isEmpty()) return;

        ItemStack generated = MineGeeksCore.makeGenerated(draft);
        MineGeeksCore.makeGeneratedUnbreakable(generated);

        ServerLevel level = (ServerLevel) serverPlayer.level();
        BlockPos pos = MineGeeksCore.generateDelivery(level, serverPlayer, generated);

        container.setItem(INPUT, ItemStack.EMPTY);

        serverPlayer.sendSystemMessage(Component.literal(
                "MineGeeks: item criado em X=" + pos.getX()
                        + " Y=" + pos.getY()
                        + " Z=" + pos.getZ()));

        serverPlayer.closeContainer();
    }

    @Override
    public void clicked(int slot, int button, ContainerInput input, Player player) {
        if (slot >= 45 && slot <= 53) {
            action(slot, player);
            return;
        }

        if (slot >= 0 && slot < SIZE && slot != INPUT) return;
        super.clicked(slot, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < SIZE) return ItemStack.EMPTY;

        ItemStack source = player.getInventory().getItem(index - SIZE);
        if (source.isEmpty() || !container.getItem(INPUT).isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = source.copyWithCount(1);
        container.setItem(INPUT, copy);
        source.shrink(1);

        refresh();
        broadcastChanges();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        ItemStack input = container.getItem(INPUT);
        if (input.isEmpty()) return;

        ItemStack copy = input.copy();
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }

        container.setItem(INPUT, ItemStack.EMPTY);
    }
}
