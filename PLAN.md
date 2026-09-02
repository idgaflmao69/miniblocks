# Miniblocks: Future Ideas

The core of the mod is working: blocks can become small decorative blocks, be placed together, and be converted with the Miniblock Bench.

Future work should make the mod feel more complete without making it harder to use.

## Make every miniblock a faithful miniature

The biggest improvement would be preserving more than a block's basic identity.

Many Minecraft blocks remember details such as direction, rotation, growth stage, whether they are powered, or whether they contain water. A miniblock should keep those details when it is picked up, placed, stored, or loaded from a saved world.

This would make the system feel consistent for players and reduce surprising differences between a normal block and its miniature version.

## Be clear about what can be miniature

Most ordinary blocks should work naturally, but some blocks have extra data or special behavior that cannot safely fit into the current system.

The mod should have a clear policy for these cases. Unsupported blocks should be refused with a clear explanation rather than being accepted and losing information. This is especially important for blocks with inventories, block entities, fluids, entities, or unusually complex models.

## Keep existing features reliable

The mod has already needed fixes for placement, collision, saving, rendering, particles, drops, and client synchronization.

Before adding larger features, it would be useful to create a small set of repeatable checks. These should cover the most important player actions:

- Place miniblocks next to and around the player.
- Fill and empty all eight spaces.
- Convert blocks in both directions at the bench.
- Break miniblocks and verify their drops.
- Save, reload, and revisit a world.
- Test different block shapes, textures, and orientations.

## Make large displays perform well

Dynamic rendering is useful because it avoids creating a separate model for every block, but large decorative builds may eventually become expensive.

The mod should be tested with many miniblocks in one area. If performance becomes an issue, repeated calculations and rendering information can be reduced or reused without changing how the blocks look.

## Improve the bench experience

The Miniblock Bench is the main way players turn blocks into miniblocks, so it should make its rules obvious.

Future polish could include clearer invalid-input feedback, better tooltips, useful sounds, smoother inventory movement, and more helpful recipe-book behavior. The goal is for players to understand the bench without needing outside instructions.

## Consider advanced movement later

Piston support would be an interesting addition, but moving a miniblock means moving both the visible block and all eight stored mini-blocks. It must also stay synchronized between the server and every client.

This should be treated as a separate advanced project and only attempted after testing single pushes, chains of pistons, chunk borders, saving, and multiplayer behavior. Until then, immovable miniblocks are safer than unreliable movement.

## Explain the mod to players and contributors

The README and GitHub releases should eventually describe:

- Installation and basic controls.
- How the Miniblock Bench works.
- How conversion and placement work.
- Which blocks have limitations.
- Known unavailable features.
- What changed in each release.

Clear documentation will make the project easier to test, share, and continue.

## Keep upgrades manageable

Minecraft and Fabric APIs change frequently. Future upgrades should be handled as deliberate maintenance tasks rather than rushed replacements.

For each upgrade, review dependencies, registrations, rendering, block entities, screen handlers, data files, loot tables, and the most important gameplay checks.

## Suggested direction

The most useful order would be:

1. Preserve complete block information.
2. Decide how special blocks should behave.
3. Add repeatable gameplay checks.
4. Improve performance and bench usability.
5. Expand documentation.
6. Revisit piston support only when it can be tested thoroughly.
7. Upgrade Minecraft and Fabric versions when the current behavior is well understood.
