# AkashPay SMS Reader - Sketchware v7.0.0 Build Guide

এই গাইডটি অনুসরণ করে আপনি স্কেচওয়্যার (Sketchware) ব্যবহার করে আকাশপে (AkashPay) এসএমএস রিডার অ্যাপটি তৈরি করতে পারবেন।

---

## ১. প্রজেক্ট সেটআপ (Project Setup)

১. **New Project:** স্কেচওয়্যারে একটি নতুন প্রজেক্ট খুলুন এবং নাম দিন `AkashPay Gateway`।
২. **Package Name:** `com.akashpay.gateway` সেট করুন।
৩. **Library:** `Appcompat and Design` এবং `Firebase` লাইব্রেরি এনাবল করুন।

---

## ২. কম্পোনেন্ট যোগ করা (Components)

নিচের কম্পোনেন্টগুলো যোগ করুন:
*   **RequestNetwork:** নাম দিন `network` (এপিআই কলের জন্য)।
*   **SharedPreferences:** নাম দিন `auth` (Client ID এবং API Key সেভ করার জন্য)।
*   **Timer:** নাম দিন `timer` (ব্যাকগ্রাউন্ড টাস্কের জন্য)।

---

## ৩. ব্রডকাস্ট রিসিভার সেটআপ (BroadcastReceiver)

এসএমএস ক্যাপচার করার জন্য একটি `BroadcastReceiver` তৈরি করতে হবে।

### A. Add Source Directly (Logic)
স্কেচওয়্যারের `ASSEMBLY` বা `Add Source Directly` ব্লকে নিচের কোডটি দিন:

```java
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                String sender = sms.getOriginatingAddress().toLowerCase();
                String body = sms.getMessageBody();
                
                // AkashPay Formula: bKash/Nagad Filter
                if (isPaymentSms(sender, body)) {
                    sendToAkashPay(context, sender, body);
                }
            }
        }
    }

    private boolean isPaymentSms(String sender, String body) {
        String content = body.toLowerCase();
        // bKash Filter
        if ((sender.contains("bkash") || sender.equals("247")) && 
            content.contains("received") && content.contains("tk")) return true;
        // Nagad Filter
        if ((sender.contains("nagad") || sender.equals("16122")) && 
            content.contains("money received")) return true;
        return false;
    }
}
```

---

## ৪. ফায়ারবেজ কানেকশন (Firebase Connection)

১. **Firebase Console:** একটি প্রজেক্ট তৈরি করুন।
২. **Google-Services.json:** স্কেচওয়্যারে আপনার Firebase API Key, Project ID এবং Database URL দিন।
৩. **Database Rules:** `Realtime Database` এর রুলস `public` বা `authenticated` রাখুন।

---

## ৫. এপিআই ইন্টিগ্রেশন (API Call)

অ্যাপ থেকে ডাটা সার্ভারে পাঠাতে `RequestNetwork` ব্যবহার করুন:
*   **Method:** `POST`
*   **URL:** `https://akashpay-api.axura.workers.dev/v1/public/logs/{clientId}`
*   **Headers:** `X-Device-ID` (আপনার ডিভাইসের ইউনিক আইডি)।

---

## ৬. ব্যাকগ্রাউন্ড সার্ভিস (Foreground Service)

অ্যাপটি বন্ধ থাকলেও যাতে কাজ করে, সেজন্য `Service` কম্পোনেন্ট ব্যবহার করুন এবং `Notification` দেখান। স্কেচওয়্যারে `Service` এর `onStartCommand` এ এসএমএস লিসেনারটি চালু রাখুন।

---
**টিপস:** অ্যাপটি ইনস্টল করার পর ফোনের **Battery Optimization** বন্ধ করতে ভুলবেন না, অন্যথায় অ্যান্ড্রয়েড সিস্টেম অ্যাপটিকে বন্ধ করে দিতে পারে।
