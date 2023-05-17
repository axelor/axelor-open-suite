# Axelor Budget

## Description

Stable version of 6.1 version for budget


## Dependencies compatibility


Budget version | Compatible from AOS version | Compatible to AOS version
 --- | --- | --- 
6.1 | 6.4 | -


## How to release

1. Checkout

    Checkout `main` branch. If you want to release an old version, checkout the right `x.y-stable` branch.

2. Verify

    Make sure `version.txt` is the desired version. Else, do and commit necessary change.
    Make sure `CHANGELOG.md` include the changes in the `## Current (unreleased)` section.

3. Release

    Run `./release`. It will :
    - Update `CHANGELOG.md` for the release : Moving `## Current (unreleased)` section for the tag.
    - Create and push a commit `Release x.y.z`.
    - Merge into `x.y-stable` branch.
    - Create the tag corresponding to the version.

    In case you want to release and old version, run `./release x.y-stable` where `x.y-stable` is the stable branch of the version you want to release.

4. In case you release the latest version, update `version.txt` with the next version and push it to `main` branch.
