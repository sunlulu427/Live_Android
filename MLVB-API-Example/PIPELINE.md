# MLVB Stream Processing Pipeline Documentation

This document describes the core pipeline architecture for pushing and pulling streams in the MLVB (Mobile Live Video Broadcasting) SDK.

## Push Stream Pipeline

The push stream pipeline consists of the following stages:

### 1. Capture
**Purpose**: Acquire video and audio data from input sources
- **Video Sources**: Camera (`startCamera()`), Screen recording, Custom video frames
- **Audio Sources**: Microphone (`startMicrophone()`), Custom audio data
- **Implementation**:
  - Camera capture via `V2TXLivePusher.startCamera(true/false)` for front/rear camera
  - Custom capture via `enableCustomVideoCapture(true)` + `sendCustomVideoFrame()`
  - Screen capture through system APIs

### 2. Pre-processing
**Purpose**: Apply effects and filters before encoding
- **Beauty Filters**: Third-party beauty SDKs (Tencent Effect, FaceUnity)
- **Video Effects**: Rotation, mirror, resolution adjustments
- **Audio Processing**: Noise reduction, echo cancellation
- **Implementation**:
  - Enable custom processing: `enableCustomVideoProcess(true, pixelFormat, bufferType)`
  - Process frames in `V2TXLivePusherObserver.onProcessVideoFrame()`
  - Apply effects via third-party SDKs or custom algorithms

### 3. Mixing
**Purpose**: Combine multiple audio/video streams (multi-anchor scenarios)
- **Video Mixing**: Overlay multiple video streams with positioning
- **Audio Mixing**: Mix multiple audio sources with volume control
- **Use Cases**: Co-anchoring, PK (Player vs Player) battles
- **Implementation**:
  - Multi-stream scenarios handled by higher-level components
  - Individual stream mixing done at application level

### 4. Codec (Encoding)
**Purpose**: Compress video and audio data for transmission
- **Video Codec**: H.264/H.265 hardware/software encoding
- **Audio Codec**: AAC encoding
- **Quality Control**:
  - Video: Resolution (`setVideoQuality()`), bitrate, frame rate
  - Audio: Sample rate, bitrate (`setAudioQuality()`)
- **Implementation**:
  - Automatic codec selection based on device capabilities
  - Quality parameters set via `V2TXLiveVideoEncoderParam` and audio quality enums

### 5. Render (Local Preview)
**Purpose**: Display local preview to the broadcaster
- **Render View**: `TXCloudVideoView` for local video preview
- **Render Controls**: Mirror mode, rotation, aspect ratio
- **Implementation**:
  - Set render view: `setRenderView(TXCloudVideoView)`
  - Configure rendering: `setRenderMirror()`, `setRenderRotation()`

### 6. Push (Network Transmission)
**Purpose**: Send encoded stream to streaming server
- **Protocols**: RTMP (standard), RTC (ultra-low latency), WebRTC
- **Network Adaptation**: Adaptive bitrate, network quality monitoring
- **Implementation**:
  - Start pushing: `startPush(pushUrl)`
  - URL generation via `URLUtils.generatePushUrl()`
  - Protocol selection via `V2TXLiveMode` (RTMP/RTC)

## Pull Stream Pipeline

The pull stream pipeline handles receiving and playing live streams:

### 1. Network Reception
**Purpose**: Receive encoded stream data from streaming server
- **Protocols**: RTMP, FLV, HLS, RTC (ultra-low latency)
- **Network Handling**: Buffering, packet loss recovery, adaptive streaming
- **Implementation**:
  - Player creation: `V2TXLivePlayerImpl(context)`
  - URL types: RTMP (`rtmp://`), FLV (`.flv`), HLS (`.m3u8`), RTC custom URLs

### 2. Demux (Stream Separation)
**Purpose**: Separate audio and video streams from multiplexed data
- **Container Formats**: FLV, MPEG-TS (HLS), Custom RTC format
- **Stream Synchronization**: Audio-video sync maintenance
- **Implementation**: Handled internally by SDK

