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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.sndctrl.audio.Category;
import org.orecruncher.sndctrl.audio.ISoundCategory;
import org.orecruncher.sndctrl.audio.acoustic.AcousticEvent;
import org.orecruncher.sndctrl.audio.acoustic.IAcoustic;
import org.orecruncher.sndctrl.audio.acoustic.NullAcoustic;

import javax.annotation.Resource;

@OnlyIn(Dist.CLIENT)
public final class Constants {
    private Constants() {

    }

    public static final IAcoustic EMPTY = new NullAcoustic(new ResourceLocation(MobEffects.MOD_ID, "empty"));
    public static final IAcoustic NOT_EMITTER = new NullAcoustic(new ResourceLocation(MobEffects.MOD_ID,"not_emitter"));
    public static final IAcoustic MESSY_GROUND = new NullAcoustic(new ResourceLocation(MobEffects.MOD_ID,"messy_ground"));

    public static AcousticEvent WALK = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "walk"), null);
    public static AcousticEvent WANDER = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "wander"), null);
    public static AcousticEvent SWIM = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "swim"), null);
    public static AcousticEvent RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "run"), WALK);
    public static AcousticEvent JUMP = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "jump"), WANDER);
    public static AcousticEvent LAND = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "land"), RUN);
    public static AcousticEvent CLIMB = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "climb"), WALK);
    public static AcousticEvent CLIMB_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "climb_run"), RUN);
    public static AcousticEvent DOWN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "down"), WALK);
    public static AcousticEvent DOWN_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "down_run"), RUN);
    public static AcousticEvent UP = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "up"), WALK);
    public static AcousticEvent UP_RUN = new AcousticEvent(new ResourceLocation(MobEffects.MOD_ID, "up_run"), RUN);

    public static ISoundCategory FOOTSTEPS = new Category("footsteps", () -> 1F);
}
