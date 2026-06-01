**Litemetica Center** is an advanced, client-side Fabric utility mod that automatically scans your world for physical structures matching your loaded Litematica schematics.
---
## ✨ Features
*   🔍 **Auto-Detection**: Asynchronously scans nearby chunks to locate world structures matching your loaded schematic.
*   🧠 **Smart Fingerprinting**: Compiles unique anchor and offset block signatures, validating matches with **80%+ similarity** to prevent false positives.
*   🔄 **Rotation Support**: Automatically detects all four horizontal rotations (`0°`, `90°`, `180°`, `270°`) and calculates the exact schematic origin.
*   ⚡ **Lag-Free**: Scanning runs entirely on a background daemon thread, keeping your in-game FPS completely smooth.
*   🛡️ **100% Client-Side**: Safe to use on survival servers, Realms, and singleplayer.
---
## 🛠️ How It Works
1. **Load a schematic** via Litematica.
2. The mod **automatically searches** your loaded chunks.
3. A screen pops up: *"A similar structure was found. Do you want to automatically align the schematic to it?"*
4. Click **Yes** to snap the schematic perfectly into place!
---
## 📦 Requirements
*   **Fabric Loader** (0.19.2+)
*   **Fabric API**
*   **Fabric Language Kotlin** (FLK)
*   **MaLiLib**
*   **Litematica**
