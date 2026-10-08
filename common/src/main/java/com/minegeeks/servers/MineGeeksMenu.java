package com.minegeeks.servers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public final class MineGeeksMenu extends ChestMenu{
    private static final int SIZE=54,INPUT=13; private final SimpleContainer container;
    public MineGeeksMenu(int id,Inventory inv){this(id,inv,new SimpleContainer(SIZE));}
    public MineGeeksMenu(int id,Inventory inv,SimpleContainer c){super(MenuType.GENERIC_9x6,id,inv,c,6);container=c;refresh();}
    private ItemStack button(Item item,String title,String... lore){
        ItemStack s=new ItemStack(item);s.set(DataComponents.CUSTOM_NAME,Component.literal(title));
        s.set(DataComponents.LORE,new ItemLore(java.util.Arrays.stream(lore).map(Component::literal).toList()));return s;
    }
    private void refresh(){
        for(int i=0;i<SIZE;i++)if(i!=INPUT)container.setItem(i,button(Items.GRAY_STAINED_GLASS_PANE," "));
        ItemStack d=container.getItem(INPUT);
        container.setItem(4,button(Items.NETHER_STAR,"GERADOR MineGeeks","Item no slot central.","Encantamentos e poderes próprios."));
        container.setItem(45,button(Items.NETHER_STAR,"Silk Touch","Nível: "+MineGeeksCore.power(d,MineGeeksCore.SILK_KEY),"0/1"));
        container.setItem(46,button(Items.EMERALD,"Fortuna","Nível: "+MineGeeksCore.power(d,MineGeeksCore.FORTUNE_KEY),"0-10"));
        container.setItem(47,button(Items.IRON_PICKAXE,"Eficiência","Nível: "+MineGeeksCore.power(d,MineGeeksCore.EFFICIENCY_KEY),"0-10"));
        container.setItem(48,button(Items.BEDROCK,"INQUEBRÁVEL","Ativo: "+MineGeeksCore.flag(d,MineGeeksCore.UNBREAKABLE_KEY),"Sem durabilidade; fogo/cactus/explosões."));
        container.setItem(49,button(Items.GRASS_BLOCK,"Eficiência em Terra/Grama","Ativo: "+MineGeeksCore.flag(d,MineGeeksCore.EARTH_EFFICIENCY_KEY),"Afeta terra, grama, areia e cascalho."));
        container.setItem(50,button(Items.SCULK_SHRIEKER,"Rajada de Warden","Nível: "+MineGeeksCore.power(d,MineGeeksCore.WARDEN_KEY),"Usável por qualquer portador."));
        container.setItem(51,button(Items.FEATHER,"Pena Cósmica","Ativo: "+MineGeeksCore.flag(d,MineGeeksCore.FEATHER_KEY),"0,3 bloco/s, sem efeito, sem dano de queda."));
        container.setItem(52,button(Items.REDSTONE,"LIMPAR","Remove poderes MineGeeks."));
        container.setItem(53,button(Items.END_CRYSTAL,"GERAR","Baú remoto + shulker preta + coordenada."));
    }
    private void action(int slot,Player p){
        ItemStack d=container.getItem(INPUT);if(d.isEmpty())return;
        switch(slot){
            case 45->MineGeeksCore.toggleLevel(d,MineGeeksCore.SILK_KEY,1);
            case 46->MineGeeksCore.toggleLevel(d,MineGeeksCore.FORTUNE_KEY,10);
            case 47->MineGeeksCore.toggleLevel(d,MineGeeksCore.EFFICIENCY_KEY,10);
            case 48->MineGeeksCore.toggleFlag(d,MineGeeksCore.UNBREAKABLE_KEY);
            case 49->MineGeeksCore.toggleFlag(d,MineGeeksCore.EARTH_EFFICIENCY_KEY);
            case 50->MineGeeksCore.toggleLevel(d,MineGeeksCore.WARDEN_KEY,10);
            case 51->MineGeeksCore.toggleFlag(d,MineGeeksCore.FEATHER_KEY);
            case 52->{MineGeeksCore.setCustomData(d,new net.minecraft.nbt.CompoundTag());d.remove(DataComponents.CUSTOM_NAME);}
            case 53->generate(p);
        }
        refresh();broadcastChanges();
    }
    private void generate(Player p){
        if(!(p instanceof ServerPlayer sp))return;ItemStack d=container.getItem(INPUT);if(d.isEmpty())return;
        ItemStack out=MineGeeksCore.makeGenerated(d);
        if(MineGeeksCore.flag(out,MineGeeksCore.UNBREAKABLE_KEY)){
            out.set(DataComponents.UNBREAKABLE,net.minecraft.world.item.component.Unbreakable.EMPTY);
            out.set(DataComponents.FIRE_RESISTANT,true);
        }
        ServerLevel level=sp.serverLevel();var pos=MineGeeksCore.generateDelivery(level,sp,out);
        container.setItem(INPUT,ItemStack.EMPTY);sp.sendSystemMessage(Component.literal("MineGeeks: enviado para X="+pos.getX()+" Y="+pos.getY()+" Z="+pos.getZ()));sp.closeContainer();
    }
    @Override public void clicked(int slot,int button,ClickType type,Player p){
        if(slot>=45&&slot<=53){action(slot,p);return;} if(slot>=0&&slot<SIZE&&slot!=INPUT)return;super.clicked(slot,button,type,p);
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<SIZE)return ItemStack.EMPTY;ItemStack s=p.getInventory().getItem(index-SIZE);if(s.isEmpty()||!container.getItem(INPUT).isEmpty())return ItemStack.EMPTY;
        ItemStack c=s.copyWithCount(1);container.setItem(INPUT,c);s.shrink(1);refresh();broadcastChanges();return c;
    }
    @Override public void removed(Player p){super.removed(p);ItemStack in=container.getItem(INPUT);if(!in.isEmpty()){p.getInventory().placeItemBackInInventory(in.copy());container.setItem(INPUT,ItemStack.EMPTY);}}
}
