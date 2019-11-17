package com.djpadbit.psioc.spells;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.ISocketableCapability;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemSpellBullet;

import li.cil.oc.api.driver.DriverItem;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.items.ItemCADWrapper;
import com.djpadbit.psioc.components.CADComponent;
import com.djpadbit.psioc.recipes.SpellBulletRecipe;

public class OCCompiledSpell extends CompiledSpell {

	public int state = 0;
	public Spell spell;
	
	public OCCompiledSpell(Spell spell) {
		super(spell);
		this.spell = spell;
	}

	@Override
	public boolean execute(SpellContext context) throws SpellRuntimeException {
		//PsiOC.logger.info("Exec OCCompiledSpell, state = "+String.valueOf(state));
		PieceOC sp = (PieceOC)sourceSpell.grid.gridData[0][0];
		ItemStack cadstack = PsiAPI.getPlayerCAD(context.caster);
		if (cadstack == ItemStack.EMPTY) throw new SpellRuntimeException(SpellRuntimeException.NO_CAD);
		ItemCADWrapper wrapper = ItemCADWrapper.get(cadstack,context.caster);

		if (state==0) {
			wrapper.setEEPROM(sp.currentEEPROM);
			wrapper.setSpell(spell,context);
			wrapper.machine().start();
			context.delay = 1;
			state = 1;
			return true;
		}
		if (!wrapper.machine().isRunning()) {
			String error = wrapper.machine().lastError();
			if (error != null && error != "")
				context.caster.sendMessage(new TextComponentString("Error: "+error).setStyle(new Style().setColor(TextFormatting.RED)));
			((CADComponent)wrapper.cadComponent).cleanup();
			wrapper.setEEPROM(ItemStack.EMPTY);
			wrapper.setSpell(null,null);
			ItemStack bulletIn = cadstack;
			if (context.tool != ItemStack.EMPTY) bulletIn = context.tool;
			if (ISocketableCapability.isSocketable(bulletIn)) {
				ISocketableCapability sock = ISocketableCapability.socketable(bulletIn);
				ItemStack bullet = sock.getBulletInSocket(sock.getSelectedSlot());
				if (bullet != null && bullet != ItemStack.EMPTY) {
					if (bullet.getItem() instanceof ItemSpellBullet) {
						spell.name = SpellBulletRecipe.getEEPROMName(sp.currentEEPROM);
						ItemSpellBullet sb = (ItemSpellBullet)bullet.getItem();
						sb.setSpell(null, bullet, spell);
					} 
				}
			}
			state = 0;
			return false;
		}
		context.delay = 1;
		return true;
	}

	@Override
	public void safeExecute(SpellContext context) {
		if (context.caster.getEntityWorld().isRemote)
			return;

		try {
			if(context.cspell.execute(context))
				PsiAPI.internalHandler.delayContext(context);
		} catch(SpellRuntimeException e) {
			if(!context.shouldSuppressErrors()) {
				context.caster.sendMessage(new TextComponentTranslation(e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
			}
		}
	}
}