### 3. Decode (Decompression)
**Purpose**: Decompress encoded audio and video data
- **Video Decode**: H.264/H.265 hardware/software decoding
- **Audio Decode**: AAC decoding
- **Performance**: Hardware acceleration when available
- **Implementation**: Automatic decoder selection by SDK

### 4. Post-processing
**Purpose**: Apply client-side effects and adjustments
- **Video Processing**: Color correction, scaling, rotation
- **Audio Processing**: Volume adjustment, equalization
- **Custom Processing**: Via `onRenderVideoFrame()` callback
- **Implementation**: Optional custom processing in observer callbacks

### 5. Render (Display)
**Purpose**: Display video and play audio to the viewer
- **Video Render**: `TXCloudVideoView` for video display
- **Audio Render**: System audio output
- **Render Controls**: Aspect ratio, fill mode, rotation
- **Implementation**:
  - Set render view: `setRenderView(TXCloudVideoView)`
  - Start playback: `startLivePlay(playUrl)`

### 6. Synchronization
**Purpose**: Maintain audio-video synchronization
- **A/V Sync**: Automatic synchronization of audio and video streams
- **Buffer Management**: Adaptive buffering based on network conditions
- **Latency Control**: Buffer size adjustment for latency vs stability trade-off
- **Implementation**: Handled automatically by SDK

## Pipeline Flow Diagrams

### Push Pipeline Flow (PlantUML)

```plantuml
@startuml push_pipeline
!theme plain
skinparam backgroundColor #FFFFFF
skinparam componentStyle rectangle

package "Input Sources" {
    [Camera] as camera
    [Microphone] as mic
    [Screen] as screen
    [Custom Video] as customv
    [Custom Audio] as customa
}

package "1. Capture" {
    [Video Capture] as vcapture
    [Audio Capture] as acapture
}

package "2. Pre-processing" {
    [Beauty Filters] as beauty
    [Video Effects] as veffects
    [Audio Processing] as aprocess
}

package "3. Mixing" {
    [Video Mixer] as vmixer
    [Audio Mixer] as amixer
}

package "4. Codec" {
    [H.264/H.265 Encoder] as vcodec
    [AAC Encoder] as acodec
}

package "5. Render" {
    [Local Preview] as preview
    [TXCloudVideoView] as renderview
}

package "6. Push" {
    [Network Stack] as network
    [RTMP/RTC Server] as server
}

' Input to Capture
camera --> vcapture
mic --> acapture
screen --> vcapture
customv --> vcapture
customa --> acapture

' Capture to Pre-processing
vcapture --> beauty
vcapture --> veffects
acapture --> aprocess

' Pre-processing to Mixing
beauty --> vmixer
veffects --> vmixer
aprocess --> amixer

' Mixing to Codec
vmixer --> vcodec
amixer --> acodec

' Codec to Render and Push
vcodec --> preview
vcodec --> network
acodec --> network

' Render
preview --> renderview

' Push to Server
network --> server

note right of beauty : enableCustomVideoProcess()\nonProcessVideoFrame()
note right of vcapture : startCamera()\nenableCustomVideoCapture()
note right of network : startPush(pushUrl)\nV2TXLiveMode.RTMP/RTC
@enduml
```

### Pull Pipeline Flow (PlantUML)

