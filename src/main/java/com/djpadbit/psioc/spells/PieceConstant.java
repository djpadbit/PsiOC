package com.djpadbit.psioc.spells;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

public class PieceConstant extends SpellPiece {

	private Object data = null;

	public PieceConstant(Spell spell, Object data) {
		super(spell);
		this.data = data;
	}

	public PieceConstant(Spell spell) {
		super(spell);
	}

	@Override
	public EnumPieceType getPieceType() {
		return EnumPieceType.CONSTANT;
	}

	@Override
	public Class<?> getEvaluationType() {
		return data != null ? data.getClass() : null;
	}

	@Override
	public Object evaluate() {
		return data;
	}

	@Override
	public Object execute(SpellContext context) {
		return null;
	}

}