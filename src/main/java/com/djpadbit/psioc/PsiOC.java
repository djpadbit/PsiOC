package com.djpadbit.psioc;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

//import com.djpadbit.psioc.item.ItemOCSpellBullet;
import com.djpadbit.psioc.spells.PieceOC;
import com.djpadbit.psioc.spells.PieceConstant;
import com.djpadbit.psioc.recipes.SpellBulletRecipe;
import com.djpadbit.psioc.components.CADComponent;
import com.djpadbit.psioc.network.PsiOCPacketHandler;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.common.lib.LibPieceGroups;
import vazkii.psi.common.lib.LibPieceNames;

import li.cil.oc.api.Items;
import li.cil.oc.api.Driver;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Mod(modid = PsiOC.modid, acceptedMinecraftVersions = PsiOC.mcVersion, version = PsiOC.version, dependencies = PsiOC.deps)
public class PsiOC {

	public static final String modid = "psioc";
	public static final String version = "1.0.0";
	public static final String mcVersion = "1.12.2";
	public static final String deps = 	"required-after:psi;" +
										"required-after:opencomputers;" +
										"after:rpsideas;" +
										"after:psiaddons;";
	public static final String nbtPrefix = modid+":";

	public static final Logger logger = LogManager.getLogger(modid);

	@Mod.EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		String ockey = modid + ".ocspell";
		String cstkey = modid + ".constant";
		PsiAPI.registerSpellPiece(ockey, PieceOC.class);
		PsiAPI.simpleSpellTextures.put(ockey, new ResourceLocation(modid, "textures/spell/ocspell.png"));
		PsiAPI.registerSpellPiece(cstkey, PieceConstant.class);
		PsiAPI.simpleSpellTextures.put(cstkey, new ResourceLocation(modid, "textures/spell/ocspell.png"));
		PsiOCPacketHandler.registerHandlers();
	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		SpellBulletRecipe.itemEEPROM = Items.get("eeprom").item();
		GameRegistry.findRegistry(IRecipe.class).register(new SpellBulletRecipe().setRegistryName(new ResourceLocation(modid, "spellBulletEEPROM")));
		CADComponent.initMethods();
	}
}
