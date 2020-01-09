### MobEffects-1.14.4-0.0.4.0
**Requirements**
* Forge 1.14.4-28.1.96+
* Dynamic Surroundings: Sound Control 1.14.4-0.0.4.0+

**What's New**
* Option to disable/lessen player particle effects as to minimize display clutter.  By default it is enabled.
* Option to disable footstep accents (armor rustle and puddle splash)
* Added footstep accent for water logged blocks

**Fixes**
* Footprints on snow now linger rather than disappear
* Swim acoustic was not using footstep sound category for volume scaling
* Fix for breath effect "streaming" particles when applied

**Changes**
* Caching of acoustic calculations to minimize hash map lookups

### MobEffects-1.14.4-0.0.3.0
**Requirements**
* Forge 1.14.4-28.1.96+
* Dynamic Surroundings: Sound Control 1.14.4-0.0.3.0+

**Fixes**
* Add configs for solid blocks that are using material that is not solid.

### MobEffects-1.14.4-0.0.2.0
**Requirements**
* Forge 1.14.4-28.1.96+
* Dynamic Surroundings: Sound Control 1.14.4-0.0.2.0+

**What's New**
* Initial release pulling with the following features from Dynamic Surroundings:
  * Sound effects for Footsteps
  * Footstep particle effects
  * Breath particles in cold/underwater regions
  * Tool/Weapon swing sound effects
  * Item toolbar change sound effects
  * Disable critical particle trail from arrows
* Support for the following mods:
  * Minecraft (obviously)
  * Biomes O' Plenty
  * Serene Seasons
* Mods that are not directly supported should still have step sound effects based on:
  * Block data tagging (assuming modded block is tagged)
  * Block material and sound type
* If overall sound of footsteps is too loud/low you can change by modifying "Footstep Volume Scale" in the mod's TOML config file.
