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

package org.orecruncher.mobeffects;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.orecruncher.lib.fml.ConfigUtils;
import org.orecruncher.lib.fml.UpdateChecker;
import org.orecruncher.lib.logging.ModLog;
import org.orecruncher.mobeffects.effects.EntityBreathEffect;
import org.orecruncher.mobeffects.effects.EntityFootprintEffect;
import org.orecruncher.mobeffects.library.Constants;
import org.orecruncher.mobeffects.library.Libraries;
import org.orecruncher.sndctrl.IMC;

import javax.annotation.Nonnull;
import java.nio.file.Path;

@Mod(MobEffects.MOD_ID)
public final class MobEffects {

    /**
     * ID of the mod
     */
    public static final String MOD_ID = "mobeffects";
    /**
     * Logging instance for trace
     */
    public static final ModLog LOGGER = new ModLog(MobEffects.class);
    /**
     * Path to the mod's configuration directory
     */
    @Nonnull
    public static final Path CONFIG_PATH = ConfigUtils.getConfigPath(MOD_ID);

    public MobEffects() {

        // Various event bus registrations
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupComplete);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        MinecraftForge.EVENT_BUS.register(this);

        // Initialize our configuration
        Config.setup();
    }

    private void commonSetup(@Nonnull final FMLCommonSetupEvent event) {

    }

    private void clientSetup(@Nonnull final FMLClientSetupEvent event) {
        KeyBoard.initialize();
    }

    private void setupComplete(@Nonnull final FMLLoadCompleteEvent event) {

    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Sound Category
        IMC.registerSoundCategory(Constants.FOOTSTEPS);

        // Register our AcousticEvents
        IMC.registerAcousticEvent(
                Constants.WALK,
                Constants.WANDER,
                Constants.SWIM,
                Constants.RUN,
                Constants.JUMP,
                Constants.LAND,
                Constants.CLIMB,
                Constants.CLIMB_RUN,
                Constants.DOWN,
                Constants.DOWN_RUN,
                Constants.UP,
                Constants.UP_RUN
        );

        // Register our sounds through SoundControl because Forge likes stomping client side sounds from the registry
        IMC.registerSoundFile(new ResourceLocation(MOD_ID, "sounds.json"));

        // Register our acoustics.  Do this after the sound file because of dependencies.
        IMC.registerAcousticFile(new ResourceLocation(MOD_ID, "acoustics.json"));

        // Register our effect handlers
        IMC.registerEffectFactoryHandler(EntityFootprintEffect.DEFAULT_HANDLER);
        IMC.registerEffectFactoryHandler(EntityBreathEffect.DEFAULT_HANDLER);

        // Callback for completions
        IMC.registerCompletionCallback(Libraries::initialize);
        IMC.registerCompletionCallback(Libraries::complete);
    }

    @SubscribeEvent
    public void onPlayerLogin(@Nonnull final PlayerLoggedInEvent event) {
        LOGGER.debug("Player login: %s", event.getPlayer().getDisplayName().getFormattedText());
        if (Config.CLIENT.logging.onlineVersionCheck.get())
            UpdateChecker.doCheck(event, MOD_ID);
    }
}
