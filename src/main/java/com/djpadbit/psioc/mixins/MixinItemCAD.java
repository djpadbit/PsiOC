package com.djpadbit.psioc.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.lib.LibItemNames;
import vazkii.arl.interf.IItemColorProvider;
import vazkii.arl.item.ItemMod;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.ISpellSettable;
import vazkii.psi.common.item.base.IPsiItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import li.cil.oc.common.inventory.ComponentInventory;
import li.cil.oc.common.item.data.MicrocontrollerData;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.machine.MachineHost;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.items.ItemCADWrapper;

@Mixin(ItemCAD.class)
public abstract class MixinItemCAD {

	/*@Inject(method = "setComponent(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), remap = false)
	private static void onSetComponent(ItemStack stack, ItemStack componentStack, CallbackInfo ci) {
		//PsiOC.logger.info("OnSetComponent "+String.valueOf(stack)+","+String.valueOf(componentStack));
	}*/

	@Inject(method = "func_77663_a(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("HEAD"), remap = false)
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected, CallbackInfo ci) {
		if (entityIn instanceof EntityPlayer || entityIn instanceof EntityPlayerMP) {
			ItemCADWrapper.get(stack,(EntityPlayer)entityIn).update(stack, worldIn, (EntityPlayer)entityIn, itemSlot, isSelected);
		}
	}
}