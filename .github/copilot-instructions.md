# Copilot Instructions for Procedural Assets Elective

## Architecture Overview
This Java application generates procedural spurs (radial spikes) on outlines extracted from binary images. The pipeline processes images through morphological cleanup and geometric spur addition.

**Data Flow:**
1. Load input image → convert to binary mask (black pixels < 128)
2. Clean outline using morphological thinning with precomputed blacklist
3. Generate spurs radially from shape center
4. Output modified mask as PNG

**Key Components:**
- `Main.java`: Orchestrates the pipeline using config.properties
- `ImageRW.java`: Image loading/writing and mask conversion
- `Outline.java`: Morphological cleanup with 3x3 neighborhood encoding
- `Spurs.java`: Radial spur generation with angle distribution
- `Precompute.java`: Blacklist generation for connected components
- `Helper.java`: Geometric utilities (distance, angle, sorting)

## Key Patterns & Conventions

### Image Representation
- Use 1D `boolean[]` arrays for masks, indexed row-major: `y * width + x`
- Black pixels = `true`, white = `false`
- Bounds checking with `getSafe1D()` for neighborhood operations

### Morphological Operations
- 3x3 neighborhood encoded as 8-bit int (bits 0-7 clockwise from top-left)
- Precomputed blacklist excludes patterns with multiple connected components
- Iterative thinning until no removable pixels remain

### Geometric Calculations
- Center calculated as average of outline points
- Angles computed with `Math.atan2(dy, dx)` (returns -π to π)
- Points sorted by descending distance from center
- Spurs distributed evenly around 360° with configurable count

### Line Drawing
- Bresenham algorithm for drawing spurs from center to outline points
- Bounds-checked pixel setting to prevent array out-of-bounds

## Development Workflow

### Building & Running
```bash
# From project root
javac src/*.java
java -cp src Main
```

### Configuration
Edit `config.properties`:
- `input`: Path to input PNG (e.g., `images/input/art.png`)
- `output`: Path to output PNG (e.g., `images/output/spurs.png`)
- `spurcount`: Number of radial spurs to generate

### Debugging
- Use `System.out.println()` for progress tracking
- Assert statements validate array lengths and invariants
- Test morphological patterns in `Precompute.main()`

## Common Patterns

### Finding Pixels
```java
List<Integer> blackPixels = Helper.findBlack(mask);
int center = Helper.findCenter(blackPixels, width);
```

### Neighborhood Encoding
```java
int encoded = encodeDonut(mask, location, width, height);
// Check if pattern should be removed
if (isRemovable(encoded, blacklist)) mask[location] = false;
```

### Angle-Based Filtering
```java
double angle = Helper.findAngle(center, point, width);
if (!isWithinRanges(angle, angles, dAngle)) {
    // Add new spur direction
}
```

## File Organization
- `src/`: All Java source files in default package
- `images/input/`: Input images
- `images/output/`: Generated outputs
- `config.properties`: Runtime configuration

## Notes
- Heightmap generation (`Heightmap.java`) is incomplete
- ImageRW has bugs in `writeMaskToFile()` loop bounds
- No external dependencies; uses Java AWT for image processing</content>
<parameter name="filePath">c:\Users\Gijs\Documents\GitHub\NewProceduralAssetsElective\.github\copilot-instructions.md