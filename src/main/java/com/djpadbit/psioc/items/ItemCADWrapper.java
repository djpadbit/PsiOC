package com.djpadbit.psioc.items;

import org.apache.commons.lang3.reflect.FieldUtils;

import scala.Option;
import scala.collection.mutable.ArrayBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import vazkii.psi.common.lib.LibItemNames;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;

import li.cil.oc.common.inventory.ComponentInventory;
import li.cil.oc.common.inventory.ComponentInventory$class;
import li.cil.oc.common.item.data.MicrocontrollerData;
import li.cil.oc.api.Items;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.util.Lifecycle;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.MachineHost;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.components.CADComponent;
import com.djpadbit.psioc.inventory.ComponentInventoryImpl;
import com.djpadbit.psioc.recipes.SpellBulletRecipe;
import com.djpadbit.psioc.network.PsiOCPacketHandler;

// Pretty much just a OpenComputers tablet (most of the code is from there)

@Mod.EventBusSubscriber
public class ItemCADWrapper implements MachineHost {

	public static final String TAG_UUID = PsiOC.nbtPrefix+"uuid";
	public static final String TAG_CAD_COMPONENT = PsiOC.nbtPrefix+"cadComponent";
	public static final String TAG_MACHINE = PsiOC.nbtPrefix+"machine";

	private static ClientCADCache clientCache = new ClientCADCache();
	private static ServerCADCache serverCache = new ServerCADCache();

	World world;
	Machine machine;
	MicrocontrollerData info = new MicrocontrollerData(LibItemNames.CAD);
	public ItemStack stack;
	EntityPlayer player;
	boolean isInit;
	boolean autoSave = true;
	boolean lastRunning = false;
	public ManagedEnvironment cadComponent;
	ComponentInventory compInventory;
	public Spell spell;
	public SpellContext spellContext;

	public ItemCADWrapper(ItemStack stack, EntityPlayer player) {
		this.stack = stack;
		this.player = player;
		this.world = player.world;
		if (!this.world.isRemote) {
			this.machine = li.cil.oc.api.Machine.create(this);
		}
		this.isInit = !this.world.isRemote;
		cadComponent = new CADComponent(this);
		compInventory = new ComponentInventoryImpl(this,this.info,this.stack,this.cadComponent);
		readFromNBT();
		if (!this.world.isRemote) {
			Network.joinNewNetwork(machine.node());
			((Connector)cadComponent.node()).changeBuffer(1000);
			writeToNBT();
		}
	}

