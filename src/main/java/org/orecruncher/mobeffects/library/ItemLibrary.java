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

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.orecruncher.lib.logging.IModLog;
import org.orecruncher.mobeffects.MobEffects;
import org.orecruncher.mobeffects.library.config.ModConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public final class ItemLibrary {

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(ItemLibrary.class);

    // https://www.regexplanet.com/advanced/java/index.html

    // Pattern for matching Java class names
    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");

    // Pattern for matching ItemStack names
    private static final Pattern ITEM_PATTERN = Pattern.compile("([\\w\\-]+:[\\w\\.\\-/]+)[:]?(\\d+|\\*)?(\\{.*\\})?");

    private static final int SET_CAPACITY = 64;
    private static final int MAP_CAPACITY = 256;

    private static SimpleItemData NONE_DATA;

    private static final EnumMap<ItemClass, Set<Class<?>>> classMap = new EnumMap<>(ItemClass.class);

    private static final Map<Item, IItemData> items = new IdentityHashMap<>(MAP_CAPACITY);

    private ItemLibrary() {
    }

    static void initialize() {
        classMap.clear();
        items.clear();
        NONE_DATA = SimpleItemData.CACHE.get(ItemClass.NONE);

        for (final ItemClass ic : ItemClass.values())
            classMap.put(ic, new ReferenceOpenHashSet<>(SET_CAPACITY));
    }

    static void initFromConfig(@Nonnull final ModConfig mod) {
        for (final Map.Entry<String, List<String>> entry : mod.items.entrySet()) {
            process(entry.getValue(), entry.getKey());
        }

    }

    static void complete() {
        // Iterate through the list of registered Items to see if we know about them, or can infer based on class
        // matching.
        for (final Item item : ForgeRegistries.ITEMS) {
            if (!items.containsKey(item)) {
                final ItemClass ic = resolveClass(item);
                items.put(item, SimpleItemData.CACHE.get(ic));
            }
        }
    }

    private static ItemClass resolveClass(@Nonnull final Item item) {
        for (final ItemClass ic : ItemClass.values()) {
            final Set<Class<?>> itemSet = classMap.get(ic);
            if (doesBelong(itemSet, item))
                return ic;
        }
        return ItemClass.NONE;
    }

    private static boolean doesBelong(@Nonnull final Set<Class<?>> itemSet, @Nonnull final Item item) {

        final Class<?> itemClass = item.getClass();

        // If the item is in the collection already, return
        if (itemSet.contains(itemClass))
            return true;

        // Need to iterate to see if an item is a sub-class of an existing
        // item in the list.
        final Optional<Class<?>> result = itemSet.stream().filter(c -> c.isAssignableFrom(itemClass)).findFirst();
        if (result.isPresent()) {
            itemSet.add(itemClass);
            return true;
        }
        return false;
    }

    private static void process(@Nullable final List<String> itemList, @Nonnull final String itemClass) {
        if (itemList == null || itemList.isEmpty())
            return;

        final ItemClass ic = ItemClass.valueOf(itemClass);
        final Set<Class<?>> theList = classMap.get(ic);

        for (final String c : itemList) {
            // If its not a like match it has to be a concrete item
            Matcher match = ITEM_PATTERN.matcher(c);
            if (match.matches()) {
                final String itemName = match.group(1);
                if (ResourceLocation.isResouceNameValid(itemName)) {
                    final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
                    if (item != null) {
                        items.put(item, SimpleItemData.CACHE.get(ic));
                    } else {
                        LOGGER.warn("Cannot locate item [%s] for ItemRegistry", c);
                    }
                } else {
                    LOGGER.warn("Item specification [%s] is not valid", itemName);
                }
            } else {
                match = FQCN.matcher(c);
                if (match.matches()) {
                    try {
                        // If we don't have an Item assume its a class name. If it is an item
                        // we want that class.
                        final Class<?> clazz = Class.forName(c, false, ItemLibrary.class.getClassLoader());
                        theList.add(clazz);
                    } catch (@Nonnull final ClassNotFoundException e) {
                        LOGGER.warn("Cannot locate class '%s' for ItemRegistry", c);
                    }
                } else {
                    LOGGER.warn("Unrecognized pattern '%s' for ItemRegistry", c);
                }
            }
        }
    }

    @Nonnull
    public static IItemData getItemData(@Nonnull final ItemStack stack) {
        return stack.isEmpty() ? NONE_DATA : items.get(stack.getItem());
    }
}
