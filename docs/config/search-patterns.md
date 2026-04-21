---
title: Search Patterns
nav_order: 8
parent: Config Reference
---

# Search Patterns

The `search-pattern` key in `rtp.yml` controls how EzRTP generates candidate coordinates
on each teleport attempt. Different patterns produce different spatial distributions,
which affects both fairness and performance depending on your world shape and biome layout.

```yml
# rtp.yml
search-pattern: random
```

Valid values: `random`, `circle`, `square`, `diamond`, `triangle`

---

## random (default)

**Config key:** `random`

Picks a random integer radius within `[min-radius, max-radius]` and a random angle, then
places the candidate on the perimeter of that circle. Because the radius is sampled as a
whole-block integer, candidates cluster slightly more at smaller radii but the distribution
is otherwise uniform.

**Best for:** general use; matches the classic RTP feel on flat or varied terrain.

```yml
search-pattern: random
```

---

## circle

**Config key:** `circle`

Identical in shape to `random` but uses a **floating-point** radius sampled continuously
from `[min-radius, max-radius]`. This eliminates the slight integer-radius clustering and
produces a more even ring distribution.

**Best for:** large radius ranges where you want an even spread across the full annulus.

```yml
search-pattern: circle
```

---

## square

**Config key:** `square`

Candidates are placed uniformly on the **perimeter of an axis-aligned square** whose
half-side equals the sampled radius. Each of the four edges has equal probability.

The result is a square band of candidate points tilted to align with the world axes — the
four cardinal directions (N/S/E/W) see no concentration bias.

**Best for:** square or grid-shaped worlds, or servers with rectangular claimed areas to
work around.

```yml
search-pattern: square
```

---

## diamond

**Config key:** `diamond`

Places candidates on the perimeter of an **8-vertex chamfered diamond** (an octagonal
lozenge) aligned so the four primary vertices point north, east, south, and west. The shape
approximates the classic Minecraft "diamond" silhouette that appears on maps, with small
beveled corners.

Each edge of the octagon has equal probability, giving a uniform perimeter distribution
with a 45-degree rotated footprint compared to `square`.

**Best for:** when a rotated square feel is preferred, or when you want to bias exploration
toward the intercardinal (NE/SE/SW/NW) diagonals.

```yml
search-pattern: diamond
```

---

## triangle

**Config key:** `triangle`

Generates a candidate on the **perimeter of an equilateral triangle** centered on the RTP
center. One vertex of the triangle is always placed due north; the other two are rotated
120° and 240° from it. A random edge is selected, then a uniformly random interpolation
along that edge determines the final point.

The triangle pattern concentrates travel in three preferred directions and leaves the
space between the vertices relatively undersampled — this can be useful to force players
into distinct "wings" of the world.

**Best for:** experimental use or unusual world shapes where three-directional bias is
acceptable.

```yml
search-pattern: triangle
```

---

## Comparison summary

| Pattern | Shape | Coverage type | Radius sampling |
|:--------|:------|:-------------|:----------------|
| `random` | Circle perimeter | Perimeter only | Integer |
| `circle` | Circle perimeter | Perimeter only | Floating-point |
| `square` | Square perimeter | Perimeter only | Integer |
| `diamond` | Octagonal lozenge perimeter | Perimeter only | Integer |
| `triangle` | Equilateral triangle perimeter | Perimeter only | Integer |

All patterns respect `min-radius` and `max-radius`. Candidates are generated fresh on each
attempt up to `max-attempts` before the teleport fails.
