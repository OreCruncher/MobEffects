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
import org.orecruncher.sndctrl.api.acoustics.IAcoustic;
import org.orecruncher.sndctrl.api.acoustics.IAcousticFactory;
import org.orecruncher.sndctrl.api.acoustics.Library;
import org.orecruncher.sndctrl.api.sound.IFadableSoundInstance;
import org.orecruncher.sndctrl.audio.AudioEngine;
import org.orecruncher.sndctrl.audio.BackgroundSoundInstance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ItemData {

    @Nonnull
    private final String name;
    @Nonnull
    private final ResourceLocation swing;
    @Nonnull
    private final ResourceLocation use;
    @Nonnull
    private final ResourceLocation equip;

    ItemData(@Nonnull final String name, @Nonnull final ResourceLocation sound) {
        this(name, sound, sound, sound);
    }

    ItemData(@Nonnull final String name, @Nonnull final ResourceLocation swing, @Nonnull final ResourceLocation use,
             @Nonnull final ResourceLocation equip) {
        this.name = name;
        this.swing = swing;
        this.use = use;
        this.equip = equip;
    }

    /**
     * Determines the effective armor class of the Entity. Chest and legs are used
     * to make the determination.
     */
    public static ItemStack effectiveArmorItemStack(@Nonnull final LivingEntity entity) {
        final ItemStack chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        final ItemStack legs = entity.getItemStackFromSlot(EquipmentSlotType.LEGS);
        final ItemData chestItemData = ItemLibrary.getItemData(chest);
        final ItemData legsItemData = ItemLibrary.getItemData(legs);
        final int chestPriority = chestItemData.isArmor() ? ((ArmorItemData) chestItemData).getPriority() : -1;
        final int legPriority = legsItemData.isArmor() ? ((ArmorItemData) legsItemData).getPriority() : -1;
        return chestPriority > legPriority ? chest.copy() : legs.copy();
    }

    /**
     * Gets the armor class of the entities feet.
     */
    public static ItemStack footArmorItemStack(@Nonnull final LivingEntity entity) {
        return entity.getItemStackFromSlot(EquipmentSlotType.FEET);
    }

    @Nonnull
    public String getName() {
        return this.name;
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
        return this instanceof ArmorItemData;
    }

    protected void play(@Nullable final ResourceLocation acoustic) {
        if (acoustic == null)
            return;
        final IAcousticFactory factory = Library.resolve(acoustic).getFactory();
        if (factory != null) {
            final IFadableSoundInstance instance = new BackgroundSoundInstance(factory.createSound(), Constants.TOOLBAR);
            instance.noFade();
            AudioEngine.play(instance);
        }
    }

    public static class ArmorItemData extends ItemData {

        private final ResourceLocation armor;
        private final ResourceLocation foot;
        private final int priority;

        ArmorItemData(@Nonnull final String name, @Nonnull final ResourceLocation item, @Nonnull final ResourceLocation armor, @Nonnull final ResourceLocation foot, final int priority) {
            super(name, item);
            this.armor = armor;
            this.foot = foot;
            this.priority = priority;
        }

        @Nonnull
        public IAcoustic getArmorSound(@Nonnull final ItemStack stack) {
            return Library.resolve(this.armor);
        }

        @Nonnull
        public IAcoustic getFootArmorSound(@Nonnull final ItemStack stack) {
            return Library.resolve(this.foot);
        }

        public int getPriority() {
            return this.priority;
        }

    }

}