```plantuml
@startuml pull_pipeline
!theme plain
skinparam backgroundColor #FFFFFF
skinparam componentStyle rectangle

package "Network" {
    [RTMP/FLV Server] as rtmpserver
    [HLS Server] as hlsserver
    [RTC Server] as rtcserver
}

package "1. Reception" {
    [Network Stack] as network
    [Protocol Handler] as protocol
}

package "2. Demux" {
    [Stream Demuxer] as demux
    [A/V Separator] as separator
}

package "3. Decode" {
    [H.264/H.265 Decoder] as vdecoder
    [AAC Decoder] as adecoder
}

package "4. Post-processing" {
    [Video Processing] as vprocess
    [Audio Processing] as aprocess
}

package "5. Render" {
    [Video Renderer] as vrender
    [Audio Renderer] as arender
    [TXCloudVideoView] as renderview
    [System Audio] as systemaudio
}

package "6. Synchronization" {
    [A/V Sync] as avsync
    [Buffer Manager] as buffer
}

' Server to Reception
rtmpserver --> network
hlsserver --> network
rtcserver --> network

' Reception to Demux
network --> protocol
protocol --> demux
demux --> separator

' Demux to Decode
separator --> vdecoder : Video Stream
separator --> adecoder : Audio Stream

' Decode to Post-processing
vdecoder --> vprocess
adecoder --> aprocess

' Post-processing to Render
vprocess --> vrender
aprocess --> arender

' Render to Output
vrender --> renderview
arender --> systemaudio

' Synchronization
vrender --> avsync
arender --> avsync
avsync --> buffer

note right of network : startLivePlay(playUrl)\nV2TXLivePlayerImpl
note right of vprocess : onRenderVideoFrame()\nCustom effects
note right of avsync : Automatic A/V sync\nLatency control
@enduml
```

### Simplified Architecture Overview (PlantUML)

```plantuml
@startuml architecture_overview
!theme plain
skinparam backgroundColor #FFFFFF

package "Push Side (Broadcaster)" {
    [Mobile App] as pusher_app
    [V2TXLivePusher] as pusher_sdk
    [Camera/Mic] as input_devices

    pusher_app --> pusher_sdk
    input_devices --> pusher_sdk
}

package "Streaming Infrastructure" {
    [RTMP/RTC Server] as streaming_server
    [CDN] as cdn

    streaming_server --> cdn
}

package "Pull Side (Viewer)" {
    [Mobile App] as player_app
    [V2TXLivePlayer] as player_sdk
    [Display/Speaker] as output_devices

    player_app --> player_sdk
    player_sdk --> output_devices
}

' Connections
pusher_sdk --> streaming_server : Push Stream
cdn --> player_sdk : Pull Stream

note top of streaming_server : Protocols:\nRTMP (2-5s latency)\nRTC (<1s latency)\nHLS (5-15s latency)
@enduml
```

## Key Classes and Methods

### Push Stream Classes
- `V2TXLivePusher`: Main pusher interface
- `V2TXLivePusherImpl`: Implementation class
- `V2TXLivePusherObserver`: Callback interface for push events
- `V2TXLiveDef`: Constants and data structures

### Pull Stream Classes
- `V2TXLivePlayer`: Main player interface
- `V2TXLivePlayerImpl`: Implementation class
- `V2TXLivePlayerObserver`: Callback interface for player events
- `TXCloudVideoView`: Render view component

### Common Configuration
- `V2TXLiveVideoEncoderParam`: Video encoding parameters
- `V2TXLiveAudioQuality`: Audio quality settings
- `V2TXLiveMode`: Streaming protocol selection (RTMP/RTC)

## Protocol Comparison

| Protocol | Latency | Compatibility | Use Case |
|----------|---------|---------------|----------|
| RTMP     | 2-5s    | High          | Standard streaming |
| FLV      | 2-5s    | High          | Web playback |
| HLS      | 5-15s   | Highest       | CDN distribution |
| RTC      | <1s     | Medium        | Interactive streaming |

## Performance Considerations

### Push Optimization
- Use hardware encoding when available
- Optimize resolution and bitrate for network conditions
- Enable adaptive bitrate for poor network conditions
- Use appropriate buffer settings

### Pull Optimization
- Use hardware decoding when available
- Implement appropriate buffering strategy
- Handle network interruptions gracefully
- Optimize render view performance

## Error Handling

Both push and pull pipelines include comprehensive error handling:
- Network connection failures
- Codec initialization errors
- Hardware resource conflicts
- Permission denied scenarios
- Stream format incompatibilities

Error callbacks are provided through observer interfaces for application-level handling.