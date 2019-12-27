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
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.footsteps.facade.FacadeHelper;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.audio.acoustic.SimultaneousAcoustic;

@OnlyIn(Dist.CLIENT)
public class AcousticResolver {

	private static final ResourceLocation ADHOC = new ResourceLocation(MobEffects.MOD_ID, "adhoc");

	protected final BlockState airState = Blocks.AIR.getDefaultState();
	protected final IWorldReader world;
	protected final FootStrikeLocation loc;
	protected final double distanceToCenter;

	protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

	public AcousticResolver(@Nonnull final IWorldReader world,
			@Nonnull final FootStrikeLocation loc, final double distanceToCenter) {
		this.world = world;
		this.loc = loc;
		this.distanceToCenter = distanceToCenter;
	}

	protected BlockState getBlockStateFacade(@Nonnull final Vec3d pos) {
		return FacadeHelper.resolveState(this.loc.getEntity(), getBlockState(pos), this.world, pos, Direction.UP);
	}

	protected BlockState getBlockState(@Nonnull final Vec3d pos) {
		return this.world.getBlockState(new BlockPos(pos));
	}

	/**
	 * Find an association for an entity, and a location. This will try to find the
	 * best matching block on that location, or near that location, for instance if
	 * the player is walking on the edge of a block when walking over non-emitting
	 * blocks like air or water)
	 *
	 * Returns null if no blocks are valid emitting blocks. Returns a string that
	 * begins with "_NO_ASSOCIATION" if a matching block was found, but has no
	 * association in the blockmap.
	 */
	@Nullable
	public Association findAssociationForEvent() {

		final Vec3d pos = this.loc.getStrikePosition();

		Association worked = resolve(pos);

		// If it didn't work, the player has walked over the air on the border
		// of a block.
		// ------ ------ --> z
		// | o | < player is here
		// wool | air |
		// ------ ------
		// |
		// V z
		if (worked == null) {
			// Create a trigo. mark contained inside the block the player is
			// over
			final LivingEntity entity = this.loc.getEntity();
			final BlockPos adj = new BlockPos(pos);
			final double xdang = (entity.posX - adj.getX()) * 2 - 1;
			final double zdang = (entity.posZ - adj.getZ()) * 2 - 1;
			// -1 0 1
			// ------- -1
			// | o |
			// | + | 0 --> x
			// | |
			// ------- 1
			// |
			// V z

			// If the player is at the edge of that
			if (Math.max(MathStuff.abs(xdang), MathStuff.abs(zdang)) > this.distanceToCenter) {
				// Find the maximum absolute value of X or Z
				final boolean isXdangMax = MathStuff.abs(xdang) > MathStuff.abs(zdang);
				// --------------------- ^ maxofZ-
				// | . . |
				// | . . |
				// | o . . |
				// | . . |
				// | . |
				// < maxofX- maxofX+ >
				// Take the maximum border to produce the sound
				if (isXdangMax) {
					// If we are in the positive border, add 1,
					// else subtract 1
					worked = resolve(xdang > 0 ? this.loc.east() : this.loc.west());
				} else {
					worked = resolve(zdang > 0 ? this.loc.south() : this.loc.north());
				}

				// If that didn't work, then maybe the footstep hit in the
				// direction of walking. Try with the other closest block
				if (worked == null) {
					// Take the maximum direction and try with
					// the orthogonal direction of it
					if (isXdangMax) {
						worked = resolve(zdang > 0 ? this.loc.south() : this.loc.north());
					} else {
						worked = resolve(xdang > 0 ? this.loc.east() : this.loc.west());
					}
				}
			}
		}
		return worked;
	}

	@Nullable
	protected Association resolve(@Nonnull Vec3d vec) {
		BlockState in = null;
		IAcoustic acoustics = Constants.EMPTY;

		Vec3d tPos = vec.add(0, 1, 0);
		final BlockState above = getBlockState(tPos);

		if (above != this.airState)
			acoustics = FootstepLibrary.getBlockAcoustics(above, Substrate.CARPET);

		if (acoustics == Constants.NOT_EMITTER || acoustics == Constants.EMPTY) {
			// This condition implies that if the carpet is NOT_EMITTER, solving
			// will CONTINUE with the actual block surface the player is walking
			// on NOT_EMITTER carpets will not cause solving to skip

			in = getBlockStateFacade(vec);
			if (in == this.airState) {
				tPos = vec.add(0, -1, 0);
				final BlockState below = getBlockState(tPos);
				acoustics = FootstepLibrary.getBlockAcoustics(below, Substrate.FENCE);
				if (acoustics != Constants.EMPTY) {
					vec = tPos;
					in = below;
				}
			}

			if (acoustics == Constants.EMPTY) {
				acoustics = FootstepLibrary.getBlockAcoustics(in);
			}

			if (acoustics != Constants.NOT_EMITTER) {
				// This condition implies that foliage over a NOT_EMITTER block
				// CANNOT PLAY This block most not be executed if the association
				// is a carpet => this block of code is here, not outside this
				// if else group.

				if (above != this.airState) {
					final IAcoustic foliage = FootstepLibrary.getBlockAcoustics(above, Substrate.FOLIAGE);
					if (foliage != Constants.NOT_EMITTER) {
						final SimultaneousAcoustic a = new SimultaneousAcoustic(ADHOC);
						a.add(acoustics);
						a.add(foliage);
						acoustics = a;
					}
				}
			}
		} else {
			vec = tPos;
			in = above;
		}

		if (acoustics == Constants.NOT_EMITTER) {
			// Player has stepped on a non-emitter block as defined in the blockmap
			return null;
		} else {
			// Let's play the fancy acoustics we have defined for the block
			return new Association(in, this.loc.rebase(new BlockPos(vec)), acoustics);
		}
	}

}
