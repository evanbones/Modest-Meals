### Added

- Effects that foods grant on their own (e.g. golden apple's Regeneration, raw chicken's Hunger) now show up in the food
  editor and can be edited or removed.
- Effect traits now support a chance to apply, shown in tooltips (e.g. "Hunger (00:30) (30% chance)").
- The Health trait's tooltip preview in the food editor now shows its regeneration speed.

### Changed

- Gameplay config options (hunger, health regeneration, stamina, and food effect settings) are now controlled by the
  server. 
  - On a server, they show the server's values and are greyed out in the config screen.
- Dedicated servers now reload the config file on `/reload` and send the new values to everyone online.