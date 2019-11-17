package com.djpadbit.psioc.recipes;

import net.minecraft.item.crafting.IRecipe;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.oredict.OreDictionary;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.item.ItemSpellBullet;
import vazkii.psi.common.item.ItemSpellDrive;

import com.djpadbit.psioc.spells.PieceOC;
import com.djpadbit.psioc.PsiOC;

public class SpellBulletRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	public static Item itemEEPROM;

	private ItemStack[] getitms(InventoryCrafting inv) {
		ItemStack[] stacks = new ItemStack[2];
		stacks[0] = ItemStack.EMPTY; // Spell Bullet
		stacks[1] = ItemStack.EMPTY; // EEPROM

		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itmstack = inv.getStackInSlot(i);

			if (!itmstack.isEmpty()) {
				Item itm = itmstack.getItem();
				if (itm instanceof ItemSpellBullet && stacks[0].isEmpty()) {
					stacks[0] = itmstack;
				} else if (itemEEPROM.getClass().isInstance(itm) && stacks[1].isEmpty()) {
					stacks[1] = itmstack;
				}
			}
		}

		return stacks;
	}
	
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack[] stacks = getitms(inv);
		if (stacks[0].isEmpty() || stacks[1].isEmpty()) return false;
		return true;
	}

	public static String getEEPROMName(ItemStack eeprom) {
		String name = "OC Spell";
		if (eeprom.hasTagCompound()) {
			NBTTagCompound tag = eeprom.getTagCompound();
			if (tag.hasKey("oc:data")) {
				NBTTagCompound data = tag.getCompoundTag("oc:data");
				if (data.hasKey("oc:label")) {
					name = data.getString("oc:label");
				}
			}
		}
		return name;
	}

	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack[] stacks = getitms(inv);
		if (stacks[0].isEmpty() || stacks[1].isEmpty()) return ItemStack.EMPTY;
		ItemStack itmstack = stacks[0].copy();
		if (!itmstack.hasTagCompound()) itmstack.setTagCompound(new NBTTagCompound());
		Spell spell = new Spell();
		spell.name = getEEPROMName(stacks[1]);
		PieceOC sp = new PieceOC(spell);
		sp.currentEEPROM = stacks[1].copy();
		sp.isReal = true;
		spell.grid.gridData[0][0] = sp;
		ItemSpellBullet sb = (ItemSpellBullet)itmstack.getItem();
		sb.setSpell(null, itmstack, spell);
		return itmstack;
	}

	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		ItemStack[] stacks = getitms(inv);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itmstack = inv.getStackInSlot(i);

			if (!itmstack.isEmpty()) {
				if (itemEEPROM.getClass().isInstance(itmstack.getItem())) {
					if (!stacks[0].isEmpty()) {
						Spell spell = ItemSpellDrive.getSpell(stacks[0]);
						if (spell != null) {
							PieceOC sp = (PieceOC)spell.grid.gridData[0][0];
							if (sp.currentEEPROM != ItemStack.EMPTY)
								nonnulllist.set(i, sp.currentEEPROM.copy());
						}
					}
					break;
				}
			}
		}

		return nonnulllist;
	}

	public boolean isDynamic() {
		return true;
	}
}