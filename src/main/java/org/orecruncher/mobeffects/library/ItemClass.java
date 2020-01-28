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

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.api.sound.IFadableSoundInstance;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.BackgroundSoundInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public enum ItemClass {

    EMPTY(Constants.NONE),    // For things that don't make an equip sound - like AIR
    NONE(Constants.NONE, Constants.NONE, Constants.UTILITY_EQUIP, false),
    LEATHER(Constants.LEATHER_ARMOR_EQUIP, true),
    CHAIN(Constants.CHAIN_ARMOR_EQUIP, true),
    CRYSTAL(Constants.CRYSTAL_ARMOR_EQUIP, true),
    PLATE(Constants.PLATE_ARMOR_EQUIP, true),
    SHIELD(Constants.TOOL_SWING, Constants.SHIELD_USE, Constants.SHIELD_EQUIP, false),
    SWORD(Constants.SWORD_SWING, Constants.NONE, Constants.SWORD_EQUIP, false),
    AXE(Constants.AXE_SWING, Constants.NONE, Constants.AXE_EQUIP, false),
    BOW(Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP, false),
    CROSSBOW(Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP, false),
    TOOL(Constants.TOOL_SWING, Constants.NONE, Constants.TOOL_EQUIP, false),
    BOOK(Constants.BOOK_EQUIP, Constants.BOOK_EQUIP, Constants.BOOK_EQUIP, false),
    POTION(Constants.POTION_EQUIP, Constants.POTION_EQUIP, Constants.POTION_EQUIP, false);

    @Nonnull
    private final ResourceLocation swing;
    @Nonnull
    private final ResourceLocation use;
    @Nonnull
    private final ResourceLocation equip;
    private final boolean isArmor;

    ItemClass(@Nonnull final ResourceLocation sound) {
        this(sound, sound, sound, false);
    }

    ItemClass(@Nonnull final ResourceLocation sound, final boolean isArmor) {
        this(sound, sound, sound, isArmor);
    }

    ItemClass(@Nonnull final ResourceLocation swing, @Nonnull final ResourceLocation use,
              @Nonnull final ResourceLocation equip, final boolean isArmor) {
        this.swing = swing;
        this.use = use;
        this.equip = equip;
        this.isArmor = isArmor;
    }

    public void playSwingSound() {
        play(this.swing);
    }

    public void playUseSound() {
        play(this.use);
    }

    public void playEquipSound() {
        play(this.equip);
    }

    public boolean isArmor() {
        return this.isArmor;
    }

    private void play(@Nullable final ResourceLocation acoustic) {
        if (acoustic == null)
            return;
        final IAcousticFactory factory = Library.resolve(acoustic).getFactory();
        if (factory != null) {
            final IFadableSoundInstance instance = new BackgroundSoundInstance(factory.createSound(), Constants.TOOLBAR);
            instance.noFade();
            AudioEngine.play(instance);
        }
    }

    /**
     * Determines the effective armor class of the Entity. Chest and legs are used
     * to make the determination.
     */
    public static ItemStack effectiveArmorItemStack(@Nonnull final LivingEntity entity) {
        final ItemStack chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        final ItemStack legs = entity.getItemStackFromSlot(EquipmentSlotType.LEGS);
        final ItemClass chestItemClass = ItemLibrary.getItemData(chest).getItemClass();
        final ItemClass legsItemClass = ItemLibrary.getItemData(legs).getItemClass();
        return chestItemClass.compareTo(legsItemClass) > 0 ? chest.copy() : legs.copy();
    }

    /**
     * Gets the armor class of the entities feet.
     */
    public static ItemStack footArmorItemStack(@Nonnull final LivingEntity entity) {
        return entity.getItemStackFromSlot(EquipmentSlotType.FEET);
    }

}
