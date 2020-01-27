/*
 *  Dynamic Surroundings: Mob Effects
 *  Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.library;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SimpleItemData implements IItemData {

    public static final Reference2ObjectOpenHashMap<ItemClass, SimpleItemData> CACHE = new Reference2ObjectOpenHashMap<>();

    static {
        for (final ItemClass ic : ItemClass.values())
            if (ic.isArmor())
                CACHE.put(ic, new SimpleArmorItemData(ic));
            else
                CACHE.put(ic, new SimpleItemData(ic));
    }

    protected final ItemClass itemClass;

    public SimpleItemData(@Nonnull final ItemClass ic) {
        this.itemClass = ic;
    }

    @Override
    @Nonnull
    public ItemClass getItemClass() {
        return this.itemClass;
    }

    @Override
    public void playEquipSound(@Nonnull final ItemStack stack) {
        this.itemClass.playEquipSound();
    }

    @Override
    public void playSwingSound(@Nonnull final ItemStack stack) {
        this.itemClass.playSwingSound();
    }

    @Override
    public void playUseSound(@Nonnull final ItemStack stack) {
        this.itemClass.playUseSound();
    }

}
