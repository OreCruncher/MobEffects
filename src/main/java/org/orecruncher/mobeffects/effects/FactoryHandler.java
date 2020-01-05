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

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.effects.AbstractEntityEffect;
import org.orecruncher.mobeffects.library.EffectLibrary;
import org.orecruncher.sndctrl.library.EntityEffectLibrary;

import javax.annotation.Nonnull;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class FactoryHandler implements EntityEffectLibrary.IEntityEffectFactoryHandler {

    private final ResourceLocation name;
    private final Function<Entity, AbstractEntityEffect> factory;

    public FactoryHandler(@Nonnull final ResourceLocation res, @Nonnull final Function<Entity, AbstractEntityEffect> factory) {
        this.name = res;
        this.factory = factory;
    }

    @Override
    @Nonnull
    public ResourceLocation getName() {
        return this.name;
    }

    @Override
    public boolean appliesTo(@Nonnull final Entity entity) {
        return EffectLibrary.hasEffect(entity, getName());
    }

    @Override
    @Nonnull
    public AbstractEntityEffect get(@Nonnull final Entity entity) {
        return this.factory.apply(entity);
    }
}
