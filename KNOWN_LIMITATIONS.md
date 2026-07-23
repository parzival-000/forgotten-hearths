# Known Limitations

- NeoForge 26.2 is a beta API line. The project is pinned to 26.2.0.25-beta and may need small updates as the platform changes.
- Homesteads generate only in new chunks. Existing terrain is not retrofitted.
- A homestead's restoration is shared. There is no per-player ownership or private reward copy.
- The three restoration places are fixed parts of the generated structure. Moving or breaking development-only marker blocks is not supported.
- The completion reward belongs to the site and is granted once. A second player who helped restore the same site does not receive another fragment.
- Automated tests validate data loading and the gameplay state machine, but final terrain fit, visual quality, and near-simultaneous two-client behavior still require the manual pass in `docs/MANUAL_TESTING.md`.
