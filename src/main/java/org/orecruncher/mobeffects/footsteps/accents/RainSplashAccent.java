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

package org.orecruncher.mobeffects.footsteps.accents;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.WorldUtils;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.mobeffects.footsteps.IFootstepAccentProvider;
import org.orecruncher.mobeffects.library.FootstepLibrary;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
class RainSplashAccent implements IFootstepAccentProvider {

    protected final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    @Override
    @Nonnull
    public String getName() {
        return "Rain Splash Accent";
    }

    @Override
    public void provide(@Nonnull final LivingEntity entity, @Nullable final BlockPos blockPos,
                                          @Nonnull final ObjectArray<IAcoustic> in) {
        final World world = entity.getEntityWorld();
        if (world.isRaining()) {
            if (blockPos != null) {
                this.mutable.setPos(blockPos.up());
            } else {
                this.mutable.setPos(entity);
            }

            // Get the precipitation type at the location
            final Biome.RainType rainType = WorldUtils.getCurrentPrecipitationAt(world, this.mutable);
            if (rainType == Biome.RainType.RAIN)
                in.add(FootstepLibrary.getRainSplashAcoustic());
        }
    }

}