# Redstone Extension Blocks — Design Spec

**Date:** 2026-04-30  
**Status:** Final — all design questions resolved

---

## 1. Scaffolded Variants

### Concept

Scaffolded redstone components are functionally identical to their base counterparts but include a visible scaffolding frame (corner posts + top rail, using vanilla `block/scaffolding_side` / `block/scaffolding_top` textures) that allows blocks to be placed directly above them.

**Solid-top implementation:** Override `getBlockSupportShape()` → `Shapes.block()`. This is the correct MC API surface for block support checks, separate from `getShape()` (visuals) and `getCollisionShape()` (player movement). Those two stay identical to the base block.

**Vanilla class accessibility:** `RepeaterBlock`, `ComparatorBlock`, `RedStoneWireBlock`, and `ComparatorBlockEntity` are all non-final in MC 26.1. ✓

### Crafting (all variants)

```
[base block] + [Scaffolding]  →  [scaffolded variant]    (shaped or shapeless)
[scaffolded variant] alone    →  [base block] + [Scaffolding]   (shapeless, reversible)
```

### In-World Conversion

Players can convert an existing in-place redstone component by right-clicking it while holding Scaffolding. This avoids break-and-replace when wiring is already laid.

**Implementation — NeoForge `PlayerInteractEvent.RightClickBlock`:**

```java
@SubscribeEvent
public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (event.getLevel().isClientSide()) return;
    Player player = event.getEntity();
    ItemStack held = player.getItemInHand(event.getHand());
    if (!(held.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ScaffoldingBlock)) return;

    Level level = event.getLevel();
    BlockPos pos = event.getPos();
    BlockState state = level.getBlockState(pos);
    Block scaffoldedBlock = SCAFFOLD_MAP.get(state.getBlock()); // static Map<Block,Block>
    if (scaffoldedBlock == null) return;

    // Copy all common block state properties
    BlockState newState = scaffoldedBlock.defaultBlockState();
    for (Property<?> prop : newState.getProperties()) {
        if (state.hasProperty(prop)) newState = copyProp(state, newState, prop);
    }

    // Transfer block entity data (for PulseLatch, RedstoneClock, Comparator)
    serializeAndTransferBlockEntity(level, pos, newState);

    level.setBlock(pos, newState, Block.UPDATE_ALL);
    if (!player.isCreative()) held.shrink(1);
    event.setCanceled(true);
    event.setInteractionResult(InteractionResult.sidedSuccess(false));
}
```

`SCAFFOLD_MAP` is a `Map<Block, Block>` mapping each base block to its scaffolded variant. Built as a static field after all blocks are registered.

`serializeAndTransferBlockEntity`: capture old BE's save data (via `saveWithoutMetadata` / `load` or ValueOutput/ValueInput round-trip depending on what MC 26.1 exposes), set the new block, then apply captured data to the freshly created BE. Only needed for PulseLatch, SluggishPulseLatch, RedstoneClock, and Comparator.

**Event handler location:** New class `core/ScaffoldingConversionEvents.java`, annotated `@EventBusSubscriber(modid = ..., bus = Bus.GAME)`.

---

### 1.1 Scaffolded Repeater

| Field | Value |
|---|---|
| Extends | `net.minecraft.world.level.block.RepeaterBlock` |
| Block Entity | None |
| Block States | `FACING`, `POWERED`, `DELAY` (1–4), `LOCKED` — identical to vanilla |
| Behavior | Identical to vanilla repeater |

Overrides: `getBlockSupportShape()` only.

---

### 1.2 Scaffolded Comparator

| Field | Value |
|---|---|
| Extends | `net.minecraft.world.level.block.ComparatorBlock` |
| Block Entity | `ScaffoldedComparatorBlockEntity extends ComparatorBlockEntity` |
| Block States | `FACING`, `POWERED`, `MODE` (COMPARE/SUBTRACT) — identical to vanilla |
| Behavior | Identical to vanilla comparator |

