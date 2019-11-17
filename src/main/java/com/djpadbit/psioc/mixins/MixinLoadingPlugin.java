package com.djpadbit.psioc.mixins;

import java.util.Map;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// Unused
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class MixinLoadingPlugin implements IFMLLoadingPlugin {

	static {
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.psioc.json");
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(final Map<String, Object> map) {
	}
}