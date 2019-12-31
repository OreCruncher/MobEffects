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

package org.orecruncher.mobeffects.footsteps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.orecruncher.mobeffects.library.Constants;
import org.orecruncher.sndctrl.audio.acoustic.AcousticEvent;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

@OnlyIn(Dist.CLIENT)
public class Association {

	private final BlockState state;
	private final FootStrikeLocation location;
	private final IAcoustic data;
	private final boolean isNotEmitter;

	public Association(@Nonnull final LivingEntity entity, @Nonnull final IAcoustic association) {
		final Vec3d vec = entity.getPositionVector();
		this.state = null;
		this.location = new FootStrikeLocation(entity, vec.x, vec.y + 1, vec.z);
		this.data = association;
		this.isNotEmitter = association == Constants.NOT_EMITTER;
	}

	public Association(@Nonnull final BlockState state, @Nonnull final FootStrikeLocation pos,
			@Nonnull final IAcoustic association) {
		this.state = state;
		this.location = pos;
		this.data = association;
		this.isNotEmitter = association == Constants.NOT_EMITTER;
	}

	public void play(@Nonnull final AcousticEvent event) {
		this.data.playAt(this.location.getStrikePosition(), event);
	}

	@Nonnull
	public FootStrikeLocation getStrikeLocation() {
		return this.location;
	}

	public boolean hasStrikeLocation() {
		return this.location != null;
	}

	@Nullable
	public BlockPos getStepPos() {
		return this.location != null ? this.location.getStepPos() : null;
	}

	public boolean isNotEmitter() {
		return this.isNotEmitter;
	}

}