`ScaffoldedComparatorBlockEntity` has no extra fields — it exists solely so we can register a `BlockEntityType` for our block without interfering with vanilla's `Blocks.COMPARATOR` registration.

---

### 1.3 Scaffolded Pulse Latch

| Field | Value |
|---|---|
| Extends | `PulseLatch` |
| Block Entity | `PulseLatchBlockEntity` — add `SCAFFOLDED_PULSE_LATCH` to existing type |
| Block States | `FACING`, `POWERED`, `DELAY` (1–4) |
| Behavior | Identical to `PulseLatch` |

Update registration:
```java
new BlockEntityType<>(PulseLatchBlockEntity::new,
    HoopyFroodTutBlocks.PULSE_LATCH.get(),
    HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get())
```

---

### 1.4 Scaffolded Sluggish Pulse Latch

| Field | Value |
|---|---|
| Extends | `SluggishPulseLatch` |
| Block Entity | `SluggishPulseLatchBlockEntity` — add `SCAFFOLDED_SLUGGISH_PULSE_LATCH` to existing type |
| Block States | `FACING`, `POWERED`, `DELAY` (1–4) |
| Behavior | Identical to `SluggishPulseLatch` |

---

### 1.5 Scaffolded Redstone Clock

| Field | Value |
|---|---|
| Extends | `RedstoneClock` |
| Block Entity | `RedstoneClockBlockEntity` — add scaffolded block to existing type |
| Block States | `FACING`, `POWERED`, `DELAY` (1–4) |
| Behavior | Identical to `RedstoneClock` |

---

### 1.6 Scaffolded Redstone Dust

| Field | Value |
|---|---|
| Extends | `net.minecraft.world.level.block.RedStoneWireBlock` |
| Block Entity | None |
| Block States | `NORTH`, `SOUTH`, `EAST`, `WEST` (`RedstoneSide`: NONE/SIDE/UP), `POWER` (0–15) — identical to vanilla |
| Behavior | Identical to vanilla redstone wire |

Overrides: `getBlockSupportShape()` only.

**Model approach:** Use a `multipart` blockstate JSON (not `variants`). This avoids enumerating all 81 wire connection combinations:

```json
{
  "multipart": [
    { "apply": { "model": "hoopyfroodtut:block/scaffolded_dust_frame" } },
    { "when": { "north": "side" }, "apply": { "model": "minecraft:block/redstone_dust_side0", ... } },
    { "when": { "north": "up"  }, "apply": { "model": "minecraft:block/redstone_dust_up",   ... } },
    ...
  ]
}
```

`scaffolded_dust_frame.json` — 4 corner posts + top horizontal rails, scaffolding textures, no tint.

---

## 2. Angled Repeater

### Concept

An angled repeater reads its input from one lateral side and outputs signal in the perpendicular direction. Two chiralities exist as separate block types. Crafting a single angled repeater alone converts to the opposite chirality. Delay resets on conversion (standard recipe behavior).

### 2.1 Block State Properties

| Property | Type | Values | Notes |
|---|---|---|---|
| `FACING` | `DirectionProperty` | N/S/E/W | Direction of signal **output** (DiodeBlock convention) |
| `POWERED` | `BooleanProperty` | true/false | Currently outputting |
| `DELAY` | `IntegerProperty` | 1–4 | 4, 8, 12, 16 game ticks |
| `LOCKED` | `BooleanProperty` | true/false | Locked by adjacent comparator/repeater |

### 2.2 Chirality Convention

Input direction relative to output `FACING`, viewed from above:

| FACING (output) | RIGHT input from | LEFT input from |
|---|---|---|
| NORTH | EAST | WEST |
| EAST | SOUTH | NORTH |
| SOUTH | WEST | EAST |
| WEST | NORTH | SOUTH |

