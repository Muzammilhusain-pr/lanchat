# LanChat — offline, same-network WhatsApp-style app (skeleton)

A chat app that works entirely over a local Wi-Fi hotspot with **no internet
connection anywhere in the loop**. One phone hosts the hotspot, everyone
else on that hotspot's Wi-Fi finds each other automatically and gets text
chat, image/video/document sharing, location sharing, and audio/video
calls — all peer-to-peer on the local network.

## How it works

**Topology:** star network. One device becomes the *host* (turns on its
Android hotspot); every other device *joins* by connecting to that Wi-Fi.

| Piece | What it does | Needs internet? |
|---|---|---|
| `network/NsdDiscoveryManager` | Host advertises itself via mDNS (NSD); clients auto-discover it — no manual IP entry | No |
| `network/LocalServer` | Runs **on the host only**: a WebSocket server (chat + call signaling) and an HTTP file server (NanoHTTPD, for images/video/docs) | No |
| `network/SignalingClient` | Runs on every device: WebSocket connection to the host, carries chat messages and WebRTC offer/answer/ICE | No |
| `call/WebRtcClient` | Audio/video calls via Google's WebRTC. Because both sides are on the same LAN, ICE resolves to local IPs directly — **no STUN/TURN server needed** | No |
| `media/FileTransferManager` | Uploads attachments to the host's HTTP server, downloads them on the receiving side | No |
| `location/LocationSharer` | Reads GPS coordinates and sends them as a message | No (GPS itself doesn't need internet) |

The only thing that genuinely can't work offline is a *visual map tile
background* behind a shared location pin (Google Maps/OSM tiles are
normally streamed) — raw coordinates + distance/bearing still work fine,
or you can bundle an offline tile pack for a specific area later.

## What's real vs. what's stubbed in this skeleton

**Wired up and structurally complete:**
- Project/module structure, Gradle setup, permissions manifest
- `Protocol.kt` — the full wire message format for every feature
- `NsdDiscoveryManager` — real NSD advertise/discover logic
- `LocalServer` — real WebSocket server + message relay; HTTP file server routes exist but upload/download bodies are TODO
- `SignalingClient` — real WebSocket client, connect/send/receive as a Flow
- `LanSession` — glues discovery + server + client + repository together
- Compose UI: Home (host/join), Chat list, Chat thread with WhatsApp-style bubbles, attach sheet (camera/gallery/video/doc/location/contact), call screen shell

**Left as `TODO` (the genuinely heavy lifting, intentionally scoped as next steps):**
- `LocalServer.handleUpload/handleDownload` — multipart file body parsing
- `FileTransferManager.upload/download` — the actual HTTP calls
- `WebRtcClient` — PeerConnection setup, local/remote tracks, SDP + ICE wiring (this is the single biggest remaining piece)
- `LocationSharer` → UI wiring for permission requests
- Room database so chat history survives app restarts (currently in-memory only)
- Turning the hotspot on programmatically isn't available to third-party apps on modern Android — the host user turns it on manually in system settings first; the app just detects/uses it

## Building it

This was scaffolded without an Android SDK available in this environment,
so **it hasn't been compiled yet** — open it in Android Studio to build:

1. Android Studio Hedgehog+ , JDK 17
2. Open this folder as a project
3. Let Gradle sync (pulls dependencies from Google/Maven Central — needs
   internet on your dev machine, not on the phones running the app)
4. Run on two physical devices (emulators can't easily share a real Wi-Fi hotspot)

### Trying it out
1. On Phone A: Settings → hotspot → turn on. Launch app → "Start as Host"
2. On Phone B: connect to Phone A's hotspot Wi-Fi. Launch app → "Join Nearby Chat"
3. They should discover each other and you can open a chat thread

## Suggested build order from here
1. Wire `LocalServer`'s upload/download + `FileTransferManager` (gets images/docs working)
2. Wire `WebRtcClient` fully (gets calls working — biggest chunk)
3. Add Room for persistent chat history
4. Polish discovery UX (show "searching…", handle host going offline, reconnect)
5. Multi-peer group chat UI (currently modeled as 1:1 threads)
