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

import it.unimi.dsi.fastutil.objects.Object2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
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

    // For things that don't make an equip sound - like AIR
    public static final ItemData EMPTY = new ItemData("EMPTY", Constants.NONE);
    public static final ItemData NONE = new ItemData("NONE", Constants.NONE, Constants.NONE, Constants.UTILITY_EQUIP);
    public static final ItemData LEATHER = new ItemData.ArmorItemData("LEATHER", Constants.LEATHER_ARMOR_EQUIP, Constants.LIGHT_ARMOR, Constants.LIGHT_FOOT_ARMOR, 0);
    public static final ItemData CHAIN = new ItemData.ArmorItemData("CHAIN", Constants.CHAIN_ARMOR_EQUIP, Constants.MEDIUM_ARMOR, Constants.MEDIUM_FOOT_ARMOR, 1);
    public static final ItemData CRYSTAL = new ItemData.ArmorItemData("CRYSTAL", Constants.CRYSTAL_ARMOR_EQUIP, Constants.CRYSTAL_ARMOR, Constants.CRYSTAL_FOOT_ARMOR, 2);
    public static final ItemData PLATE = new ItemData.ArmorItemData("PLATE", Constants.PLATE_ARMOR_EQUIP, Constants.HEAVY_ARMOR, Constants.HEAVY_FOOT_ARMOR, 3);
    public static final ItemData SHIELD = new ItemData("SHIELD", Constants.TOOL_SWING, Constants.SHIELD_USE, Constants.SHIELD_EQUIP);
    public static final ItemData SWORD = new ItemData("SWORD", Constants.SWORD_SWING, Constants.NONE, Constants.SWORD_EQUIP);
    public static final ItemData AXE = new ItemData("AXE", Constants.AXE_SWING, Constants.NONE, Constants.AXE_EQUIP);
    public static final ItemData BOW = new ItemData("BOW", Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP);
    public static final ItemData CROSSBOW = new ItemData("CROSSBOW", Constants.TOOL_SWING, Constants.BOW_PULL, Constants.BOW_EQUIP);
    public static final ItemData TOOL = new ItemData("TOOL", Constants.TOOL_SWING, Constants.NONE, Constants.TOOL_EQUIP);
    public static final ItemData BOOK = new ItemData("BOOK", Constants.BOOK_EQUIP, Constants.BOOK_EQUIP, Constants.BOOK_EQUIP);
    public static final ItemData POTION = new ItemData("POTION", Constants.POTION_EQUIP, Constants.POTION_EQUIP, Constants.POTION_EQUIP);

    private static final IModLog LOGGER = MobEffects.LOGGER.createChild(ItemLibrary.class);
    private static final Object2ReferenceAVLTreeMap<String, ItemData> CACHE = new Object2ReferenceAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);
    // Pattern for matching Java class names
    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

    // https://www.regexplanet.com/advanced/java/index.html
    private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");
    // Pattern for matching ItemStack names
    private static final Pattern ITEM_PATTERN = Pattern.compile("([\\w\\-]+:[\\w\\.\\-/]+)[:]?(\\d+|\\*)?(\\{.*\\})?");
    private static final int SET_CAPACITY = 64;
    private static final int MAP_CAPACITY = 256;
    private static final Map<ItemData, Set<Class<?>>> classMap = new Reference2ReferenceOpenHashMap<>();
    private static final Map<Item, ItemData> items = new IdentityHashMap<>(MAP_CAPACITY);

    static {
        CACHE.put(EMPTY.getName(), EMPTY);
        CACHE.put(NONE.getName(), NONE);
        CACHE.put(LEATHER.getName(), LEATHER);
        CACHE.put(CHAIN.getName(), CHAIN);
        CACHE.put(CRYSTAL.getName(), CRYSTAL);
        CACHE.put(PLATE.getName(), PLATE);
        CACHE.put(SHIELD.getName(), SHIELD);
        CACHE.put(SWORD.getName(), SWORD);
        CACHE.put(AXE.getName(), AXE);
        CACHE.put(BOW.getName(), BOW);
        CACHE.put(CROSSBOW.getName(), CROSSBOW);
        CACHE.put(TOOL.getName(), TOOL);
        CACHE.put(BOOK.getName(), BOOK);
        CACHE.put(POTION.getName(), POTION);
    }

    private ItemLibrary() {
    }

    static void initialize() {
        classMap.clear();
        items.clear();

        for (final ItemData ic : CACHE.values())
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
                final ItemData ic = resolveClass(item);
                items.put(item, ic);
            }
        }
    }

    private static ItemData resolveClass(@Nonnull final Item item) {
        for (final ItemData ic : CACHE.values()) {
            final Set<Class<?>> itemSet = classMap.get(ic);
            if (doesBelong(itemSet, item))
                return ic;
        }
        return NONE;
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

        final ItemData ic = CACHE.get(itemClass);
        final Set<Class<?>> theList = classMap.get(ic);

        for (final String c : itemList) {
            // If its not a like match it has to be a concrete item
            Matcher match = ITEM_PATTERN.matcher(c);
            if (match.matches()) {
                final String itemName = match.group(1);
                if (ResourceLocation.isResouceNameValid(itemName)) {
                    final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
                    if (item != null) {
                        items.put(item, ic);
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
    public static ItemData getItemData(@Nonnull final ItemStack stack) {
        return stack.isEmpty() ? NONE : items.get(stack.getItem());
    }
}
