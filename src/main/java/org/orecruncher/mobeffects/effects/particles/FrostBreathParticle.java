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

import net.minecraft.client.particle.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class FrostBreathParticle extends SpriteTexturedParticle {
    private final IAnimatedSprite field_217583_C;

    public FrostBreathParticle(@Nonnull final LivingEntity entity) {
        super(entity.getEntityWorld(), 0, 0, 0, 0.0D, 0.0D, 0.0D);

        // Reuse the cloud sheet
        this.field_217583_C = GameUtils.getMC().particles.sprites.get(ParticleTypes.CLOUD.getRegistryName());

        final Vec3d origin = ParticleUtils.getBreathOrigin(entity);
        final Vec3d trajectory = ParticleUtils.getLookTrajectory(entity);

        this.setPosition(origin.x, origin.y, origin.z);
        this.prevPosX = origin.x;
        this.prevPosY = origin.y;
        this.prevPosZ = origin.z;

        this.motionX = trajectory.x * 0.01D;
        this.motionY = trajectory.y * 0.01D;
        this.motionZ = trajectory.z * 0.01D;

        this.setAlphaF(0.2F);
        float f1 = 1.0F - (float)(Math.random() * (double)0.3F);
        this.particleRed = f1;
        this.particleGreen = f1;
        this.particleBlue = f1;
        this.particleScale *= 1.875F * (entity.isChild() ? 0.125F : 0.25F);
        int i = (int)(8.0D / (Math.random() * 0.8D + 0.3D));
        this.maxAge = (int)Math.max((float)i * 2.5F, 1.0F);
        this.canCollide = false;
        this.selectSpriteWithAge(this.field_217583_C);
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public float getScale(float p_217561_1_) {
        return this.particleScale * MathHelper.clamp(((float)this.age + p_217561_1_) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.selectSpriteWithAge(this.field_217583_C);
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.96F;
            this.motionY *= 0.96F;
            this.motionZ *= 0.96F;

            if (this.onGround) {
                this.motionX *= 0.7F;
                this.motionZ *= 0.7F;
            }

        }
    }
}