	public static int getTier(MicrocontrollerData data) {
		try {
			return (int)FieldUtils.readField(data,"tier",true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
			return -1;
		}
	}

	public static ItemStack[] getComponents(MicrocontrollerData data) {
		try {
			return (ItemStack[])FieldUtils.readField(data,"components",true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
			return null;
		}
	}

	public static int getStoredEnergy(MicrocontrollerData data) {
		try {
			return (int)FieldUtils.readField(data,"storedEnergy",true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
			return -1;
		}
	}

	public static void setTier(MicrocontrollerData data, int tier) {
		try {
			FieldUtils.writeField(data, "tier", tier, true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
		}
	}

	public static void setComponents(MicrocontrollerData data, ItemStack[] components) {
		try {
			FieldUtils.writeField(data, "components", components, true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
		}
	}

	public static void setStoredEnergy(MicrocontrollerData data, int storedEnergy) {
		try {
			FieldUtils.writeField(data, "storedEnergy", storedEnergy, true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
		}
	}

	public static void setCStack(ComponentInventoryImpl data, ItemStack stack) {
		try {
			FieldUtils.writeField(data, "cStack", stack, true);
		} catch (IllegalAccessException e) {
			PsiOC.logger.error(e);
		}
	}

	public void readFromNBT() {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			load(tag);
			if (!world.isRemote) {
				if (tag.hasKey(TAG_CAD_COMPONENT)) cadComponent.load(tag.getCompoundTag(TAG_CAD_COMPONENT));
				if (tag.hasKey(TAG_MACHINE)) machine.load(tag.getCompoundTag(TAG_MACHINE));
			}
		}
	}

	public void writeToNBT() {
		writeToNBT(true);
	}

	public void writeToNBT(boolean clearState) {
		if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = stack.getTagCompound();
		if (!world.isRemote) {
			//if (!tag.hasKey(TAG_MACHINE)) tag.setTag(TAG_MACHINE, new NBTTagCompound());
			NBTTagCompound tmptag = new NBTTagCompound();
			cadComponent.save(tmptag);
			tag.setTag(TAG_CAD_COMPONENT, tmptag);
			tmptag = new NBTTagCompound();
			machine.save(tmptag);
			tag.setTag(TAG_MACHINE, tmptag);
			if (clearState) tag.getCompoundTag(TAG_MACHINE).removeTag("state");
		}
		save(tag);
	}

	@Override
	public Machine machine() {
		return machine;
	}

	@Override
	public Iterable<ItemStack> internalComponents() {
		return Arrays.asList(getComponents(info));
	}

	@Override
	public int componentSlot(String address) {
		Option<ManagedEnvironment>[] comps = compInventory.components();
		for (int i=0;i<comps.length;i++) {
			ManagedEnvironment env = comps[i].get();
			if (env.node().address() == address) return i;
		}
		return -1;
	}

	@Override
	public void onMachineConnect(Node node) {
		compInventory.onConnect(node);
	}

	@Override
	public void onMachineDisconnect(Node node) {
		compInventory.onDisconnect(node);
	}

	@Override
	public World world() {
		return world;
	}

	@Override
	public double xPosition() {
		return player.posX;
	}

	@Override
	public double yPosition() {
		return player.posY;
	}

	@Override
	public double zPosition() {
		return player.posZ;
	}

	@Override
	public void markChanged() {

	}

	void load(NBTTagCompound nbt) {
		info.load(nbt);
	}

	void save(NBTTagCompound nbt) {
		compInventory.saveComponents();
		info.save(nbt);
	}

	public void update(ItemStack stack, World world, EntityPlayer player, int slot, boolean selected) {
		this.player = player;
		this.stack = stack;
		setCStack((ComponentInventoryImpl)compInventory,stack);
		if (!isInit) {
			isInit = true;
			compInventory.connectComponents();
		}
		if (!world.isRemote) {
			if (machine == null || machine.node() == null) {
				PsiOC.logger.error("No machine or node ?");
				return;
			}
			((Connector)machine.node()).changeBuffer(1000);
			machine.update();
			compInventory.updateComponents();
			if (lastRunning != machine.isRunning()) {
				compInventory.markDirty();
			}
		}
	}

	public void setSpell(Spell sp,SpellContext spctx) {
		spell = sp;
		spellContext = spctx;
	}

	public void setEEPROM(ItemStack eeprom) {
		if (eeprom == null) eeprom = ItemStack.EMPTY;
		compInventory.setInventorySlotContents(2,eeprom);
		/*if (!player.world.isRemote) {
			PsiOC.logger.info("Sync packet sent "+String.valueOf((EntityPlayerMP)player)+", "+String.valueOf(stack)+", "+String.valueOf(eeprom)+", "+String.valueOf(machine.isRunning()));
			PsiOCPacketHandler.sendSyncPacket((EntityPlayerMP)player,stack,eeprom,machine.isRunning());
		}*/
	}

	public static String getId(ItemStack stack) {
		if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey(TAG_UUID)) tag.setString(TAG_UUID,UUID.randomUUID().toString());
		return tag.getString(TAG_UUID);
	}

	public static boolean hasData(ItemStack stack) {
		if (!stack.hasTagCompound()) return false;
		NBTTagCompound tag = stack.getTagCompound();
		if (!tag.hasKey("oc:tier") || !tag.hasKey("oc:components") || !tag.hasKey("oc:storedEnergy")) return false;
		return true;
	}

	public static void addData(ItemStack stack) {
		MicrocontrollerData data = new MicrocontrollerData(LibItemNames.CAD);
		ItemStack[] components = new ItemStack[2];
		components[0] = Items.get("cpu1").createItemStack(1);
		components[1] = Items.get("ram1").createItemStack(1);
		setComponents(data,components);
		setStoredEnergy(data,1000);
		data.save(stack);
	}

	public static ItemCADWrapper get(ItemStack stack, EntityPlayer player) {
		if (player.world.isRemote) return clientCache.get(stack, player);
		return serverCache.get(stack,player);
	}

	@SubscribeEvent
	public static void onWorldSave(WorldEvent.Save e) {
		serverCache.saveAll(e.getWorld());
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload e) {
		clientCache.clear(e.getWorld());
		serverCache.clear(e.getWorld());
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent e) {
		clientCache.cleanUp();
		if (FMLCommonHandler.instance().getMinecraftServerInstance() instanceof IntegratedServer) {
			if (Minecraft.getMinecraft().isGamePaused()) {
				clientCache.keepAlive();
				serverCache.keepAlive();
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent e) {
		serverCache.cleanUp();
	}

	private static abstract class CADCache implements Callable<ItemCADWrapper>,RemovalListener<String, ItemCADWrapper> {

		protected int timeout = 10;
		public Cache<String, ItemCADWrapper> cache = CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS).
																				removalListener(this).build();
		private ItemStack currentStack;
		private EntityPlayer currentHolder;

		public ItemCADWrapper get(ItemStack stack, EntityPlayer holder) {
			String id = getId(stack);
			synchronized(cache) {
				currentStack = stack;
				currentHolder = holder;
				
				// Client shit?

				try {
					ItemCADWrapper wrapper = cache.get(id, this);
	
					if (holder.world != wrapper.world) {
						wrapper.writeToNBT(false);
						wrapper.autoSave = false;
						cache.invalidate(id);
						cache.cleanUp();
						wrapper = cache.get(id,this);
					}

					currentStack = null;
					currentHolder = null;

					wrapper.stack = stack;
					wrapper.player = holder;
					return wrapper;
				} catch (Exception e) {
					PsiOC.logger.error(e);
					currentStack = null;
					currentHolder = null;
					return null;
				}
			}
		}

		public ItemCADWrapper call() {
			if (!hasData(currentStack))
				addData(currentStack);
			return new ItemCADWrapper(currentStack,currentHolder);
		}

		public void onRemoval(RemovalNotification<String, ItemCADWrapper> e) {
			ItemCADWrapper wrapper = e.getValue();
			if (wrapper.compInventory.node() != null) {
				if (wrapper.autoSave) wrapper.writeToNBT();
				wrapper.machine.stop();
				for (Node node : wrapper.machine.node().network().nodes()) {
					node.remove();
				}
				if (wrapper.autoSave) wrapper.writeToNBT();
				wrapper.compInventory.markDirty();
			}
		}

		public void clear(World world) {
			synchronized(cache) {
				Map<String, ItemCADWrapper> inWorld = cache.asMap().entrySet().stream().
											filter(map -> map.getValue().world == world).
											collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
				cache.invalidateAll(inWorld.keySet());
				cache.cleanUp();
			}
		}

		public void cleanUp() {
			synchronized(cache) {
				cache.cleanUp();
			}
		}

		public ImmutableMap<String, ItemCADWrapper> keepAlive() {
			return cache.getAllPresent(cache.asMap().keySet());
		}
	}

	private static class ClientCADCache extends CADCache {
		public ItemCADWrapper getWeak(ItemStack stack) {
			String key = getId(stack);
			Map<String, ItemCADWrapper> map = cache.asMap();
			if (map.containsKey(key)) return map.get(key);
			return null;
		}

		public ItemCADWrapper get(ItemStack stack) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_UUID)) {
				String id = stack.getTagCompound().getString(TAG_UUID);
				synchronized(cache) {
					return cache.getIfPresent(id);
				}
			}
			return null;
		}
	}

	private static class ServerCADCache extends CADCache {
		public void saveAll(World world) {
			synchronized(cache) {
				for (ItemCADWrapper wrapper : cache.asMap().values()) {
					if (wrapper.world == world) wrapper.writeToNBT();
				}
			}
		}
	}

}