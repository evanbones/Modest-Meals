### Added

- Added ParCool integration! (1.21 Neo)
    - Parkour moves take stamina in place of ParCool's own stamina bar, which is hidden.
    - An exhausted player can't start a move that costs stamina, and held moves such as hanging or wall running end
      when the bar runs out.
    - A new ParCool config tab sets the cost of each move: single moves (vault, wall jump, dodge...) take a set amount,
      held moves (hang on, horizontal wall run, slide down...) take stamina every second.
    - ParCool's Inexhaustible effect (Energy Drink) makes parkour free.
- Added a Stamina Costs config section for every stamina cost.
    - New options for how fast sprinting, sprint-swimming, elytra flight and Reliable Gliders gliding drain stamina.
        - 1.0 keeps the old rate, 0.5 drains half as fast.
    - The blocking, parry, combat roll, and hand crank options moved here. Existing configs are unchanged

### Fixed

- Fixed inconsistent vat EMI GUI.