**Right** = input clockwise from output. **Left** = input counterclockwise from output.

### 2.3 Class Hierarchy

```
DiodeBlock
  └── AngledRepeater (abstract — shared logic)
        ├── LeftAngledRepeater
        └── RightAngledRepeater

AngledRepeater
  └── ScaffoldedAngledRepeater (abstract)
        ├── ScaffoldedLeftAngledRepeater
        └── ScaffoldedRightAngledRepeater
```

No block entity needed for any variant.

### 2.4 Key Overrides on AngledRepeater

```java
/** Chirality-specific: returns the direction from which this block reads its input. */
public abstract Direction getInputDirection(BlockState state);
// RightAngledRepeater: state.getValue(FACING).getClockWise()
// LeftAngledRepeater:  state.getValue(FACING).getCounterClockWise()
```

**`getInputSignal`** — copy DiodeBlock's implementation but substitute `getInputDirection(state)` wherever the vanilla code uses `state.getValue(FACING)` as the read direction:

```java
@Override
protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
    Direction inputDir = getInputDirection(state);
    BlockPos targetPos = pos.relative(inputDir);
    int input = level.getSignal(targetPos, inputDir);
    if (input >= 15) return input;
    // preserve DiodeBlock's diode-chaining logic verbatim, using inputDir
    BlockState inputState = level.getBlockState(targetPos);
    ...
    return input;
}
```

**`getSignal` / `getDirectSignal`** — inherited from `DiodeBlock` unchanged; emits in `FACING` direction. ✓

**`canConnectRedstone`** — allow only `FACING` (output) and `getInputDirection(state)` (input); block the other two faces:

```java
@Override
public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
    return dir == state.getValue(FACING) || dir == getInputDirection(state);
}
```

**`isFacingAnInputSide`** — used by redstone wire rendering to determine which face to connect to:

```java
@Override
public boolean isFacingAnInputSide(BlockState state, Direction dir) {
    return dir == getInputDirection(state);
}
```

### 2.5 Locking

DiodeBlock's `getAlternateSignal` iterates the two lateral faces for lock detection. For an angled repeater, the two "unused" faces (neither input nor output) become the lock sources. This is automatically correct — no override needed. Verify in-game.

### 2.6 Placement

`AngledRepeater` inherits `getStateForPlacement()` from `DiodeBlock`: `FACING` points away from the placer (output toward open space). Chirality is determined by which item (left or right variant) the player holds. No auto-detection logic.

### 2.7 Recipes

**Initial craft:**
```
Shaped:  . . .
         R D .
         . . .
  R = Repeater, D = Redstone Dust  →  Left Angled Repeater
```
(Left is the default; players flip to right via the flip recipe.)

**Flip recipes (shapeless, 1 ingredient):**
```
[left_angled_repeater]  → [right_angled_repeater]
[right_angled_repeater] → [left_angled_repeater]
```

Delay resets to 1 on flip.

### 2.8 Scaffolded Angled Repeater

`ScaffoldedAngledRepeater` adds only `getBlockSupportShape()` → `Shapes.block()` on top of `AngledRepeater`.

**Craft:**
```
[left_angled_repeater]  + [Scaffolding]  →  [scaffolded_left_angled_repeater]
[right_angled_repeater] + [Scaffolding]  →  [scaffolded_right_angled_repeater]
```

Flip recipe also works on scaffolded variants:
```
[scaffolded_left_angled_repeater]  → [scaffolded_right_angled_repeater]
[scaffolded_right_angled_repeater] → [scaffolded_left_angled_repeater]
```

In-world conversion: right-click angled repeater with Scaffolding to convert in-place (SCAFFOLD_MAP covers all 4 angled variants).

---

## 3. Block Models

All new blocks are excluded from `getKnownBlocks()` in `BlockModelProvider` and use hand-authored JSONs.

### Scaffolded component visual

