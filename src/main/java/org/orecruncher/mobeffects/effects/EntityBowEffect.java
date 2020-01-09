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

package org.orecruncher.mobeffects.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.effects.AbstractEntityEffect;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.IItemData;
import org.orecruncher.mobeffects.library.ItemClass;
import org.orecruncher.mobeffects.library.ItemLibrary;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

@OnlyIn(Dist.CLIENT)
public class EntityBowEffect extends AbstractEntityEffect {

    private static final ResourceLocation NAME = new ResourceLocation(MobEffects.MOD_ID, "bow");
    public static final FactoryHandler FACTORY = new FactoryHandler(
            EntityBowEffect.NAME,
            entity -> new EntityBowEffect());

    protected ItemStack lastActiveStack = ItemStack.EMPTY;

    public EntityBowEffect() {
        super(NAME);
    }

    @Override
    public void update() {
        final LivingEntity entity = (LivingEntity) getEntity();
        final ItemStack currentStack = entity.getActiveItemStack();
        if (!currentStack.isEmpty()) {
            if (!ItemStack.areItemStacksEqual(currentStack, this.lastActiveStack)) {
                final IItemData data = ItemLibrary.getItemData(currentStack);
                final ItemClass itemClass = data.getItemClass();
                if (itemClass == ItemClass.BOW || itemClass == ItemClass.CROSSBOW || itemClass == ItemClass.SHIELD) {
                    final IAcoustic soundEffect = data.getUseSound(currentStack);
                    soundEffect.playAt(entity.getPosition());
                }

                this.lastActiveStack = currentStack;
            }

        } else {
            this.lastActiveStack = ItemStack.EMPTY;
        }
    }

}