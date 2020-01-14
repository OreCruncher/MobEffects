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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.opengl.OpenGlUtil;
import org.orecruncher.lib.particles.IParticleMote;
import org.orecruncher.lib.particles.ParticleCollectionHelper;
import org.orecruncher.lib.particles.ParticleRenderType;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.footsteps.FootprintStyle;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = MobEffects.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Collections {

    private static final ParticleRenderType FOOTPRINT_RENDER =
            new ParticleRenderType(new ResourceLocation(MobEffects.MOD_ID, "textures/particles/footprint.png")) {
                @Override
                public void beginRender(@Nonnull final BufferBuilder buffer, @Nonnull final TextureManager textureManager) {
                    super.beginRender(buffer, textureManager);
                    GlStateManager.depthMask(false);
                    OpenGlUtil.setStandardBlend();
                }
            };

    private final static ParticleCollectionHelper thePrints =
            new ParticleCollectionHelper(
                    "Footprints",
                    FOOTPRINT_RENDER);

    private Collections() {

    }

    public static void addFootprint(@Nonnull final FootprintStyle style, @Nonnull final World world,
                                    final Vec3d loc, final float rot, final float scale, final boolean isRight) {
        if (thePrints.get().canFit()) {
            final IParticleMote mote = new FootprintMote(style, world, loc.x, loc.y, loc.z, rot, scale, isRight);
            thePrints.get().addParticle(mote);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(@Nonnull final WorldEvent.Unload event) {
        if (event.getWorld() instanceof ClientWorld) {
            thePrints.clear();
        }
    }
}
