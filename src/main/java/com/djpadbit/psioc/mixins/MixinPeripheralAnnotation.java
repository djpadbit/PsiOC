package com.djpadbit.psioc.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import li.cil.oc.common.inventory.ComponentInventory;
import li.cil.oc.common.item.data.MicrocontrollerData;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.machine.MachineHost;
import li.cil.oc.server.machine.PeripheralAnnotation;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.components.CADComponent;

@Mixin(PeripheralAnnotation.class)
public abstract class MixinPeripheralAnnotation {

	private boolean notDirect;

	@Inject(method = "<init>*", at = @At("RETURN"), remap = false)
	public void onConstructor(String name, CallbackInfo ci) {
		notDirect = CADComponent.isMethodTrick(name);
	}

	@Inject(method = "direct", at = @At("HEAD"), remap = false, cancellable = true)
	public void onDirect(CallbackInfoReturnable<Boolean> cir) {
		if (notDirect) cir.setReturnValue(false);
	}
}