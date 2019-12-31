/*
 * Dynamic Surroundings: Mob Effects
 * Copyright (C) 2019  OreCruncher
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
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.mobeffects.effects;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.effects.AbstractEntityEffect;
import org.orecruncher.lib.effects.EntityEffectManager;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import org.orecruncher.mobeffects.Config;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;
import org.orecruncher.mobeffects.footsteps.Generator;
import org.orecruncher.mobeffects.library.EffectLibrary;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;

@OnlyIn(Dist.CLIENT)
public class EntityFootprintEffect extends AbstractEntityEffect {

	private static final ResourceLocation NAME = new ResourceLocation(MobEffects.MOD_ID, "footprint");

	protected Generator generator;

	public EntityFootprintEffect() {
		super(NAME);
	}

	@Override
	public void intitialize(@Nonnull final EntityEffectManager state) {
		super.intitialize(state);
		this.generator = FootstepLibrary.createGenerator((LivingEntity)getEntity());
	}

	@Override
	public void update() {
		this.generator.generateFootsteps((LivingEntity) getEntity());
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.generator.getPedometer();
	}

	public static final EntityEffectLibrary.IEntityEffectFactoryHandler DEFAULT_HANDLER = new EntityEffectLibrary.IEntityEffectFactoryHandler() {

		@Override
		public ResourceLocation getName() {
			return EntityFootprintEffect.NAME;
		}

		@Override
		public boolean appliesTo(@Nonnull final Entity entity) {
			//return entity instanceof PlayerEntity;
			return EffectLibrary.hasEffect(entity, getName());
		}

		@Override
		public List<AbstractEntityEffect> get(@Nonnull final Entity entity) {
			return ImmutableList
					.of(entity instanceof PlayerEntity ? new PlayerFootprintEffect() : new EntityFootprintEffect());
		}
	};

	@OnlyIn(Dist.CLIENT)
	private static class PlayerFootprintEffect extends EntityFootprintEffect {

		protected FootprintStyle lastStyle;

		@Override
		public void intitialize(@Nonnull final EntityEffectManager state) {
			super.intitialize(state);
			this.lastStyle = Config.CLIENT.footsteps.get_playerFootprintStyle();
		}

		@Override
		public void update() {
			final FootprintStyle currentStyle = Config.CLIENT.footsteps.get_playerFootprintStyle();
			if (this.lastStyle != currentStyle) {
				this.generator = FootstepLibrary.createGenerator((LivingEntity) getEntity());
				this.lastStyle = currentStyle;
			}
			super.update();
		}

	}

}
