package com.minegeeks.servers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.util.Unit;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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
import java.util.List;

public final class MineGeeksCore {
    public static final String STAR_KEY="MineGeeksCosmicStar";
    public static final String POWER_KEY="MineGeeksPower";
    public static final String SILK_KEY="SilkTouch";
    public static final String FORTUNE_KEY="Fortune";
    public static final String EFFICIENCY_KEY="Efficiency";
    public static final String UNBREAKABLE_KEY="Unbreakable";
    public static final String EARTH_EFFICIENCY_KEY="EarthEfficiency";
    public static final String WARDEN_KEY="WardenBurst";
    public static final String FEATHER_KEY="CosmicFeather";
    private MineGeeksCore(){}

    public static ItemStack cosmicStar(){
        ItemStack s=new ItemStack(Items.NETHER_STAR);
        CompoundTag t=new CompoundTag(); t.putBoolean(STAR_KEY,true);
        s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
        s.set(DataComponents.CUSTOM_NAME,Component.literal("Estrela Cósmica"));
        List<Component> lore=List.of(
                Component.literal("MineGeeks For Servers"),
                Component.literal("Painel administrativo do servidor"),
                Component.literal("Não pode ser descartada"));
        s.set(DataComponents.LORE,new ItemLore(lore));
        return s;
    }
    public static CompoundTag customData(ItemStack s){
        CustomData d=s.get(DataComponents.CUSTOM_DATA);
        return d==null?new CompoundTag():d.copyTag();
    }
    public static void setCustomData(ItemStack s,CompoundTag t){
        if(t.isEmpty())s.remove(DataComponents.CUSTOM_DATA);else s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
    }
    public static boolean isCosmicStar(ItemStack s){
        if(s.isEmpty()||s.getItem()!=Items.NETHER_STAR)return false;
        CustomData d=s.get(DataComponents.CUSTOM_DATA);
        return d!=null&&d.copyTag().getBoolean(STAR_KEY).orElse(false);
    }
    public static boolean isGenerated(ItemStack s){return customData(s).getBoolean(POWER_KEY).orElse(false);}
    public static int power(ItemStack s,String key){return customData(s).getInt(key).orElse(0);}
    public static boolean flag(ItemStack s,String key){return customData(s).getBoolean(key).orElse(false);}
    public static ItemStack makeGenerated(ItemStack source){
        ItemStack out=source.copy();
        CompoundTag data=customData(out); data.putBoolean(POWER_KEY,true); setCustomData(out,data);
        out.set(DataComponents.CUSTOM_NAME,Component.literal("MineGeeks: "+source.getHoverName().getString()));
        return out;
    }
    public static void toggleLevel(ItemStack stack,String key,int max){
        CompoundTag data=customData(stack); int value=data.getInt(key).orElse(0);
        value=value>=max?0:value+1; data.putInt(key,value); data.putBoolean(POWER_KEY,true); setCustomData(stack,data);
    }
    public static void toggleFlag(ItemStack stack,String key){
        CompoundTag data=customData(stack);
        data.putBoolean(key,!data.getBoolean(key).orElse(false)); data.putBoolean(POWER_KEY,true); setCustomData(stack,data);
    }
    public static boolean isOre(BlockState state){return state.is(net.minecraft.tags.BlockTags.ORES);}

    public static void breakWithPowers(ServerLevel level,ServerPlayer player,BlockPos pos,ItemStack tool){
        BlockState state=level.getBlockState(pos); if(state.isAir())return;
        int silk=power(tool,SILK_KEY), fortune=power(tool,FORTUNE_KEY);
        boolean efficiency=power(tool,EFFICIENCY_KEY)>0;
        boolean earthEfficiency=flag(tool,EARTH_EFFICIENCY_KEY);
        if(silk==0&&fortune==0&&!efficiency&&!earthEfficiency)return;

        BlockEntity blockEntity=level.getBlockEntity(pos); boolean ore=isOre(state);
        if(silk>0&&!(ore&&efficiency)){
            ItemStack clone=state.getCloneItemStack(level,pos,false);
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
            if(!clone.isEmpty())Block.popResource(level,pos,clone);
            state.spawnAfterBreak(level,pos,tool,true);
            damageToolIfNeeded(player,tool);
            return;
        }

        List<ItemStack> drops=Block.getDrops(state,level,pos,blockEntity,player,tool);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        for(ItemStack drop:drops){
            if(ore&&fortune>0){
                int multiplier=1+level.getRandom().nextInt(fortune+1);
                drop=drop.copyWithCount(Math.min(drop.getMaxStackSize(),drop.getCount()*multiplier));
            }
            if(!drop.isEmpty())Block.popResource(level,pos,drop);
        }
        state.spawnAfterBreak(level,pos,tool,true);
        damageToolIfNeeded(player,tool);
    }

