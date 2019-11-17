package com.djpadbit.psioc.network;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Collections;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.ByteBufInputStream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.spells.PieceOC;
import com.djpadbit.psioc.spells.PieceConstant;
import com.djpadbit.psioc.recipes.SpellBulletRecipe;
import com.djpadbit.psioc.components.CADComponent;
import com.djpadbit.psioc.items.ItemCADWrapper;

import vazkii.psi.api.PsiAPI;

// Unused
public class PsiOCPacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PsiOC.modid);

	private static int packetIDs = 0;

	public static void registerHandlers() {
		//INSTANCE.registerMessage(SyncPacketHandlerClient.class, SyncPacket.class, packetIDs++, Side.CLIENT);
	}

	public static void sendSyncPacket(EntityPlayerMP player, ItemStack stack, ItemStack eeprom, boolean state) {
		INSTANCE.sendTo(new SyncPacket(stack,eeprom,state),player);
	}

	public static class SyncPacket implements IMessage {
		public SyncPacket() {}

		public ItemStack stack;
		public ItemStack eeprom;
		public boolean state;

		public SyncPacket(ItemStack stack, ItemStack eeprom, boolean state) {
			this.stack = stack;
			this.eeprom = eeprom;
			this.state = state;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			try {
				ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
				CompressedStreamTools.write(stack.writeToNBT(new NBTTagCompound()),bbos);
				CompressedStreamTools.write(eeprom.writeToNBT(new NBTTagCompound()),bbos);
				bbos.writeBoolean(state);
			} catch (Exception e) {
				//PsiOC.logger.error(e);
				e.printStackTrace();
			}
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			try {
				ByteBufInputStream bbis = new ByteBufInputStream(buf);
				DataInputStream dis = new DataInputStream(bbis);
				stack = new ItemStack(CompressedStreamTools.read(dis));
				eeprom = new ItemStack(CompressedStreamTools.read(dis));
				state = bbis.readBoolean();
			} catch (Exception e) {
				//PsiOC.logger.error(e);
				e.printStackTrace();
			}
		}
	}

	public static class SyncPacketHandlerClient implements IMessageHandler<SyncPacket, IMessage> {
		@Override
		public IMessage onMessage(SyncPacket msg, MessageContext ctx) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			Minecraft.getMinecraft().addScheduledTask(() -> {
				ItemCADWrapper wrapper = ItemCADWrapper.get(msg.stack, player);
				wrapper.setEEPROM(msg.eeprom);
			});
			return null;
		}
	}
}
