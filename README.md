# AkashPay Ecosystem

Public source distribution for the AkashPay native Android SMS reader, Cloudflare Worker/D1 backend, canonical Pages dashboard, and rebuild documentation.

## Security

This repository intentionally contains **no Telegram bot token, Cloudflare API token, Firebase service-account JSON, admin token, private key, or signing keystore**. Configure deployment secrets through Wrangler secrets or your CI secret store. The deploy example contains only public routing values.

## Components

- `android/`: Pure-native Android application. WebView is not included. Only bKash and Nagad payment confirmation messages are accepted.
- `cloudflare-worker/`: Cloudflare Worker and D1-backed API, Telegram webhook, device registration, gateway-number settings, SMS ingestion, and admin routes.
- `cloudflare-pages/`: Canonical dashboard source.
- `docs/`: Android, Sketchware, and integration guidance.
- `releases/`: Latest verified signed APK.

## Build

```bash
cd android
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleRelease
```

Never commit `local.properties`, release keystores, or deployment secrets.
