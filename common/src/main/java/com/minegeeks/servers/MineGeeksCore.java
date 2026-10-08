package com.minegeeks.servers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class MineGeeksCore {
    public static final String MODID = "minegeeks_for_servers";
    public static final ResourceKey<Item> COSMIC_STAR_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, "cosmic_star"));
    public static final Item COSMIC_STAR_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            COSMIC_STAR_KEY,
            new Item(new Item.Properties().setId(COSMIC_STAR_KEY).stacksTo(1)));

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MODID, "main"));
    public static final CreativeModeTab CREATIVE_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            CREATIVE_TAB_KEY,
            CreativeModeTab.builder()
                    .title(Component.literal("MineGeeks For Servers"))
                    .icon(() -> COSMIC_STAR_ITEM.getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(COSMIC_STAR_ITEM))
                    .build());
    public static final String STAR_KEY = "MineGeeksCosmicStar";
    public static final String POWER_KEY = "MineGeeksPower";
    public static final String SILK_KEY = "SilkTouch";
    public static final String FORTUNE_KEY = "Fortune";
    public static final String EFFICIENCY_KEY = "Efficiency";
    public static final String UNBREAKABLE_KEY = "Unbreakable";
    public static final String EARTH_EFFICIENCY_KEY = "EarthEfficiency";
    public static final String WARDEN_KEY = "WardenBurst";
    public static final String FEATHER_KEY = "CosmicFeather";

    private MineGeeksCore() {}

    public static void initialize() {
        // Force class initialization on both client and server.
    }

    private static ServerLevel serverLevel(ServerPlayer player) {
        return (ServerLevel) player.level();
    }

    public static ItemStack cosmicStar() {
        ItemStack stack = COSMIC_STAR_ITEM.getDefaultInstance();
        CompoundTag data = new CompoundTag();
        data.putBoolean(STAR_KEY, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Estrela Cósmica"));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("MineGeeks For Servers"),
                Component.literal("Painel administrativo do servidor"),
                Component.literal("Não pode ser descartada")
        )));
        return stack;
    }

    public static CompoundTag customData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static void setCustomData(ItemStack stack, CompoundTag data) {
        if (data.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static boolean isCosmicStar(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != COSMIC_STAR_ITEM) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(STAR_KEY).orElse(false);
    }

    public static boolean isGenerated(ItemStack stack) {
        return customData(stack).getBoolean(POWER_KEY).orElse(false);
    }

    public static int power(ItemStack stack, String key) {
        return customData(stack).getInt(key).orElse(0);
    }

    public static boolean flag(ItemStack stack, String key) {
        return customData(stack).getBoolean(key).orElse(false);
    }

    public static ItemStack makeGenerated(ItemStack source) {
        ItemStack result = source.copy();
        CompoundTag data = customData(result);
        data.putBoolean(POWER_KEY, true);
        setCustomData(result, data);
        result.set(DataComponents.CUSTOM_NAME,
                Component.literal("MineGeeks: " + source.getHoverName().getString()));
        addPowerLore(result);
        return result;
    }

    private static void addPowerLore(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        ItemLore existing = stack.get(DataComponents.LORE);
        if (existing != null) lines.addAll(existing.lines());

        if (power(stack, SILK_KEY) > 0) lines.add(Component.literal("MineGeeks Silk Touch"));
        if (power(stack, FORTUNE_KEY) > 0) lines.add(Component.literal("MineGeeks Fortune " + power(stack, FORTUNE_KEY)));
        if (power(stack, EFFICIENCY_KEY) > 0) lines.add(Component.literal("MineGeeks Efficiency " + power(stack, EFFICIENCY_KEY)));
        if (flag(stack, UNBREAKABLE_KEY)) lines.add(Component.literal("MineGeeks Indestrutível"));
        if (flag(stack, EARTH_EFFICIENCY_KEY)) lines.add(Component.literal("MineGeeks Earth Efficiency"));
        if (power(stack, WARDEN_KEY) > 0) lines.add(Component.literal("MineGeeks Warden Burst " + power(stack, WARDEN_KEY)));
        if (flag(stack, FEATHER_KEY)) lines.add(Component.literal("MineGeeks Cosmic Feather"));

        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    public static void toggleLevel(ItemStack stack, String key, int max) {
        CompoundTag data = customData(stack);
        int value = data.getInt(key).orElse(0);
        value = value >= max ? 0 : value + 1;
        data.putInt(key, value);
        data.putBoolean(POWER_KEY, true);
        setCustomData(stack, data);
        addPowerLore(stack);
    }

    public static void toggleFlag(ItemStack stack, String key) {
        CompoundTag data = customData(stack);
        data.putBoolean(key, !data.getBoolean(key).orElse(false));
        data.putBoolean(POWER_KEY, true);
        setCustomData(stack, data);
        addPowerLore(stack);
    }

    public static boolean isOre(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.ORES);
    }

    public static void breakWithPowers(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        int silk = power(tool, SILK_KEY);
        int fortune = power(tool, FORTUNE_KEY);
        boolean efficiency = power(tool, EFFICIENCY_KEY) > 0;
        boolean earthEfficiency = flag(tool, EARTH_EFFICIENCY_KEY);

        if (silk == 0 && fortune == 0 && !efficiency && !earthEfficiency) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        boolean ore = isOre(state);

        // Silk Touch wins for ordinary blocks.
        // Ores with Silk + Efficiency are intentionally handled by Fortune only.
        if (silk > 0 && !(ore && efficiency)) {
            ItemStack clone = state.getBlock().asItem().getDefaultInstance();
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            if (!clone.isEmpty()) Block.popResource(level, pos, clone);
            state.spawnAfterBreak(level, pos, tool, true);
            damageToolIfNeeded(player, tool);
            return;
        }

        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        for (ItemStack drop : drops) {
            if (ore && fortune > 0) {
                int multiplier = 1 + level.getRandom().nextInt(fortune + 1);
                drop = drop.copyWithCount(Math.min(drop.getMaxStackSize(), drop.getCount() * multiplier));
            }
            if (!drop.isEmpty()) Block.popResource(level, pos, drop);
        }

        state.spawnAfterBreak(level, pos, tool, true);
        damageToolIfNeeded(player, tool);
    }

    private static void damageToolIfNeeded(ServerPlayer player, ItemStack tool) {
        if (flag(tool, UNBREAKABLE_KEY) || tool.isEmpty() || !tool.isDamageableItem()) return;
        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static void wardenBurst(ServerLevel level, ServerPlayer player, ItemStack source) {
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0F);

        LivingEntity hit = null;
        double best = 48.0 * 48.0;

        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(48.0),
                candidate -> candidate instanceof LivingEntity && candidate != player && candidate.isAlive())) {
            LivingEntity living = (LivingEntity) entity;
            Vec3 center = living.getBoundingBox().getCenter();
            Vec3 relative = center.subtract(origin);
            double forward = relative.dot(direction);

            if (forward < 0.0 || forward > 48.0) continue;

            Vec3 projected = origin.add(direction.scale(forward));
            double sideDistance = center.distanceToSqr(projected);

            if (sideDistance < 4.0 && forward * forward < best) {
                hit = living;
                best = forward * forward;
            }
        }

        for (int i = 1; i <= 32; i++) {
            Vec3 particlePos = origin.add(direction.scale(i * 1.5));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                    particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }

        level.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM,
                net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);

        if (hit != null) {
            float damage = 12.0F + Math.min(40.0F, power(source, WARDEN_KEY) * 4.0F);
            hit.hurtServer(level, level.damageSources().sonicBoom(player), damage);
            Vec3 push = direction.scale(2.2);
            hit.push(push.x, 0.45, push.z);
        }
    }

    public static void levitationTick(ServerPlayer player) {
        boolean hasFeather = flag(player.getMainHandItem(), FEATHER_KEY)
                || flag(player.getOffhandItem(), FEATHER_KEY)
                || player.getInventory().contains(stack -> flag(stack, FEATHER_KEY));

        if (!hasFeather) return;

        player.fallDistance = 0.0F;

        if (!player.onGround() && !player.isFallFlying()) {
            Vec3 velocity = player.getDeltaMovement();
            if (velocity.y < 0.015) {
                player.setDeltaMovement(velocity.x, 0.015, velocity.z);
            }
        }
    }

    public static void maintainStar(ServerPlayer player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return;

        ItemStack star = null;
        int size = player.getInventory().getContainerSize();

        for (int i = 0; i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!isCosmicStar(stack)) continue;

            if (star == null) star = stack.copy();
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        if (star == null) star = cosmicStar();

        ItemStack last = player.getInventory().getItem(8);
        if (!last.isEmpty() && !isCosmicStar(last)) {
            player.getInventory().setItem(8, ItemStack.EMPTY);
            if (!player.getInventory().add(last)) {
                player.drop(last, false, Prediction.SERVER_ONLY);
            }
        }

        player.getInventory().setItem(8, star);

        for (Entity entity : serverLevel(player).getEntities(
                player, player.getBoundingBox().inflate(3.0),
                e -> e instanceof ItemEntity item && isCosmicStar(item.getItem()))) {
            entity.discard();
        }
    }

    public static void protectGeneratedItemEntity(ItemEntity item) {
        if (isCosmicStar(item.getItem())) {
            item.discard();
            return;
        }

        if (isGenerated(item.getItem()) && flag(item.getItem(), UNBREAKABLE_KEY)) {
            item.setPermanentlyInvulnerable(true);
            item.setUnlimitedLifetime();
        }
    }

    public static void prepareOutputShulker(ServerPlayer player, BlockPos pos) {
        NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);

        for (int i = 0; i < 26; i++) {
            contents.set(i, Items.DIRT.getDefaultInstance());
        }

        ItemStack paper = Items.PAPER.getDefaultInstance();
        paper.set(DataComponents.CUSTOM_NAME,
                Component.literal("Coordenada: X=" + pos.getX() + " Y=" + pos.getY() + " Z=" + pos.getZ()));

        CompoundTag data = new CompoundTag();
        data.putBoolean("MineGeeksCoordinatePaper", true);
        data.putInt("X", pos.getX());
        data.putInt("Y", pos.getY());
        data.putInt("Z", pos.getZ());
        setCustomData(paper, data);
        contents.set(26, paper);

        ItemStack shulker = new ItemStack(Items.DYED_SHULKER_BOX.black());
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        shulker.set(DataComponents.CUSTOM_NAME, Component.literal("Entrega Cósmica"));

        CompoundTag shulkerData = new CompoundTag();
        shulkerData.putBoolean("MineGeeksDelivery", true);
        setCustomData(shulker, shulkerData);

        if (!player.getInventory().add(shulker)) {
            player.drop(shulker, false, Prediction.SERVER_ONLY);
        }
    }

    public static BlockPos randomDeliveryPosition(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = Math.sqrt(random.nextDouble()) * 2000.0;

        return new BlockPos(
                Mth.floor(player.getX() + Math.cos(angle) * distance),
                Mth.nextInt(random, -50, 100),
                Mth.floor(player.getZ() + Math.sin(angle) * distance)
        );
    }

    public static BlockPos generateDelivery(ServerLevel level, ServerPlayer player, ItemStack generated) {
        BlockPos pos = randomDeliveryPosition(level, player);
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

        level.getChunk(chunk.x(), chunk.z(), ChunkStatus.FULL, true);

        level.setBlock(
                pos,
                Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, player.getDirection().getOpposite()),
                Block.UPDATE_ALL
        );

        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ChestBlockEntity chest) {
            chest.setItem(0, generated.copy());
            chest.setChanged();
        }

        prepareOutputShulker(player, pos);
        return pos;
    }

    public static void makeGeneratedUnbreakable(ItemStack generated) {
        if (!flag(generated, UNBREAKABLE_KEY)) return;

        generated.set(DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);

        DamageResistant resistance =
                Items.NETHERITE_INGOT.getDefaultInstance().get(DataComponents.DAMAGE_RESISTANT);

        if (resistance != null) {
            generated.set(DataComponents.DAMAGE_RESISTANT, resistance);
        }
    }
}
