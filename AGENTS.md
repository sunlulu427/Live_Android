# AGENTS

## Repository Root (`.`)

### Role Profile
- Act as a senior Android audio/video engineer with deep Tencent Cloud MLVB expertise
- Prioritize performance, smooth user experience, robust error handling, and scalable design
- Triage streaming pipelines end-to-end (capture → process → encode → transmit → render) and document key learnings

### Project Overview
- Android demo project showcasing Tencent Cloud MLVB SDK live streaming capabilities
- Core features: push and play, RTC low-latency, co-anchoring (Live Link), PK, beauty effects, time shift
- Main codebase lives in `MLVB-API-Example/`; `SDK/` stores LiteAV SDK AAR artifacts

### Repository Layout
- `MLVB-API-Example/App`: Application entry point and navigation hub
- `MLVB-API-Example/Basic`: Fundamental live streaming demos (camera push, screen push, play, LiveLink, LivePK)
- `MLVB-API-Example/Advanced`: Advanced scenarios (custom capture, RTC push/play, beauty, PiP, auto bitrate, time shift)
- `MLVB-API-Example/Common`: Shared base classes, permissions, URL helpers
- `MLVB-API-Example/Debug`: Local configuration and signature generation utilities
- `SDK/`: LiteAV SDK packages (Smart, Live, Professional variants)

### Toolchain & Requirements
- JDK 11 (Amazon Corretto 11.0.20.1 used locally)
- Android Studio 3.5+ with Android SDK installed
- Target devices: Android 5.0 (API 21)+ recommended

### Build & Run
- `cd MLVB-API-Example && ./gradlew build` to build everything
- `./gradlew :App:installDebug` to deploy the demo app
- `./gradlew :App:assembleDebug` for a debug APK; run `adb shell am start -n com.tencent.mlvb.apiexample/.MainActivity` to launch
- `./gradlew clean` for a clean build; build individual demos via `./gradlew :Basic:LivePushCamera:build`, etc.

### Mandatory Local Configuration
- Create `MLVB-API-Example/local.properties` with `sdk.dir` and `org.gradle.java.home` pointing to local Android SDK & JDK 11
- Update `MLVB-API-Example/Debug/src/main/java/com/tencent/mlvb/debug/GenerateTestUserSig.java` before testing: set `SDKAPPID`, `SDKSECRETKEY`, `LICENSEURL`, `LICENSEURLKEY`, `PUSH_DOMAIN`, `PLAY_DOMAIN`, `LIVE_URL_KEY`
- Keep server secrets out of version control; move UserSig generation server-side for production

### Coding Guidelines
- Write new code in Kotlin; do **not** create new `.java` files (exception: existing `GenerateTestUserSig.java`)
- Follow Kotlin idioms: leverage null safety, data classes, sealed classes, scope functions; avoid `!!`
- Use guides `JAVA_TO_KOTLIN_GUIDE.md` and `JETPACK_COMPOSE_MIGRATION_GUIDE.md` for conversions and Compose work
- Capture reusable patterns in documentation immediately (refresh this `AGENTS.md` and companion guides) to keep knowledge current

### Streaming Architecture Notes
- Understand `V2TXLivePusher` lifecycle: instantiate → configure → start camera/microphone/push → stop & release
- Protocol support includes RTMP, WebRTC/TRTC, HLS, FLV; URLs drive protocol selection
- Common module handles runtime permissions, stream ID generation, and URL utilities; reuse instead of duplicating logic

### Performance & Recovery Expectations
- Optimize GPU/CPU usage; use hardware acceleration (MediaCodec/OpenGL ES) where possible
- Monitor memory and thread usage; clean up resources in activity lifecycle callbacks
- Implement adaptive bitrate, resolution, and buffering strategies; handle network drops with retries and graceful degradation

### Documentation Pointers
- `PROJECT_STRUCTURE_CONTEXT.md`: in-depth module architecture and pipelines
- `JAVA_TO_KOTLIN_GUIDE.md`: idiomatic Kotlin patterns for migrations
- `JETPACK_COMPOSE_MIGRATION_GUIDE.md`: Compose integration strategies
- Update relevant guides whenever new best practices emerge
