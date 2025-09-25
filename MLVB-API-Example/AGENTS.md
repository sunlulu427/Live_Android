# AGENTS

## Module `MLVB-API-Example`

### Mission & Entry Points
- Android demo app showcasing Tencent Cloud MLVB feature set; `App` module drives navigation
- `MainActivity.kt` routes users to feature demos via explicit intents; avoid introducing fragments unless necessary
- `MLVBApplication.kt` initializes SDK licensing state at process start

### Module Layout
- `App/`: Hosts launch UI and global lifecycle wiring
- `Common/`: Shared infrastructure (`MLVBBaseActivity`, permission helpers, URL utilities)
- `Debug/`: Local-only configuration (e.g., `GenerateTestUserSig.java` for dev signatures)
- `Basic/`: Core live scenarios (`LivePushCamera`, `LivePushScreen`, `LivePlay`, `LiveLink`, `LivePK`)
- `Advanced/`: Specialized demos (`CustomVideoCapture`, `RTCPushAndPlay`, `SwitchRenderView`, `ThirdBeauty`, `TimeShift`, `PictureInPicture`, `*AutoBitrate`)

### Core Implementation Patterns
- Derive feature activities from `MLVBBaseActivity` to unify runtime permission handling and stream ID generation
- Use `URLUtils` helpers for stream URL composition across RTMP/HLS/WebRTC/TRTC; extend via shared utilities instead of local constants
- Select protocol modes by URL or user choice; instantiate `V2TXLivePusherImpl`/player with matching `V2TXLiveMode`
- Manage lifecycle explicitly: start/stop camera, microphone, push, and rendering surfaces in `onResume`/`onPause`/`onDestroy`

### Feature Highlights
- **LivePushCamera**: Toggle RTC vs RTMP push modes; expose quality settings (resolution, frame rate, mirror)
- **LivePlay**: Accept RTMP/FLV/HLS/WebRTC URLs; auto-detect protocol and configure player accordingly
- **LiveLink & LivePK**: Coordinate simultaneous push-and-play, manage remote anchor audio mixing
- **CustomVideoCapture**: Pipeline `Camera → SurfaceTexture → OpenGL → Texture → sendCustomVideoFrame`
- **RTCPushAndPlay**: Separate anchor/audience flows using TRTC for sub-second latency
- **TimeShift/NewTimeShiftSprite**: HLS-based timeshift with sprite thumbnails for scrubbing
- **PictureInPicture**: Integrate native PiP API; handle player surface transitions across lifecycle events
- **LebAutoBitrate/HlsAutoBitrate**: Monitor bandwidth and adjust bitrate/resolution dynamically
- **ThirdBeauty**: Plug third-party beauty SDKs into GPU pipeline; manage parameter updates in real time

### Performance & Reliability Expectations
- Use hardware acceleration (MediaCodec, OpenGL ES) for processing-heavy features
- Pool and reuse buffers/textures; clean up GPU/encoder resources promptly to avoid leaks
- Implement retry/backoff for network instability; degrade quality before dropping streams
- Record and expose diagnostics (bitrate, fps, network quality) in UI where useful for debugging

### Configuration Checklist
- Keep sensitive credentials in `Debug` module only; never hardcode production secrets
- Validate permissions before starting capture; degrade gracefully if the device lacks capabilities (e.g., PiP support)
- Ensure `local.properties` supplies `sdk.dir` and `org.gradle.java.home`; update sample values in docs if they change

### Reference Material
- Review `PROJECT_STRUCTURE_CONTEXT.md` for deeper architecture notes
- Consult `JAVA_TO_KOTLIN_GUIDE.md` for Kotlin patterns when modernizing legacy Java
- For Jetpack Compose adoption, see `JETPACK_COMPOSE_MIGRATION_GUIDE.md`
