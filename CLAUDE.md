# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android demo project for Tencent Cloud's Mobile Live Video Broadcasting (MLVB) SDK. The repository contains example implementations demonstrating live streaming features including push, play, co-anchoring, and advanced capabilities.

## Repository Structure

- **MLVB-API-Example/**: Main Android project containing all demo modules
  - **App/**: Main application entry point with navigation to all features
  - **Basic/**: Core live streaming features (push, play, co-anchoring, PK)
  - **Advanced/**: Advanced features (custom capture, beauty filters, RTC, etc.)
  - **Common/**: Shared utilities and base classes
  - **Debug/**: Configuration and test utilities
- **SDK/**: Contains Tencent Cloud LiteAV SDK AAR files

## Development Requirements

- **JDK**: Java 11 (Amazon Corretto 11.0.20.1 configured in local.properties)
- **Android Studio**: 3.5 or above
- **Device**: Android 5.0+ recommended for testing

### JDK Configuration
The project is configured to use JDK 11 via `local.properties`:
```properties
org.gradle.java.home=/Users/username/Library/Java/JavaVirtualMachines/corretto-11.0.20.1/Contents/Home
```

**Why JDK 11**:
- Required for Android Gradle Plugin 7.1.3 compatibility
- Optimal for Jetpack Compose compilation performance
- Avoids version conflicts with newer JDK versions

## Development Commands

### Building the Project
```bash
cd MLVB-API-Example
./gradlew build
```

### Running the Android App
```bash
# Install and run the main app on connected device/emulator
./gradlew :App:installDebug

# Build and install debug APK
./gradlew :App:assembleDebug

# Run the app directly (requires connected device)
./gradlew :App:installDebug && adb shell am start -n com.tencent.mlvb.apiexample/.MainActivity
```

### Clean Build
```bash
./gradlew clean
```

### Build Specific Modules
```bash
# Build individual feature modules
./gradlew :Basic:LivePushCamera:build
./gradlew :Advanced:CustomVideoCapture:build
```

## Configuration Requirements

Before building, configure these files:

### 1. Local Properties Setup
Create **MLVB-API-Example/local.properties** to configure local development settings:

```properties
# Location of the Android SDK
sdk.dir=/path/to/your/Android/sdk

# Java Home for JDK 11 (required for Android Gradle Plugin 7.1.3)
org.gradle.java.home=/path/to/your/jdk11

# Example paths:
# macOS with Android Studio:
# sdk.dir=/Users/username/Library/Android/sdk
# org.gradle.java.home=/Users/username/Library/Java/JavaVirtualMachines/corretto-11.0.25/Contents/Home

# Linux:
# sdk.dir=/home/username/Android/Sdk
# org.gradle.java.home=/usr/lib/jvm/java-11-openjdk

# Windows:
# sdk.dir=C\:\\Users\\username\\AppData\\Local\\Android\\Sdk
# org.gradle.java.home=C\:\\Program Files\\Java\\jdk-11
```

**Note**: This file should NOT be committed to version control as it contains local machine-specific paths.

### 2. Tencent Cloud Configuration
Configure **MLVB-API-Example/Debug/src/main/java/com/tencent/mlvb/debug/GenerateTestUserSig.java**:
   - `SDKAPPID`: Your Tencent Cloud application ID
   - `SDKSECRETKEY`: Your application secret key
   - `LICENSEURL`: Your MLVB license URL
   - `LICENSEURLKEY`: Your MLVB license key
   - `PUSH_DOMAIN`: Your configured push domain
   - `PLAY_DOMAIN`: Your configured playback domain
   - `LIVE_URL_KEY`: Authentication key (if enabled)

**Note**: This file remains in Java for compatibility with the SDK's signature generation requirements, but all other new code should be written in Kotlin.

## SDK Integration

The project uses Tencent Cloud LiteAV SDK Professional version via Gradle dependency:
```gradle
implementation 'com.tencent.liteav:LiteAVSDK_Professional:latest.release'
```

## Module Architecture

### Basic Features
- **LivePushCamera**: Camera-based live streaming
- **LivePushScreen**: Screen recording and streaming
- **LivePlay**: Standard live stream playback
- **LebPlay**: Low-latency WebRTC playback
- **LiveLink**: Co-anchoring between streamers
- **LivePK**: Competition mode between streamers

### Advanced Features
- **CustomVideoCapture**: Custom video input sources
- **ThirdBeauty**: Integration with beauty filter SDKs
- **RTCPushAndPlay**: RTC-based ultra-low latency streaming
- **SwitchRenderView**: Dynamic view switching capabilities
- **TimeShift**: Live stream time-shifting functionality
- **NewTimeShiftSprite**: Enhanced time-shifting with sprite support
- **PictureInPicture**: Android PiP mode support
- **LebAutoBitrate**: Auto bitrate adjustment for low-latency streaming
- **HlsAutoBitrate**: Auto bitrate adjustment for HLS streaming

## Key Technical Details

- **Minimum SDK**: Android API 19, recommended Android 5.0 (API 21)+
- **Target SDK**: 26
- **Compile SDK**: 34
- **Supported ABIs**: arm64-v8a (default), armeabi-v7a also supported
- **Build Tools**: Android Gradle Plugin 7.1.3, Kotlin 1.6.21
- **Gradle Build System**: Uses Kotlin DSL (.gradle.kts files)

## Development Notes

- Each feature module is self-contained with its own Activity and resources
- The `Common` module provides shared utilities like `MLVBBaseActivity` and `URLUtils`
- The `Debug` module contains configuration management and test user signature generation
- Live streaming requires valid Tencent Cloud credentials and domain configuration
- For production use, move UserSig generation to server-side for security

## Code Style Guidelines

- **IMPORTANT**: This project uses Kotlin as the primary language. **NEVER create new Java files (.java)**
- All new code should be written in Kotlin following idiomatic Kotlin conventions
- When converting existing Java to Kotlin, maintain the Kotlin style and avoid force unwrapping (!!)
- Use proper Kotlin null safety, scope functions, and data classes where appropriate
- Follow the patterns established in the `JAVA_TO_KOTLIN_GUIDE.md` for conversions
- Prefer data classes for simple data holders, extension functions for utilities
- Use sealed classes for type-safe hierarchies and `when` expressions for control flow

## Java to Kotlin Migration

This project has been migrated from Java to Kotlin. When working with legacy Java files:
- Refer to `JAVA_TO_KOTLIN_GUIDE.md` for comprehensive conversion patterns
- Focus on null safety, functional programming, and Kotlin idioms
- Avoid direct line-by-line translation; redesign for Kotlin's strengths
- The `GenerateTestUserSig.java` file remains in Java for SDK compatibility

## Documentation Update Policy

**IMPORTANT**: When implementing changes, improvements, or optimizations:

1. **Identify Best Practices**: If you discover a good solution or universal approach during development
2. **Document Immediately**: Update the relevant documentation files with the new patterns/solutions
3. **Update Guidelines**: Add the optimization to appropriate guide documents for future reference
4. **Maintain Consistency**: Ensure all similar implementations follow the documented best practices

### Documentation Files to Update:
- `CLAUDE.md`: General project guidelines and development practices
- `JETPACK_COMPOSE_MIGRATION_GUIDE.md`: Compose-specific patterns and solutions
- `PROJECT_STRUCTURE_CONTEXT.md`: Architecture patterns and implementation principles
- `JAVA_TO_KOTLIN_GUIDE.md`: Kotlin conversion best practices

This ensures knowledge is captured and reusable for future development work.