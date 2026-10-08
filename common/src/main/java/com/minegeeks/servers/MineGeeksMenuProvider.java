package com.minegeeks.servers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
public final class MineGeeksMenuProvider extends SimpleMenuProvider{
 public MineGeeksMenuProvider(){super((id,inv,p)->new MineGeeksMenu(id,inv),Component.literal("MineGeeks For Servers"));}
}
