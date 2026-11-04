# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-11-04

### Added

- Added pies and cakes, which work like Farmer's Delight's.
- Added milkshakes, coloured by their flavour.
    - Neapolitan flavours make Neapolitan's own milkshakes.
- Added three-scoop ice cream bowls, made in the Cultural Delights vat.
- Soup, curry, pasta, ramen, salad, jam, and sushi rolls now show their ingredients like sandwiches and wraps
  do.
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

### Fixed

- Fixed temporary health from meals not being granted.
- Fixed bowl meals not giving the bowl back when eaten.

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