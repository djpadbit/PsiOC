package com.djpadbit.psioc.components;

import org.apache.commons.lang3.reflect.FieldUtils;

import scala.Option;
import scala.collection.mutable.ArrayBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Arrays;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.lib.LibItemNames;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.arl.item.ItemMod;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.Spell;

import vazkii.psi.common.item.base.IPsiItem;

import li.cil.oc.common.inventory.ComponentInventory;
import li.cil.oc.common.item.data.MicrocontrollerData;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.driver.DeviceInfo.DeviceAttribute;
import li.cil.oc.api.driver.DeviceInfo.DeviceClass;
import li.cil.oc.api.driver.DeviceInfo;
import li.cil.oc.api.Items;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.util.Lifecycle;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.items.ItemCADWrapper;
import com.djpadbit.psioc.spells.PieceConstant;

public class CADComponent extends AbstractManagedEnvironment implements DeviceInfo,ManagedPeripheral {

	public ItemCADWrapper wrapper;
	public Node node;
	public Map<String, String> deviceInfo;

	static private String[] methods;
	static private Map<String, SpellPieceData> methodMap;
	private Map<String, Object> objectMap;

	private static class SpellPieceData {
		public String spell;
		public boolean isTrick;
		public SpellPieceData(String spell, boolean isTrick) {
			this.spell = spell;
			this.isTrick = isTrick;
		}
	}

	public CADComponent(ItemCADWrapper wrapper) {
		this.wrapper = wrapper;
		this.deviceInfo = new HashMap<String, String>();
		this.deviceInfo.put(DeviceAttribute.Class, DeviceClass.System);
		this.deviceInfo.put(DeviceAttribute.Description, "Magic casting device");
		this.deviceInfo.put(DeviceAttribute.Vendor, "Magic Stuff, Inc.");
		this.deviceInfo.put(DeviceAttribute.Product, "CAD");
		this.deviceInfo.put(DeviceAttribute.Capacity, "3"/*String.valueOf(this.wrapper.compInventory.getSizeInventory())*/);
		this.node = Network.newNode(this, Visibility.Network).withComponent("cad").withConnector(1000).create();
		this.objectMap = new HashMap<String,Object>();
	}

	public static void initMethods() {
		methodMap = new HashMap<String, SpellPieceData>();
		Set<String> spells = PsiAPI.spellPieceRegistry.getKeys();
		methods = new String[spells.size()-2+1]; // 2 PsiOC spells peices + print method
		int index = 0;
		Spell tmpspell = new Spell();
		for (String spell : spells) {
			if (spell.startsWith(PsiOC.modid+".")) continue;
			String method = spellToMethod(spell);
			methods[index++] = method;
			Class<? extends SpellPiece> spellpclass = PsiAPI.spellPieceRegistry.getObject(spell);
			SpellPieceData sd;
			try {
				SpellPiece spellp = spellpclass.getConstructor(Spell.class).newInstance(tmpspell);
				sd = new SpellPieceData(spell,spellp.getPieceType()==EnumPieceType.TRICK);
			} catch (Exception e) {sd = new SpellPieceData(spell,false);}
			methodMap.put(method,sd);
		}
		methods[index] = "print";
	}

	public static boolean hasMethod(String name) {
		return methodMap.containsKey(name);
	}

	public static boolean isMethodTrick(String name) {
		if (!methodMap.containsKey(name)) return false;
		return methodMap.get(name).isTrick;
	}

	public void cleanup() {
		objectMap.clear();
	}

	private static String spellToMethod(String spell) {
		return spell.replace(".","_");
	}

	private static String methodToSpell(String method) {
		return methodMap.get(method).spell;
	}

	public Node node() {
		return node;
	}

	public Map<String, String> getDeviceInfo() {
		return deviceInfo;
	}

	public String[] methods() {
		return methods;
	}

	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		PsiOC.logger.info("Invoke on "+method+"("+methodToSpell(method)+")");
		if (method == "print") {
			String st = "Print:";
			for (int i=0;i<args.count();i++) {
				Object obj = args.checkAny(i);
				if (obj instanceof byte[]) {
					st += new String((byte[])obj)+" ";
				} else {
					st += String.valueOf(obj)+" ";
				}
			}
			PsiOC.logger.info(st);
			return new Object[]{null};
		}
		Class<? extends SpellPiece> spellpclass = PsiAPI.spellPieceRegistry.getObject(methodToSpell(method));
		if (spellpclass == null) {
			PsiOC.logger.error("Didn't get spell ?");
			throw new IllegalArgumentException("Coudln't get spell piece");
		}
		SpellPiece spellp = spellpclass.getConstructor(Spell.class).newInstance(wrapper.spell);
		spellp.x = 4;
		spellp.y = 4;
		wrapper.spell.grid.gridData[4][4] = spellp;
		spellp.initParams();
		SpellParam[] spellparms = new SpellParam[spellp.params.size()];
		int nbparms = 0;
		int i = 0;
		for (SpellParam sp : spellp.params.values()) {
			//PsiOC.logger.info("SP "+sp.name+", "+sp.getRequiredTypeString()+", "+String.valueOf(sp.canDisable));
			if (!sp.canDisable) nbparms++;
			spellparms[i++] = sp;
		}
		int nbargs = args.count();
		//PsiOC.logger.info(String.valueOf(nbparms)+" nb needed params, "+String.valueOf(nbargs)+" given");
		if (nbargs < nbparms) throw new IllegalArgumentException("Not enough arguments");
		if (nbargs > spellparms.length || nbargs > 4) throw new IllegalArgumentException("Too many arguments");
		for (i=0;i<nbargs;i++) {
			SpellParam.Side sps = SpellParam.Side.DIRECTIONS[i];
			spellp.paramSides.put(spellparms[i],sps);
			Object obj = args.checkAny(i);
			//PsiOC.logger.info("SP:"+String.valueOf(obj)+", "+obj != null ? String.valueOf(obj.getClass()) : "null");
			if (obj instanceof byte[]) {
				String str = new String((byte[])obj);
				if (objectMap.containsKey(str)) obj = objectMap.get(str);
				else obj = str;
			}
			int sx = 4+sps.offx;
			int sy = 4+sps.offy;
			SpellPiece pc = new PieceConstant(wrapper.spell,obj);
			pc.x = sx;
			pc.y = sy;
			wrapper.spell.grid.gridData[sx][sy] = pc;
			wrapper.spellContext.evaluatedObjects[sx][sy] = obj;
		}
		Object ret = null;
		try {
			ret = spellp.execute(wrapper.spellContext);
		} catch(SpellRuntimeException e) {
			if(!wrapper.spellContext.shouldSuppressErrors()) {
				wrapper.spellContext.caster.sendMessage(new TextComponentTranslation(e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
			}
			throw e;
		}
		//Class<?> evalclass = spellp.getEvaluationType();
		//PsiOC.logger.info("Out class is "+String.valueOf(evalclass));
		if (ret instanceof Boolean || ret instanceof Integer || ret instanceof Double || ret instanceof String) {
			return new Object[]{ret};
		}
		if (ret == null) return new Object[]{null};
		String uuid = UUID.randomUUID().toString();
		objectMap.put(uuid,ret);
		return new Object[]{uuid};
	}

}