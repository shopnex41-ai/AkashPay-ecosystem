# AkashPay v1.1.0 — Pure-Native Android Rebuild

## Architecture

The WebView and JavaScript bridge have been removed from the Android application. The app now renders its dashboard natively with Android views and calls the Cloudflare Worker over HTTPS.

- **Native dashboard:** `MainActivity.java`
- **Cloudflare API:** `https://akashpay-bot.axura.workers.dev`
- **SMS receiver:** `SmsReceiver.java`
- **Background retry and connection status:** `MyBackgroundService.java`
- **Notifications:** `Functions.java`
- **Storage:** Cloudflare D1 through the Worker; no Firebase library or Firebase credential is used by Android.

## Native features

- Animated AkashPay header and live Cloudflare connection state.
- Client ID and masked API-key card.
- Pulsing **Sync now** button.
- Native **Gateway numbers**, **SMS logs**, and **Orders** buttons.
- Native dialogs for bKash/Nagad number editing.
- Native accepted-SMS summary and local notification events.
- Background SMS retry queue and Telegram-link status polling remain active.
- Strict bKash/Nagad-only SMS filtering is unchanged.

## Build

1. Open the extracted `Android_Source` project in Android Studio or import the Java/XML files into Sketchware v7.0.0.
2. Keep package `com.payment.com` and app label `AkashPay`.
3. Use Android SDK 34 and Java 8 source compatibility.
4. Do not add Firebase libraries, `google-services.json`, or a WebView activity.
5. Keep the Worker URL in `MainActivity.java`, `LoginActivity.java`, `SmsReceiver.java`, and `MyBackgroundService.java` aligned with `https://akashpay-bot.axura.workers.dev`.
6. Build a release APK and sign it with your own release key.

## User flow

1. Install the APK and grant SMS, notifications, phone, and background permissions.
2. Tap **Connect AkashPay Device** on the connection screen.
3. The app stores `user_email`, `device_key`, and `device_ip` in private SharedPreferences.
4. The native dashboard loads Cloudflare status and the current SMS-log count.
5. Use **Gateway numbers** to save the bKash and Nagad merchant numbers.
6. Use **SMS logs** and **Orders** to inspect server data without leaving the app.

## API examples

```http
GET https://akashpay-bot.axura.workers.dev/v1/health

GET https://akashpay-bot.axura.workers.dev/v1/public/logs/{clientId}
X-API-Key: {apiKey}
X-Device-ID: {androidId}
```

The app must never send Firebase Admin credentials or Telegram bot credentials.

## Verification

The v1.1.0 APK was compiled successfully, verified with APK Signature Scheme v2 and v3, and inspected to confirm the removed WebView activity and bridge are not present.
