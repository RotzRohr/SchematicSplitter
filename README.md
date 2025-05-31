# SchematicSplitter

A powerful Paper/Spigot plugin that intelligently splits large WorldEdit schematics into smaller, manageable chunks to prevent server crashes and lag during pasting operations. Perfect for massive builds like castles, cities, or terrain features.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [File Structure](#file-structure)
- [Technical Details](#technical-details)
- [Troubleshooting](#troubleshooting)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Features
- **Smart Schematic Splitting**: Divide large schematics into configurable X×Y grid chunks
- **Automatic Format Conversion**: Converts old MCEdit (.schematic) format to modern Sponge format (.schem)
- **Asynchronous Operations**: All splitting and pasting operations run asynchronously to prevent server lag
- **Player History Integration**: Paste operations are tracked in WorldEdit history for undo functionality
- **Configurable Delays**: Set custom delays between chunk pastes to maintain server performance
- **Progress Tracking**: Real-time feedback on splitting and pasting operations

### Advanced Features
- **Custom Start Positions**: Specify exact coordinates for manual pasting instructions
- **Metadata Preservation**: Maintains chunk dimensions and positioning data for accurate reconstruction
- **Multi-Format Support**: Works with both .schem and .schematic file formats
- **Automatic Instructions**: Generates detailed manual pasting instructions for non-Paper servers
- **Tab Completion**: Full tab completion support for all commands and parameters

## Requirements

- **Dependencies**: WorldEdit 7.2+ or FastAsyncWorldEdit (FAWE) 2.0+
- **Permissions**: Operator status or appropriate permissions

## Installation

1. **Download Dependencies**
   - Install either [WorldEdit](https://dev.bukkit.org/projects/worldedit) or [FastAsyncWorldEdit](https://intellectualsites.github.io/download/fawe.html)
   - FastAsyncWorldEdit is recommended for better performance with large schematics

2. **Install Plugin**
   - Download the latest SchematicSplitter.jar from the releases page
   - Place the JAR file in your server's `plugins` folder
   - Restart your server

3. **Verify Installation**
   - Run `/plugins` to ensure SchematicSplitter is loaded
   - Check console for any error messages
   - Create the necessary folders if they don't exist

## Commands

### Main Command
```
/schematicsplitter <subcommand>
/schemsplit <subcommand>  (alias)
```

### Split Command
Splits a schematic file into smaller chunks.

```
/schematicsplitter split <filename> <x-chunks> <y-chunks> [start-x] [start-y] [start-z]
```

**Parameters:**
- `filename`: Name of the schematic file (without extension)
- `x-chunks`: Number of chunks along the X-axis (width)
- `y-chunks`: Number of chunks along the Z-axis (length)
- `start-x, start-y, start-z`: (Optional) Starting coordinates for manual pasting instructions

**Examples:**
```
/schematicsplitter split castle 4 4
/schematicsplitter split city 8 8 1000 64 -500
```

### Paste Command
Automatically pastes all chunks of a split schematic.

```
/schematicsplitter paste <schematic-name>
```

**Parameters:**
- `schematic-name`: Name of the split schematic (without _split suffix)

**Example:**
```
/schematicsplitter paste castle
```

### NextTile Command
Manually paste the next chunk when using WorldEdit in manual mode.

```
/schematicsplitter nexttile
```

**Usage:**
- Only available when `paste-delay-worldedit` is set to -1
- Use after starting a paste operation with `/schematicsplitter paste`
- Allows full control over when each chunk is pasted

**Example Workflow:**
```
/schematicsplitter paste castle       # Starts manual paste session
/schematicsplitter nexttile          # Pastes chunk 1
/schematicsplitter nexttile          # Pastes chunk 2
# Continue until all chunks are pasted
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `schematicsplitter.use` | Access to all SchematicSplitter commands | op |
| `schematicsplitter.split` | Permission to split schematics | op |
| `schematicsplitter.paste` | Permission to use automatic pasting | op |

## Configuration

The plugin configuration is located at `plugins/SchematicSplitter/config.yml`:

```yaml
# SchematicSplitter Configuration

# Delay between pasting chunks when using FastAsyncWorldEdit (in server ticks, 20 ticks = 1 second)
# FAWE can handle faster pasting due to its async nature
paste-delay-fawe: 40

# Delay between pasting chunks when using regular WorldEdit (in server ticks, 20 ticks = 1 second)
# Set to -1 for manual mode where players must use /schematicsplitter nexttile to paste each chunk
# This is recommended for servers with limited resources or when pasting very large chunks
paste-delay-worldedit: -1

# Whether to announce each chunk paste in chat
announce-chunks: true

# Maximum chunks to paste per operation (safety limit)
max-chunks: 100
```

### Delay Configuration

The plugin automatically detects whether you're using FastAsyncWorldEdit (FAWE) or regular WorldEdit and applies the appropriate delay settings:

**FastAsyncWorldEdit (FAWE)**
- Uses `paste-delay-fawe` setting
- Default: 40 ticks (2 seconds)
- FAWE's async nature allows for faster pasting without server lag
- Recommended for most servers

**Regular WorldEdit**
- Uses `paste-delay-worldedit` setting
- Default: -1 (manual mode)
- Manual mode requires using `/schematicsplitter nexttile` for each chunk
- Set to a positive number (e.g., 60) for automatic pasting with delays
- Manual mode is recommended for:
  - Servers with limited resources
  - Very large chunks that might cause lag
  - When you need precise control over pasting timing

## Usage Guide

### Step 1: Prepare Your Schematic

1. Create a folder structure if it doesn't exist:
   ```
   plugins/SchematicSplitter/
   ├── Input/
   └── Output/
   ```

2. Place your schematic file in the Input folder:
   ```
   plugins/SchematicSplitter/Input/castle.schem
   ```

### Step 2: Split the Schematic

1. Decide on your grid size based on the schematic size:
   - Small builds (< 100x100): 2x2 grid
   - Medium builds (100-500): 4x4 grid
   - Large builds (500-1000): 8x8 grid
   - Massive builds (1000+): 16x16 or larger

2. Run the split command:
   ```
   /schematicsplitter split castle 4 4
   ```

3. Wait for the operation to complete. You'll see:
   ```
   Starting to split schematic: castle into 4x4 chunks...
   Successfully split schematic into 16 parts!
   ```

### Step 3: Paste the Schematic

#### Automatic Pasting (Paper Servers)

1. Stand where you want the schematic to be pasted
2. Run the paste command:
   ```
   /schematicsplitter paste castle
   ```
3. The plugin will paste each chunk with delays to prevent lag
4. Use `//undo` to undo if needed (tracked in your WorldEdit history)

#### Manual Pasting (Other Servers)

1. Navigate to the output folder:
   ```
   plugins/SchematicSplitter/Output/castle_split/
   ```

2. Open `instructions.md` for detailed pasting commands

3. Follow the commands in order, adjusting coordinates as needed

## File Structure

### Input Directory
```
plugins/SchematicSplitter/Input/
├── castle.schem
├── city.schematic
└── terrain.schem
```

### Output Directory
```
plugins/SchematicSplitter/Output/
└── castle_split/
    ├── castle_split_0_0.schem
    ├── castle_split_0_1.schem
    ├── castle_split_0_2.schem
    ├── castle_split_0_3.schem
    ├── castle_split_1_0.schem
    ├── ...
    ├── instructions.md
    └── metadata.yml
```

### File Descriptions

- **Split Files**: Individual chunk files named with their grid position
- **instructions.md**: Step-by-step manual pasting instructions with coordinates
- **metadata.yml**: Stores chunk dimensions and grid information for accurate pasting

## Technical Details

### Splitting Algorithm

1. **Load Schematic**: Reads the original schematic file
2. **Calculate Dimensions**: Divides total size by requested chunks
3. **Create Chunks**: Each chunk is a self-contained schematic
4. **Set Origins**: All chunks have origin at (0,0,0) for consistent pasting
5. **Save Metadata**: Stores original chunk dimensions for proper spacing

### Format Conversion

- Automatically converts old MCEdit format (.schematic) to Sponge format (.schem)
- Preserves all blocks, entities, and tile entities
- Maintains proper block states and NBT data

### Performance Optimizations

- Asynchronous file operations
- Chunked reading/writing to manage memory
- Configurable delays to prevent server overload
- Efficient block copying algorithms

## Troubleshooting

### Common Issues

**"Schematic file not found"**
- Ensure the file is in the Input folder
- Check file extension (.schem or .schematic)
- Verify file permissions

**"Failed to split schematic: EOFException"**
- File may be corrupted
- Try re-exporting from WorldEdit
- Check if file is complete

**"Too many chunks! Maximum allowed: X"**
- Reduce the grid size
- Increase max-chunks in config.yml
- Consider splitting in multiple operations

**Gaps between pasted chunks**
- Ensure you're using the latest version
- Check that metadata.yml exists
- Verify chunk dimensions match

### Performance Tips

1. **Use FAWE**: FastAsyncWorldEdit handles large operations better
2. **Adjust Delays**: Increase paste-delay for weaker servers
3. **Split Wisely**: More chunks = better performance but more files
4. **Monitor Resources**: Watch CPU/RAM during operations

## Building from Source

### Prerequisites
- Java Development Kit (JDK) 21+
- Apache Maven 3.6+
- Git

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/SchematicSplitter.git
   cd SchematicSplitter
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. Find the compiled JAR:
   ```bash
   target/SchematicSplitter-1.jar
   ```

### Development Setup

1. Import as Maven project in your IDE
2. Set Java 21 as project SDK
3. Add Paper API as dependency
4. Run Maven install to download dependencies

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

### Contribution Guidelines

- Follow existing code style
- Add JavaDoc for public methods
- Include unit tests for new features
- Update README for significant changes
- Test on both Paper and Spigot

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

- **Issues**: Report bugs on the GitHub issue tracker
- **Discord**: Join our support Discord server
- **Wiki**: Check the wiki for advanced usage

---

**Note**: This plugin is not affiliated with Mojang, Minecraft, or WorldEdit. It is an independent tool designed to work alongside these products.