The scaffolding frame consists of:
- 4 vertical corner posts (2×2 pixels wide, full block height)
- A top horizontal rail connecting the posts at Y=16
- Textures: `minecraft:block/scaffolding_side` (posts), `minecraft:block/scaffolding_top` (rail)
- The base component model is referenced as a parent or composed in the same JSON

Each scaffolded component has a single base model JSON (frame + base component combined). The blockstate JSON applies rotations for FACING (reusing `BlockModelGenerators.ROTATION_FACING`).

### Angled Repeater visual

Based on vanilla repeater geometry:
- Flat stone slab base
- Two redstone torches: one on the input side (unpowered = off, powered = on), one on the output side (always on when powered)
- The 90° angle is expressed by torch positioning, not by rotating the whole model

Model variants: POWERED (off/on) × DELAY (1–4) = 8 model JSONs per chirality.
Blockstate JSON: dispatch on `FACING` rotation × `POWERED` × `DELAY`.

---

## 4. Full Registration Checklist

### New block classes

```
block/
  ScaffoldedRepeater.java
  ScaffoldedComparator.java
  ScaffoldedPulseLatch.java
  ScaffoldedSluggishPulseLatch.java
  ScaffoldedRedstoneClock.java
  ScaffoldedRedstoneDust.java
  AngledRepeater.java                   (abstract)
  LeftAngledRepeater.java
  RightAngledRepeater.java
  ScaffoldedAngledRepeater.java         (abstract)
  ScaffoldedLeftAngledRepeater.java
  ScaffoldedRightAngledRepeater.java

blockentity/
  ScaffoldedComparatorBlockEntity.java

core/
  ScaffoldingConversionEvents.java
```

### HoopyFroodTutBlocks additions

```java
SCAFFOLDED_REPEATER
SCAFFOLDED_COMPARATOR
SCAFFOLDED_PULSE_LATCH
SCAFFOLDED_SLUGGISH_PULSE_LATCH
SCAFFOLDED_REDSTONE_CLOCK
SCAFFOLDED_REDSTONE_DUST
LEFT_ANGLED_REPEATER
RIGHT_ANGLED_REPEATER
SCAFFOLDED_LEFT_ANGLED_REPEATER
SCAFFOLDED_RIGHT_ANGLED_REPEATER
```

All with `.instabreak().sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)`.

### HoopyFroodBlockEntityTypes changes

| Change | Detail |
|---|---|
| Update `PULSE_LATCH` type | add `SCAFFOLDED_PULSE_LATCH` to valid blocks |
| Update `SLUGGISH_PULSE_LATCH` type | add `SCAFFOLDED_SLUGGISH_PULSE_LATCH` |
| Update `REDSTONE_CLOCK` type | add `SCAFFOLDED_REDSTONE_CLOCK` |
| New `SCAFFOLDED_COMPARATOR` type | → `ScaffoldedComparatorBlockEntity` |

### HoopyFroodItems additions

`registerSimpleBlockItem` for all 10 new blocks.

### HoopyFroodCreativeTabs

Add all 10 new block items.

### DataGen changes

| Provider | Change |
|---|---|
| `BlockModelProvider` | Exclude all 10 new blocks from `getKnownBlocks()`; hand-author blockstate + model JSONs |
| `RecipeProvider` | +10 craft recipes, +10 reverse recipes, +2 angled flip, +2 scaffolded angled flip, +1 initial angled = 25 new recipes |
| `BlockLootTableProvider` | `dropSelf()` × 10 |

---

## 5. Implementation Order

1. Core block classes (§1.1–1.6, §2) — no models yet
2. Registration (blocks, items, BE types, creative tab)
3. `ScaffoldingConversionEvents.java`
4. `BlockLootTableProvider` additions
5. `RecipeProvider` additions
6. Block model and blockstate JSONs — hand-authored last since they need the game to load to iterate
7. `runData` to verify datagen; `runClient` to check visuals
