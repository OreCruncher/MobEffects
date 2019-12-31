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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.collections.ObjectArray;
import org.orecruncher.mobeffects.footsteps.IFootstepAccentProvider;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FootstepAccents {

    private FootstepAccents() {

    }

    private static final List<IFootstepAccentProvider> providers = new ArrayList<>();

    static {
        providers.add(new ArmorAccents());
        providers.add(new RainSplashAccent());
    }

    @Nonnull
    public static ObjectArray<IAcoustic> provide(@Nonnull final LivingEntity entity, @Nullable final BlockPos pos, @Nonnull final ObjectArray<IAcoustic> in) {
        providers.forEach(provider -> provider.provide(entity, pos, in));
        return in;
    }
}