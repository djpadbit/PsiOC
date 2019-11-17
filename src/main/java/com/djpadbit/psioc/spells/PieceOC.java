package com.djpadbit.psioc.spells;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;

import com.djpadbit.psioc.PsiOC;

public class PieceOC extends SpellPiece {

	public static final String TAG_CURRENT_EEPROM = PsiOC.nbtPrefix+"currentEEPROM";
	public static final String TAG_IS_REAL = PsiOC.nbtPrefix+"isReal";

	public ItemStack currentEEPROM = ItemStack.EMPTY;
	public boolean isReal = false;
	
	public PieceOC(Spell spell) {
		super(spell);
	}

	@Override
	public EnumPieceType getPieceType() {
		return EnumPieceType.TRICK;
	}

	@Override
	public void initParams() {
		// NOOP
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException, ArithmeticException {
		/*meta.addStat(EnumSpellStat.COMPLEXITY, 1);
		meta.addStat(EnumSpellStat.PROJECTION, 1);
		meta.addStat(EnumSpellStat.POTENCY, 8);
		meta.addStat(EnumSpellStat.COST, 8);*/
	}

	@Override
	public Class<?> getEvaluationType() {
		return Null.class;
	}

	@Override
	public Object evaluate() {
		return null;
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound cmp) {
		super.readFromNBT(cmp);
		if (cmp.hasKey(TAG_CURRENT_EEPROM)) currentEEPROM = new ItemStack((NBTTagCompound)cmp.getTag(TAG_CURRENT_EEPROM));
		if (cmp.hasKey(TAG_IS_REAL)) isReal = cmp.getBoolean(TAG_IS_REAL);
	}

	@Override
	public void writeToNBT(NBTTagCompound cmp) {
		super.writeToNBT(cmp);
		if (currentEEPROM != ItemStack.EMPTY) cmp.setTag(TAG_CURRENT_EEPROM, currentEEPROM.serializeNBT());
		cmp.setBoolean(TAG_IS_REAL,isReal);
	}

}