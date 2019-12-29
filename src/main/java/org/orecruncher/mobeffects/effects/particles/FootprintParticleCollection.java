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

package org.orecruncher.mobeffects.effects.particles;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.opengl.OpenGlUtil;
import org.orecruncher.lib.particles.ParticleCollection;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FootprintParticleCollection extends ParticleCollection {

    public FootprintParticleCollection(@Nonnull final World world, @Nonnull final ResourceLocation tex) {
        super(world, tex);
    }

    @Override
    protected void preRender() {
        super.preRender();
        GlStateManager.depthMask(false);
        OpenGlUtil.setStandardBlend();
    }

    public static final ICollectionFactory FACTORY = FootprintParticleCollection::new;

}