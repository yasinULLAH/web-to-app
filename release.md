# Release Guide for WebToApp

This guide explains how to create a new release for your fork or for the main repository.

## 1. Automated Releases via GitHub Actions

The most efficient way to create a release is to use the provided GitHub Actions workflow.

### Steps:
1. **Push your changes** to your main branch or a release branch.
2. Go to the **Actions** tab on your GitHub repository.
3. Select the **Build APK** workflow.
4. Click **Run workflow**.
5. Select `release` or `both` in the "Build Type" dropdown.
6. Once the workflow completes successfully, an APK artifact will be generated.

### Creating a GitHub Release:
1. Go to the **Releases** section on your repository homepage.
2. Click **Draft a new release**.
3. Create a new tag (e.g., `v1.9.7`).
4. Give the release a title and description (you can use the commit log for ideas).
5. **Download the APKs** from the GitHub Actions run and **upload them** as assets to this release.
6. Click **Publish release**.

## 2. Manual Local Builds

If you prefer to build locally:

### Prerequisites:
- Ensure you have a `keystore` file for signing.
- Configure `signingConfigs` in `app/build.gradle.kts`.

### Commands:
```bash
# Clean and build the release APK
./gradlew clean assembleRelease
```

The APK will be located in `app/build/outputs/apk/release/`.

## 3. Versioning

Before creating a release, remember to update the version in `app/build.gradle.kts`:
- `versionCode`: Increment this (integer).
- `versionName`: Update this (e.g., from `1.9.6` to `1.9.7`).

## 4. Contributors

When publishing a release, it is good practice to credit the major contributors in the release notes.

**Current Major Contributor:**
- **Yasin Ullah** (@yasinULLAH): Enhanced NativeBridge, GitHub Actions CI, and Documentation.
