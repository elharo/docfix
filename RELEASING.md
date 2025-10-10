# Release Process

This document describes the process for releasing a new version of DocFix.

## Before Release

### Update the Reproducible Build Timestamp

The project uses [Maven reproducible builds](https://maven.apache.org/guides/mini/guide-reproducible-builds.html) to ensure that identical source code produces identical artifacts. Before each release, update the `project.build.outputTimestamp` property in the root `pom.xml` to the current date:

1. Open `pom.xml` in the root directory
2. Locate the `<project.build.outputTimestamp>` property in the `<properties>` section
3. Update it to the current date in ISO 8601 format: `YYYY-MM-DDTHH:MM:SSZ`
   - Example: `2025-10-13T00:00:00Z`
   - Use `00:00:00` for the time component
   - Always use UTC timezone (indicated by the `Z` suffix)

This timestamp will be embedded in all build artifacts (JARs, etc.) for this release, ensuring reproducibility.

## Release Steps

1. Update the timestamp as described above
2. Commit the timestamp change
3. Follow the standard Maven release process
4. Tag the release
5. Deploy artifacts
