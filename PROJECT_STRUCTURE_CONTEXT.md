# MLVB Android Project Structure & Implementation Context

## Project Architecture Overview

The MLVB (Mobile Live Video Broadcasting) Android demo project is a **modular architecture** demonstrating Tencent Cloud's LiteAV SDK capabilities. The project follows a **feature-based module structure** where each live streaming capability is implemented as an independent module.

### High-Level Architecture Pattern

```
Application Layer (App Module)
    ↓
Feature Modules (Basic + Advanced)
    ↓
Common Utilities (Common Module)
    ↓
Debug/Configuration (Debug Module)
    ↓
Tencent Cloud LiteAV SDK
```

## Core Module Structure & Implementation Principles

### 1. **App Module** - Application Entry Point
**Location**: `MLVB-API-Example/App/`
**Role**: Central navigation hub and application lifecycle management

#### Key Implementation:
- **MainActivity.kt**: Primary navigation controller using explicit Intent routing
- **MLVBApplication.kt**: Application-level initialization and SDK license setup
- **Architecture Pattern**: Simple Activity-based navigation without fragments

```kotlin
// Navigation pattern used throughout
findViewById<View>(R.id.ll_push_camera).setOnClickListener {
    startActivity(Intent(this, LivePushCameraEnterActivity::class.java))
}
```

### 2. **Common Module** - Shared Infrastructure
**Location**: `MLVB-API-Example/Common/`
**Role**: Provides shared utilities, base classes, and common functionality

#### Core Components:

##### **MLVBBaseActivity.kt** - Base Activity Pattern
- **Responsibility**: Runtime permission management, status bar configuration, stream ID generation
- **Permission Model**: Handles CAMERA, RECORD_AUDIO, STORAGE permissions uniformly
- **Extension Pattern**: All feature activities inherit from this base

```kotlin
abstract class MLVBBaseActivity : AppCompatActivity() {
    protected abstract fun onPermissionGranted()
    protected fun checkPermission(): Boolean
}
```

##### **URLUtils.kt** - URL Generation Engine
- **Responsibility**: Stream URL generation for different protocols
- **Protocol Support**: RTMP, WebRTC, HLS, FLV, TRTC
- **Security Integration**: Integrates with authentication and domain configuration

**URL Generation Patterns**:
```kotlin
// Push URL patterns
RTC:  "trtc://cloud.tencent.com/push/{streamId}?sdkappid={id}&userid={userId}&usersig={sig}"
RTMP: "rtmp://{domain}/live/{streamId}{Params}"

// Play URL patterns
WebRTC: "webrtc://{domain}/live/{streamId}"
HLS:    "http://{domain}/live/{streamId}.m3u8"
FLV:    "http://{domain}/live/{streamId}.flv"
```

### 3. **Debug Module** - Configuration Management
**Location**: `MLVB-API-Example/Debug/`
**Role**: Development configuration and authentication

#### **GenerateTestUserSig.java** - Authentication Engine
- **Responsibility**: UserSig generation, domain configuration, URL authentication
- **Security Model**: Client-side signature generation (development only)
- **Configuration Points**: SDKAPPID, SDKSECRETKEY, domain settings, license configuration

### 4. **Basic Modules** - Core Live Streaming Features

#### **LivePushCamera** - Camera-based Live Streaming
**Implementation Architecture**:
```kotlin
// Core streaming pipeline
V2TXLivePusher creation → Camera setup → Stream configuration → Push start
```

**Key Technical Patterns**:
- **Dual Protocol Support**: RTC mode vs RTMP mode based on `streamType`
- **Dynamic Quality Control**: Resolution, rotation, mirror settings during live streaming
- **Audio Management**: Microphone enable/disable with real-time feedback

**SDK Integration Pattern**:
```kotlin
// Mode selection affects entire pipeline
livePusher = when(streamType) {
    0 -> V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC)
    1 -> V2TXLivePusherImpl(this, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTMP)
}
```

#### **LivePlay** - Stream Playback Engine
**Implementation Architecture**:
- **Multi-protocol Player**: Supports RTMP, FLV, HLS, WebRTC protocols
- **Adaptive Streaming**: URL-based protocol detection and player initialization
- **Buffer Management**: Automatic buffering strategies for different network conditions

#### **LiveLink & LivePK** - Interactive Broadcasting
**Implementation Architecture**:
- **Real-time Communication**: RTC-based co-anchoring between multiple streamers
- **Multi-stream Management**: Simultaneous push/pull stream handling
- **Audio Mixing**: Real-time audio mixing for multiple participants

### 5. **Advanced Modules** - Specialized Features

#### **CustomVideoCapture** - Custom Video Pipeline
**Deep Implementation Analysis**:

##### **OpenGL Rendering Pipeline**:
```
Camera → SurfaceTexture → OpenGL Processing → Texture → SDK
```

**Key Components**:
- **EGL Context Management**: `EGL10Helper`, `EGL14Helper`, `EglCore`
- **Shader Processing**: `GPUImageFilter`, `OesInputFilter` for real-time effects
- **Texture Management**: `TextureFrame`, `FrameBuffer` for efficient GPU memory handling

