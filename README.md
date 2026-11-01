# Better Patrols

When the player's transponder is off and their fleet is at least 1.5x stronger (by combat strength) than a friendly patrol, that patrol now avoids the player using the game's own `MEMORY_KEY_AVOID_PLAYER_SLOWLY` AI behavior. This is the same avoidance weaker hostile fleets use, so patrols keep their distance instead of investigating and damaging relations.

Tuning: edit `STRENGTH_RATIO` in `data/scripts/BetterPatrolsScript.java` (default `1.5f`).

Only patrol fleets (`$isPatrol`) of non-hostile, non-player factions are affected. The flag is removed as soon as the player turns the transponder back on or is no longer sufficiently stronger.

Patrols that already know the player's identity (they saw the transponder on recently, the fleet has unique-signature ships, etc. — the vanilla `knowsWhoPlayerIs()` check) are never made to avoid the player. Avoiding is only meaningful while the player is unidentified, which matches vanilla's own behavior.
