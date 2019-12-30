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

package org.orecruncher.mobeffects.library;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.orecruncher.lib.JsonUtils;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.EntityConfig;
import org.orecruncher.sndctrl.library.AcousticLibrary;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = MobEffects.MOD_ID,
        value = {Dist.CLIENT},
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class EffectLibrary {

    private static final EntityEffectInfo DEFAULT = new EntityEffectInfo();
    private static EntityEffectInfo playerEffects = DEFAULT;
    private static final Map<ResourceLocation, EntityEffectInfo> myEffects = new Object2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Class<? extends Entity>, EntityEffectInfo> effects = new Reference2ObjectOpenHashMap<>();
    private static final Set<ResourceLocation> blockedSounds = new ObjectOpenHashSet<>();

    private EffectLibrary() {

    }

    public static void initialize() {

        myEffects.clear();
        effects.clear();
        blockedSounds.clear();

        // Load up the effects
        final Map<String, EntityConfig> configMap = JsonUtils.loadConfig(new ResourceLocation(MobEffects.MOD_ID, "effects.json"), EntityConfig.class);
        for (final Map.Entry<String, EntityConfig> kvp : configMap.entrySet()) {
            final ResourceLocation loc = AcousticLibrary.resolveResource(MobEffects.MOD_ID, kvp.getKey());
            myEffects.put(loc, new EntityEffectInfo(kvp.getValue()));

            // Process blocked sounds
            for (final String r : kvp.getValue().blockedSounds) {
                try {
                    blockedSounds.add(new ResourceLocation(r));
                } catch (@Nonnull final Throwable t) {
                    MobEffects.LOGGER.error(t, "Not a valid sound resource location: %s", r);
                }
            }
        }

        playerEffects = myEffects.get(new ResourceLocation("minecraft:player"));
    }

    public static boolean hasEffect(@Nonnull final Entity entity, @Nonnull final ResourceLocation loc) {
        return getEffectInfo(entity).effects.contains(loc);
    }

    @Nonnull
    private static EntityEffectInfo getEffectInfo(@Nonnull final Entity entity) {
        if (entity instanceof PlayerEntity)
            return playerEffects;

        EntityEffectInfo info = effects.get(entity.getClass());
        if (info == null) {
            info = myEffects.get(entity.getType().getRegistryName());
            if (info == null) {
                // Slow crawl through looking for aliasing
                for (final Map.Entry<Class<? extends Entity>, EntityEffectInfo> kvp : effects.entrySet()) {
                    if (kvp.getKey().isAssignableFrom(entity.getClass())) {
                        info = kvp.getValue();
                        break;
                    }
                }
                // If it is null we didn't find a class hit so assume default
                if (info == null)
                    info = DEFAULT;
            }
            effects.put(entity.getClass(), info);
        }
        return info;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void soundPlay(@Nonnull PlaySoundEvent e) {
        final ISound theSound = e.getSound();
        if (theSound != null) {
            final ResourceLocation soundResource = theSound.getSoundLocation();
            if (blockedSounds.contains(soundResource)) {
                e.setResultSound(null);
            }
        }
    }
}