**Custom Capture Flow**:
```kotlin
// Custom capture integration
mLivePusher?.enableCustomVideoCapture(true)
val videoFrame = V2TXLiveDef.V2TXLiveVideoFrame().apply {
    pixelFormat = V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D
    bufferType = V2TXLiveBufferType.V2TXLiveBufferTypeTexture
}
mLivePusher?.sendCustomVideoFrame(videoFrame)
```

#### **RTCPushAndPlay** - Ultra-Low Latency Streaming
**Implementation Architecture**:
- **Dual-mode Operation**: Separate anchor and audience implementations
- **Real-time Protocol**: TRTC protocol for sub-second latency
- **Network Adaptation**: Automatic quality adjustment based on network conditions

#### **TimeShift & NewTimeShiftSprite** - Live Stream Time Control
**Implementation Architecture**:
- **HLS-based**: Uses HLS protocol's segment-based architecture for time shifting
- **Sprite Integration**: Video thumbnail sprites for timeline scrubbing
- **Buffer Management**: Maintains playback buffer for backward seeking

#### **PictureInPicture** - Android PiP Integration
**Implementation Architecture**:
- **Android PiP API**: Native Android Picture-in-Picture mode integration
- **Video Surface Management**: Seamless surface transfer between full-screen and PiP
- **Lifecycle Management**: Proper handling of Activity lifecycle during PiP transitions

#### **Auto Bitrate Modules** (LebAutoBitrate, HlsAutoBitrate)
**Implementation Architecture**:
- **Network Monitoring**: Real-time network quality assessment
- **Adaptive Algorithms**: Dynamic bitrate adjustment based on bandwidth and device performance
- **Quality Metrics**: Balance between quality and smooth playback

#### **ThirdBeauty** - Beauty Filter Integration
**Implementation Architecture**:
- **Plugin Architecture**: Modular integration with third-party beauty SDKs
- **Real-time Processing**: GPU-accelerated beauty filters in the video pipeline
- **Effect Management**: Real-time parameter adjustment for beauty effects

## SDK Integration Patterns

### **V2TXLivePusher Architecture**
The core streaming engine follows this pattern:
```kotlin
// Initialization
pusher = V2TXLivePusherImpl(context, mode)

// Configuration
pusher.setRenderView(view)
pusher.setAudioQuality(quality)
pusher.setVideoQuality(param)

// Lifecycle
pusher.startCamera(frontCamera)
pusher.startPush(url)
pusher.startMicrophone()

// Cleanup
pusher.stopCamera()
pusher.stopPush()
```

### **Multi-Protocol Support Strategy**
The project implements protocol abstraction:
- **URL-based Protocol Detection**: Protocol determined by URL prefix
- **Mode-specific Initialization**: Different pusher/player instances for different protocols
- **Quality Adaptation**: Protocol-specific quality parameters and network handling

## Audio/Video Pipeline Architecture

### **Video Processing Pipeline**
```
Camera/Custom Source → Capture → Processing (Filters/Effects) → Encoding → Network Transmission
                                     ↓
                              GPU-based OpenGL Processing
                                     ↓
                              Real-time Rendering → Display
```

### **Audio Processing Pipeline**
```
Microphone → Audio Capture → Processing (Noise Reduction) → Encoding → Network Transmission
                                     ↓
                              Audio Mixing (for multi-party scenarios)
```

## Network & Protocol Implementation

### **Streaming Protocols Used**
1. **RTMP**: Traditional live streaming, higher latency (~3-5s)
2. **WebRTC**: Low-latency streaming (~300ms), real-time communication
3. **TRTC**: Tencent's optimized RTC protocol for ultra-low latency
4. **HLS**: Adaptive bitrate streaming with time-shift support
5. **FLV**: HTTP-based streaming with good compatibility

### **Quality Control Implementation**
- **Adaptive Bitrate**: Real-time adjustment based on network conditions
- **Resolution Scaling**: Dynamic resolution changes during streaming
- **Frame Rate Control**: Automatic frame rate adjustment for performance
- **Buffer Management**: Intelligent buffering strategies for smooth playback

## Performance Optimization Strategies

### **Memory Management**
- **Object Pooling**: Reuse of video frames and buffers
- **Texture Management**: Efficient GPU memory handling
- **Lifecycle Awareness**: Proper cleanup in Activity lifecycle methods

### **Threading Architecture**
- **UI Thread**: UI updates and user interactions
- **Camera Thread**: Camera operations and capture
- **Encoding Thread**: Video/audio encoding operations
- **Network Thread**: Stream transmission and network operations

### **GPU Optimization**
- **Hardware Acceleration**: Use of MediaCodec for encoding/decoding
- **OpenGL ES**: GPU-based video processing and effects
- **Surface Management**: Efficient video surface handling

## Error Handling & Recovery Patterns

### **Network Error Recovery**
- **Automatic Reconnection**: Built-in retry mechanisms for network failures
- **Quality Degradation**: Automatic quality reduction under poor network conditions
- **Fallback Protocols**: Protocol switching when primary protocol fails

### **Resource Management**
- **Permission Validation**: Runtime permission checks before camera/microphone access
- **Hardware Capability Checks**: Device capability validation before feature activation
- **Graceful Degradation**: Feature disabling when hardware doesn't support advanced capabilities

This architecture demonstrates production-ready patterns for building scalable, performant live streaming applications using Tencent Cloud's MLVB SDK, with clear separation of concerns and modular design principles.