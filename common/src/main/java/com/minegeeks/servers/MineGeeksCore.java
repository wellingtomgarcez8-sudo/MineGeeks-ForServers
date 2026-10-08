package com.minegeeks.servers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
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
        s.set(DataComponents.LORE,new ItemLore(List.of(
                Component.literal("MineGeeks For Servers"),
                Component.literal("Painel administrativo do servidor"),
                Component.literal("Não pode ser descartada"))));
        return s;
    }
    public static boolean isCosmicStar(ItemStack s){
        if(s.isEmpty()||s.getItem()!=Items.NETHER_STAR)return false;
        CustomData d=s.get(DataComponents.CUSTOM_DATA);
        return d!=null&&d.copyTag().getBoolean(STAR_KEY).orElse(false);
    }
    public static CompoundTag customData(ItemStack s){
        CustomData d=s.get(DataComponents.CUSTOM_DATA);
        return d==null?new CompoundTag():d.copyTag();
    }
    public static void setCustomData(ItemStack s,CompoundTag t){
        if(t.isEmpty())s.remove(DataComponents.CUSTOM_DATA); else s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));
    }
    public static boolean isGenerated(ItemStack s){return customData(s).getBoolean(POWER_KEY).orElse(false);}
    public static int power(ItemStack s,String k){return customData(s).getInt(k).orElse(0);}
    public static boolean flag(ItemStack s,String k){return customData(s).getBoolean(k).orElse(false);}
    public static ItemStack makeGenerated(ItemStack src){
        ItemStack out=src.copy(); CompoundTag t=customData(out); t.putBoolean(POWER_KEY,true); setCustomData(out,t);
        out.set(DataComponents.CUSTOM_NAME,Component.literal("MineGeeks: "+src.getHoverName().getString()));
        return out;
    }
    public static void toggleLevel(ItemStack s,String key,int max){
        CompoundTag t=customData(s); int v=t.getInt(key).orElse(0); v=v>=max?0:v+1; t.putInt(key,v); t.putBoolean(POWER_KEY,true); setCustomData(s,t);
    }
    public static void toggleFlag(ItemStack s,String key){
        CompoundTag t=customData(s); t.putBoolean(key,!t.getBoolean(key).orElse(false)); t.putBoolean(POWER_KEY,true); setCustomData(s,t);
    }
    public static boolean isOre(BlockState s){return s.is(net.minecraft.tags.BlockTags.ORES);}

    public static void breakWithPowers(ServerLevel level,ServerPlayer player,BlockPos pos,ItemStack tool){
        BlockState state=level.getBlockState(pos); if(state.isAir())return;
        int silk=power(tool,SILK_KEY), fortune=power(tool,FORTUNE_KEY);
        boolean active=silk>0||fortune>0||power(tool,EFFICIENCY_KEY)>0||flag(tool,EARTH_EFFICIENCY_KEY);
        if(!active)return;
        BlockEntity be=level.getBlockEntity(pos); boolean ore=isOre(state);
        if(silk>0 && !(ore&&power(tool,EFFICIENCY_KEY)>0)){
            ItemStack clone=new ItemStack(state.getBlock().asItem());
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
            if(!clone.isEmpty())Block.popResource(level,pos,clone);
            state.spawnAfterBreak(level,pos,tool,true);
            damageToolIfNeeded(player,tool);
            return;
        }
        List<ItemStack> drops=Block.getDrops(state,level,pos,be,player,tool);
        level.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        for(ItemStack d:drops){
            if(ore&&fortune>0){
                int mult=1+level.getRandom().nextInt(fortune+1);
                d=d.copyWithCount(Math.min(d.getMaxStackSize(),d.getCount()*mult));
            }
            if(!d.isEmpty())Block.popResource(level,pos,d);
        }
        state.spawnAfterBreak(level,pos,tool,true);
        damageToolIfNeeded(player,tool);
    }
    private static void damageToolIfNeeded(ServerPlayer player, ItemStack tool){
        if(flag(tool,UNBREAKABLE_KEY))return;
        if(tool.isDamageableItem())tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }

    public static void wardenBurst(ServerLevel level,ServerPlayer player,ItemStack source){
        Vec3 origin=player.getEyePosition(),dir=player.getViewVector(1);
        LivingEntity hit=null; double best=48*48;
        for(Entity e:level.getEntities(player,player.getBoundingBox().inflate(48),x->x instanceof LivingEntity&&x!=player&&x.isAlive())){
            LivingEntity living=(LivingEntity)e; Vec3 c=living.getBoundingBox().getCenter(), rel=c.subtract(origin);
            double forward=rel.dot(dir); if(forward<0||forward>48)continue;
            Vec3 projected=origin.add(dir.scale(forward)); double side=c.distanceToSqr(projected);
            if(side<4&&forward*forward<best){hit=living;best=forward*forward;}
        }
        for(int i=1;i<=32;i++){Vec3 p=origin.add(dir.scale(i*1.5));level.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,p.x,p.y,p.z,1,0,0,0,0);}
        level.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM,net.minecraft.sounds.SoundSource.PLAYERS,2,1);
        if(hit!=null){
            float damage=12+Math.min(40,power(source,WARDEN_KEY)*4);
            hit.hurtServer(level,level.damageSources().sonicBoom(player),damage);
            Vec3 push=dir.scale(2.2);hit.push(push.x,.45,push.z);
        }
    }

    public static void levitationTick(ServerPlayer p){
        boolean has=flag(p.getMainHandItem(),FEATHER_KEY)||flag(p.getOffhandItem(),FEATHER_KEY)||p.getInventory().contains(s->flag(s,FEATHER_KEY));
        if(!has)return;
        p.fallDistance=0;
        if(!p.onGround()&&!p.isFallFlying()){
            Vec3 v=p.getDeltaMovement(); if(v.y<0.015)p.setDeltaMovement(v.x,0.015,v.z);
        }
    }

    public static void maintainStar(ServerPlayer p){
        if(!p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))return;
        ItemStack star=null;
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            ItemStack s=p.getInventory().getItem(i);
            if(isCosmicStar(s)){if(star==null)star=s;p.getInventory().setItem(i,ItemStack.EMPTY);}
        }
        ItemStack last=p.getInventory().getItem(8);
        if(star==null)star=cosmicStar();
        if(!last.isEmpty()&&!isCosmicStar(last)){
            p.getInventory().setItem(8,ItemStack.EMPTY);
            p.getInventory().placeItemBackInInventory(last,net.minecraft.util.Prediction.SERVER_ONLY);
        }
        p.getInventory().setItem(8,star);
        for(Entity e:p.level().getEntities(p,p.getBoundingBox().inflate(2),x->x instanceof ItemEntity ie&&isCosmicStar(ie.getItem())))e.discard();
    }

    public static void prepareOutputShulker(ServerLevel level,ServerPlayer player,BlockPos pos){
        List<ItemStack> contents=new java.util.ArrayList<>();
        for(int i=0;i<26;i++) contents.add(new ItemStack(Items.DIRT));
        ItemStack paper=new ItemStack(Items.PAPER);
        paper.set(DataComponents.CUSTOM_NAME,Component.literal("Coordenada do Item"));
        CompoundTag pd=new CompoundTag();
        pd.putBoolean("MineGeeksCoordinatePaper",true);
        pd.putInt("X",pos.getX()); pd.putInt("Y",pos.getY()); pd.putInt("Z",pos.getZ());
        setCustomData(paper,pd);
        contents.add(paper);
        ItemStack shulker=Items.DYED_SHULKER_BOX.black().getDefaultInstance();
        shulker.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(contents));
        shulker.set(DataComponents.CUSTOM_NAME,Component.literal("Entrega Cósmica"));
        player.getInventory().placeItemBackInInventory(shulker,net.minecraft.util.Prediction.SERVER_ONLY);
    }
    public static BlockPos randomDeliveryPosition(ServerLevel level,ServerPlayer p){
        RandomSource r=level.getRandom();double a=r.nextDouble()*Math.PI*2,d=Math.sqrt(r.nextDouble())*2000;
        return new BlockPos(Mth.floor(p.getX()+Math.cos(a)*d),Mth.nextInt(r,-50,100),Mth.floor(p.getZ()+Math.sin(a)*d));
    }
    public static BlockPos generateDelivery(ServerLevel level,ServerPlayer p,ItemStack generated){
        BlockPos pos=randomDeliveryPosition(level,p);ChunkPos c=new ChunkPos(pos.getX() >> 4,pos.getZ() >> 4);
        level.getChunk(c.x(),c.z(),ChunkStatus.FULL,true);
        level.setBlock(pos,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,p.getDirection().getOpposite()),Block.UPDATE_ALL);
        BlockEntity e=level.getBlockEntity(pos);if(e instanceof ChestBlockEntity chest){chest.setItem(0,generated.copy());chest.setChanged();}
        prepareOutputShulker(level,p,pos);return pos;
    }
}
