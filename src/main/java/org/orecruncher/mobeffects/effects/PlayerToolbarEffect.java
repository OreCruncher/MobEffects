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

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.effects.AbstractEntityEffect;
import org.orecruncher.lib.effects.EntityEffectManager;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.EffectLibrary;
import org.orecruncher.mobeffects.library.IItemData;
import org.orecruncher.mobeffects.library.ItemLibrary;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;

import javax.annotation.Nonnull;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PlayerToolbarEffect extends AbstractEntityEffect {

    private static final ResourceLocation NAME = new ResourceLocation(MobEffects.MOD_ID, "toolbar");
    public static final EntityEffectLibrary.IEntityEffectFactoryHandler DEFAULT_HANDLER = new EntityEffectLibrary.IEntityEffectFactoryHandler() {

        @Override
        public ResourceLocation getName() {
            return PlayerToolbarEffect.NAME;
        }

        @Override
        public boolean appliesTo(@Nonnull final Entity entity) {
            return EffectLibrary.hasEffect(entity, getName());
        }

        @Override
        public List<AbstractEntityEffect> get(@Nonnull final Entity entity) {
            return ImmutableList.of(new PlayerToolbarEffect());
        }
    };

    protected static class HandTracker {

        protected final Hand hand;
        protected Item lastHeld = null;

        protected HandTracker(@Nonnull final PlayerEntity player) {
            this(player, Hand.OFF_HAND);
        }

        protected HandTracker(@Nonnull final PlayerEntity player, @Nonnull final Hand hand) {
            this.hand = hand;
            this.lastHeld = getItemForHand(player, hand);
        }

        protected Item getItemForHand(final PlayerEntity player, final Hand hand) {
            final ItemStack stack = player.getHeldItem(hand);
            return stack.getItem();
        }

        protected boolean triggerNewEquipSound(@Nonnull final PlayerEntity player) {
            final Item heldItem = getItemForHand(player, this.hand);
            return heldItem != this.lastHeld;
        }

        public void update(@Nonnull final PlayerEntity player) {
            if (triggerNewEquipSound(player)) {
                final ItemStack currentStack = player.getHeldItem(this.hand);
                final IItemData data = ItemLibrary.getItemClass(currentStack);
                final IAcoustic soundEffect = data.getEquipSound(currentStack);
                if (soundEffect != null) {
                    soundEffect.playAt(player.getPositionVec());
                    this.lastHeld = currentStack.getItem();
                }
            }
        }
    }

    protected static class MainHandTracker extends HandTracker {

        protected int lastSlot = -1;

        public MainHandTracker(@Nonnull final PlayerEntity player) {
            super(player, Hand.MAIN_HAND);
            this.lastSlot = player.inventory.currentItem;
        }

        @Override
        protected boolean triggerNewEquipSound(@Nonnull final PlayerEntity player) {
            return this.lastSlot != player.inventory.currentItem || super.triggerNewEquipSound(player);
        }

        @Override
        public void update(@Nonnull final PlayerEntity player) {
            super.update(player);
            this.lastSlot = player.inventory.currentItem;
        }
    }

    protected MainHandTracker mainHand;
    protected HandTracker offHand;

    public PlayerToolbarEffect() {
        super(NAME);
    }

    public void intitialize(@Nonnull EntityEffectManager manager) {
        super.intitialize(manager);
        final PlayerEntity player = (PlayerEntity) getEntity();
        this.mainHand = new MainHandTracker(player);
        this.offHand = new HandTracker(player);
    }

    @Override
    public void update() {
        final PlayerEntity player = (PlayerEntity) getEntity();
        this.mainHand.update(player);
        this.offHand.update(player);
    }

}