    private static void damageToolIfNeeded(ServerPlayer player,ItemStack tool){
        if(flag(tool,UNBREAKABLE_KEY)||tool.isEmpty()||!tool.isDamageableItem())return;
        tool.hurtAndBreak(1,player,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    public static void wardenBurst(ServerLevel level,ServerPlayer player,ItemStack source){
        Vec3 origin=player.getEyePosition(), direction=player.getViewVector(1.0F);
        LivingEntity hit=null; double best=48.0*48.0;
        for(Entity entity:level.getEntities(player,player.getBoundingBox().inflate(48.0),
                e->e instanceof LivingEntity&&e!=player&&e.isAlive())){
            LivingEntity living=(LivingEntity)entity;
            Vec3 center=living.getBoundingBox().getCenter(), relative=center.subtract(origin);
            double forward=relative.dot(direction);
            if(forward<0||forward>48)continue;
            Vec3 projected=origin.add(direction.scale(forward));
            double sideDistance=center.distanceToSqr(projected);
            if(sideDistance<4.0&&forward*forward<best){hit=living;best=forward*forward;}
        }
        for(int i=1;i<=32;i++){
            Vec3 p=origin.add(direction.scale(i*1.5));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,p.x,p.y,p.z,1,0,0,0,0);
        }
        level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM,
                net.minecraft.sounds.SoundSource.PLAYERS,2.0F,1.0F);
        if(hit!=null){
            float damage=12.0F+Math.min(40.0F,power(source,WARDEN_KEY)*4.0F);
            hit.hurt(level.damageSources().sonicBoom(player),damage);
            Vec3 push=direction.scale(2.2); hit.push(push.x,0.45,push.z);
        }
    }

    public static void levitationTick(ServerPlayer player){
        boolean hasFeather=flag(player.getMainHandItem(),FEATHER_KEY)
                ||flag(player.getOffhandItem(),FEATHER_KEY)
                ||player.getInventory().contains(s->flag(s,FEATHER_KEY));
        if(!hasFeather)return;
        player.fallDistance=0.0F;
        if(!player.onGround()&&!player.isFallFlying()){
            Vec3 velocity=player.getDeltaMovement();
            if(velocity.y<0.015)player.setDeltaMovement(velocity.x,0.015,velocity.z);
        }
    }

    public static void protectGeneratedItemEntities(ServerLevel level){
        for(Entity entity:level.getEntities().getAll()){
            if(entity instanceof ItemEntity item&&isGenerated(item.getItem())&&flag(item.getItem(),UNBREAKABLE_KEY)){
                item.setPermanentlyInvulnerable(true);
            }
        }
    }

    public static void maintainStar(ServerPlayer player){
        if(player.level().getServer()==null||!player.level().getServer().getPlayerList().isOp(player.nameAndId()))return;
        ItemStack star=null;
        for(int i=0;i<player.getInventory().getContainerSize();i++){
            ItemStack stack=player.getInventory().getItem(i);
            if(isCosmicStar(stack)){
                if(star==null)star=stack;
                player.getInventory().setItem(i,ItemStack.EMPTY);
            }
        }
        if(star==null)star=cosmicStar();
        ItemStack last=player.getInventory().getItem(8);
        if(!last.isEmpty()&&!isCosmicStar(last)){
            player.getInventory().items.set(8,ItemStack.EMPTY);
            if(!player.getInventory().add(last))player.drop(last,false,Prediction.SERVER_ONLY);
        }
        player.getInventory().setItem(8,star);
        for(Entity entity:player.level().getEntities(player,player.getBoundingBox().inflate(3.0),
                e->e instanceof ItemEntity ie&&isCosmicStar(ie.getItem()))){
            entity.discard();
        }
    }

    public static void prepareOutputShulker(ServerLevel level,ServerPlayer player,BlockPos pos){
        NonNullList<ItemStack> contents=NonNullList.withSize(27,ItemStack.EMPTY);
        for(int i=0;i<26;i++)contents.set(i,new ItemStack(Items.DIRT));

        ItemStack paper=new ItemStack(Items.PAPER);
        paper.set(DataComponents.CUSTOM_NAME,Component.literal(
                "Coordenada: X="+pos.getX()+" Y="+pos.getY()+" Z="+pos.getZ()));
        CompoundTag paperData=new CompoundTag();
        paperData.putBoolean("MineGeeksCoordinatePaper",true);
        paperData.putInt("X",pos.getX());paperData.putInt("Y",pos.getY());paperData.putInt("Z",pos.getZ());
        setCustomData(paper,paperData);
        contents.set(26,paper);

        ItemStack shulker=Items.DYED_SHULKER_BOX.asItem(DyeColor.BLACK);
        shulker.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(contents));
        shulker.set(DataComponents.CUSTOM_NAME,Component.literal("Entrega Cósmica"));
        CompoundTag shulkerData=new CompoundTag();shulkerData.putBoolean("MineGeeksDelivery",true);setCustomData(shulker,shulkerData);

        if(!player.getInventory().add(shulker))player.drop(shulker,false,Prediction.SERVER_ONLY);
    }

    public static BlockPos randomDeliveryPosition(ServerLevel level,ServerPlayer player){
        RandomSource random=level.getRandom();
        double angle=random.nextDouble()*Math.PI*2.0;
        double distance=Math.sqrt(random.nextDouble())*2000.0;
        return new BlockPos(
                Mth.floor(player.getX()+Math.cos(angle)*distance),
                Mth.nextInt(random,-50,100),
                Mth.floor(player.getZ()+Math.sin(angle)*distance));
    }

    public static BlockPos generateDelivery(ServerLevel level,ServerPlayer player,ItemStack generated){
        BlockPos pos=randomDeliveryPosition(level,player);
        ChunkPos chunk=new ChunkPos(pos.getX()>>4,pos.getZ()>>4);
        level.getChunk(chunk.x(),chunk.z(),ChunkStatus.FULL,true);
        level.setBlock(pos,
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,player.getDirection().getOpposite()),
                Block.UPDATE_ALL);
        BlockEntity entity=level.getBlockEntity(pos);
        if(entity instanceof ChestBlockEntity chest){chest.setItem(0,generated.copy());chest.setChanged();}
        prepareOutputShulker(level,player,pos);
        return pos;
    }
}
