package com.djpadbit.psioc.mixins;

import com.djpadbit.psioc.PsiOC;
import com.djpadbit.psioc.spells.OCCompiledSpell;
import com.djpadbit.psioc.spells.PieceOC;

import vazkii.psi.common.spell.SpellCompiler;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.EnumSpellStat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpellCompiler.class)
public abstract class MixinSpellCompiler {
	@Shadow
	Spell spell;
	@Shadow
	CompiledSpell compiled;

	@Inject(method = "compile()V", at = @At(value = "NEW", target = "vazkii.psi.api.spell.CompiledSpell"),
			remap = false, cancellable = true)
	private void onCompile(CallbackInfo ci) throws SpellCompilationException {
		if (spell.grid.gridData[0][0] instanceof PieceOC && ((PieceOC)spell.grid.gridData[0][0]).isReal) {
			compiled = new OCCompiledSpell(spell);

			if(compiled.metadata.stats.get(EnumSpellStat.COST) < 0 || compiled.metadata.stats.get(EnumSpellStat.POTENCY) < 0)
				throw new SpellCompilationException(SpellCompilationException.STAT_OVERFLOW);

			if(spell.name == null || spell.name.isEmpty())
				throw new SpellCompilationException(SpellCompilationException.NO_NAME);
			ci.cancel();
		}
	}

}