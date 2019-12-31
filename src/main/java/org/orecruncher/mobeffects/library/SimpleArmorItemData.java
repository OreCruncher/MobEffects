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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.library.AcousticLibrary;

import javax.annotation.Nonnull;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class SimpleArmorItemData extends SimpleItemData implements IArmorItemData {

    private static final Map<ItemClass, ResourceLocation> ARMOR = new Reference2ObjectOpenHashMap<>();
    private static final Map<ItemClass, ResourceLocation> FOOT = new Reference2ObjectOpenHashMap<>();

    static {
        ARMOR.put(ItemClass.LEATHER, Constants.LIGHT_ARMOR);
        ARMOR.put(ItemClass.CHAIN, Constants.MEDIUM_ARMOR);
        ARMOR.put(ItemClass.CRYSTAL, Constants.CRYSTAL_ARMOR);
        ARMOR.put(ItemClass.PLATE, Constants.HEAVY_ARMOR);

        FOOT.put(ItemClass.LEATHER, Constants.LIGHT_FOOT_ARMOR);
        FOOT.put(ItemClass.CHAIN, Constants.MEDIUM_FOOT_ARMOR);
        FOOT.put(ItemClass.CRYSTAL, Constants.CRYSTAL_FOOT_ARMOR);
        FOOT.put(ItemClass.PLATE, Constants.HEAVY_FOOT_ARMOR);
    }

    public SimpleArmorItemData(@Nonnull final ItemClass ic) {
        super(ic);
    }

    @Nonnull
    @Override
    public IAcoustic getArmorSound(@Nonnull final ItemStack stack) {
        return AcousticLibrary.resolve(ARMOR.get(this.itemClass));
    }

    @Nonnull
    @Override
    public IAcoustic getFootArmorSound(@Nonnull final ItemStack stack) {
        return AcousticLibrary.resolve(FOOT.get(this.itemClass));
    }

}