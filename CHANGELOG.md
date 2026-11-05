# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-11-04

### Added

- Added an Elytra Uses Stamina option, off by default.

### Changed

- Whole pies, cakes, pizzas, and Farmer's Delight feasts now show what each slice or serving gives, instead of the
  whole thing.
- Whole rolls now take longer to eat.
- Slices are quicker to eat and milkshakes take as long to eat as Neapolitan's.
- Meals and slices can now have their eating time changed in the food editor.
- Sandwiches and wraps take longer to eat.
- Cut meat and fish from other mods are now worth the same share of the whole as Farmer's Delight's fish slices.
- Horse Steak, Cooked Billhook Bass, Pike, Smoked Ham, Ham, and Perch are worth more.
- Nerfed Cooked Venison, Cooked Hoglin Loin, and Cooked Mint Chops a little.
- Farmer's Delight tomatoes now give Strength in meals, and tomatoes are now as strong as beetroot and onions.
- Lemons, walnuts and cheese wedges from different mods are now worth the same.

### Fixed

- Fixed sprinting on a horse using stamina.

## [2.0.0] - 2026-11-04

### Added

- Added pies and cakes, which work like Farmer's Delight's.
- Added milkshakes, coloured by their flavour.
    - Neapolitan flavours make Neapolitan's own milkshakes.
- Added three-scoop ice cream bowls, made in the Cultural Delights vat.
- Soup, curry, pasta, ramen, salad, jam, and sushi rolls now show their ingredients like sandwiches and wraps do.
- Added meal art for ingredients from Tide, Upgrade Aquatic, Caverns & Chasms, Quark, Onion Onion, Incubation,
  Buzzier Bees, Berry Good, and Abundant Atmosphere.
- Quark Crab legs can now go in rolls.
- Added an Exhaustion Lockout option.
    - Turning it off makes it so running out of stamina only pauses you for the recharge cooldown.

### Changed

- Meals are now named after their main ingredient instead of their effect.
- Eating more slices of the same pie, cake, pizza or roll now adds to the effect's time.
- Ice cream is now called Ice Cream Cone (including Cultural Delights' one) and only has one flavour.
- Rebalanced meals so sandwiches are no longer the best choice for everything.
- Combat effects (Strength, Resistance) get half as much time from filler ingredients, and utility effects (Fire
  Resistance, Water Breathing, Night Vision, etc.) get twice as much.
- Cut meats and fish slices from Farmer's Delight and other mods no longer count as whole cuts in meals.
- Endurance's timer is paused while Nourishment is active.
- Meal recipes now show up in EMI's Crafting, Cooking and Aging categories, rather than in separate Meal categories.
- Sprinting Restriction now also applies when Sprinting Uses Stamina is disabled.
- Fish, fruit and modded raw meats now digest at the same rates as other food.
- Small raw foods like calamari no longer take ages to digest, and neither do a few modded desserts, salads, and soups.
- Tagged more ingredients from Abundant Atmosphere, Berry Good, Bountiful Fares, Create, Infernal
  Expansion, Nirvana, No Man's Land, Onion Onion, and Redomesticate.
- More modded sweets, desserts, and jam exclusions are recognised.

### Fixed

- Fixed temporary health from meals not being granted.
- Fixed bowl meals not giving the bowl back when eaten.

### Removed

- Removed Porridge.

## [1.2.1] - 2026-11-03

### Added

- Added a Sprinting Uses Stamina option.

## [1.2.0] - 2026-11-03

### Changed

- Renamed the Stamina Sprinting option to Enable Stamina, since turning it off disables the whole stamina system.

### Fixed

- Fixed layered meals z-fighting in item frames.
- Items removed with Reliable Remover no longer show up in the food profile editor.

## [1.1.0] - 2026-11-02

### Added

- Added Combat Roll support.
    - Each roll takes stamina, and an exhausted player can't roll.
- Added a Hand Crank Stamina Cost option for Create's hand crank and anything else in the `modest_meals:stamina_cranks`
  tag.

### Fixed

- Fixed cooking pot meals only cooking when their bowl or bottle was already in the container slot.

## [1.0.0] - 2026-11-02

- Initial release.