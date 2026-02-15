# طرح اپ آفلاین نقشه + BTS (محدود به محله)

## 1) پاسخ کوتاه به سوال‌های فنی شما

- **آیا اندروید می‌تواند بفهمد GPS معتبر است یا نه؟**
  - «قطعاً معتبر/نامعتبر» را تضمینی نمی‌دهد، ولی با ترکیب چند شاخص می‌توانید تشخیص تقریبی خوب بگیرید: `isMockProvider`، `accuracy`، جهش غیرواقعی سرعت، ناسازگاری با Cell-ID محلی، و اگر خواستید NMEA sanity check.
- **آیا همزمان بیش از یک BTS دیده می‌شود؟**
  - بله. موبایل معمولاً علاوه بر سلول سروینگ، سلول‌های همسایه را هم گزارش می‌کند (`allCellInfo`).
- **قبل از ping-pong می‌توان فهمید کاربر در محدوده چند BTS است؟**
  - بله. با لیست سلول‌های قابل مشاهده + RSSI/RSRP می‌توانید احتمال ping-pong را تخمین بزنید (وقتی دو سلول قدرت نزدیک دارند).
- **با RSRP دو BTS می‌شود موقعیت را تخمین زد؟**
  - بله، به‌صورت تقریبی. چون محیط شهری چندمسیره است، بهتر است از مدل وزن‌دهی و کالیبراسیون محلی استفاده کنید، نه فاصله دقیق هندسی.

## 2) معماری پیشنهادی

```text
+-------------------------- UI (Compose + MapView) ---------------------------+
| OfflineMapScreen                                                           |
| - نمایش نقشه وکتور آفلاین (.map)                                           |
| - نمایش وضعیت GPS معتبر/نامعتبر                                             |
| - نمایش سلول‌های دیده‌شده و تخمین موقعیت BTS                               |
+--------------------------|----------------------|----------------------------+
                           |                      |
                           v                      v
                 +----------------+      +---------------------+
                 | LocationValidator |    | CellScanner        |
                 | - mock/accuracy   |    | - TelephonyManager |
                 | - speed jump      |    | - allCellInfo      |
                 +--------|----------+    +----------|---------+
                          |                        
                          v                         v
                   +---------------------------------------+
                   | PositionEstimator                     |
                   | - نگاشت CellId -> Tower (DB محلی)     |
                   | - وزن‌دهی با RSRP                     |
                   +----------------|----------------------+
                                    v
                          +----------------------+
                          | LocalCellDatabase    |
                          | (۴ BTS محله شما)     |
                          +----------------------+
```

## 3) نقشه آفلاین وکتور ایران/محله

### گزینه سبک (پیشنهادی برای پروژه شما)
1. خروجی OSM محدوده محله را بگیرید (extract کوچک).
2. با ابزار Mapsforge map-writer فایل `iran-neighborhood.map` بسازید.
3. فایل `.map` را داخل `filesDir` یا `assets/` قرار دهید.
4. در اپ با Mapsforge رندر کنید (بدون اینترنت).

> چون شما فقط محله را می‌خواهید، سایز فایل خیلی کم می‌شود.

## 4) جایگذاری کدها در پروژه

- `app/src/main/AndroidManifest.xml`
  - مجوزهای Location و Phone state.
- `app/src/main/java/com/example/offlinebts/MainActivity.kt`
  - درخواست Runtime Permission و wire کردن ماژول‌ها.
- `app/src/main/java/com/example/offlinebts/data/cell/LocalCellDatabase.kt`
  - دیتابیس محلی ۴ BTS (CellId, ENODEB, Lat/Lon).
- `app/src/main/java/com/example/offlinebts/data/telephony/CellScanner.kt`
  - خواندن سلول سروینگ و همسایه (2G/3G/4G).
- `app/src/main/java/com/example/offlinebts/data/location/LocationValidator.kt`
  - اعتبارسنجی GPS مشکوک.
- `app/src/main/java/com/example/offlinebts/domain/PositionEstimator.kt`
  - تخمین موقعیت از RSRP.
- `app/src/main/java/com/example/offlinebts/ui/map/OfflineMapScreen.kt`
  - UI نقشه و وضعیت شبکه.

## 5) سناریوی ضد GPS Jam/Spoof

اگر GPS مشکوک بود:
1. لوکیشن را «Untrusted» علامت بزن.
2. fallback به تخمین Cell-based انجام بده.
3. اگر همزمان GPS و Cell اختلاف شدید داشتند، از کاربر تایید بگیر یا confidence پایین نمایش بده.

## 6) نکته مهم رادیویی

- RSRP به تنهایی برای فاصله دقیق کافی نیست.
- برای دقت بهتر:
  - کالیبراسیون میدانی در چند نقطه محله انجام بده.
  - برای هر BTS پارامتر path-loss محلی بساز.
  - فیلتر زمانی (Kalman/EMA) برای نرم کردن ping-pong بگذار.

## 7) چطور Build و Run کنم؟

### روش ۱: Android Studio (ساده‌ترین)
1. Android Studio (نسخه جدید) را نصب کن.
2. پروژه `android-offline-bts` را باز کن.
3. از `SDK Manager` این‌ها را نصب کن:
   - Android SDK Platform 34
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
4. JDK پروژه را روی **17** بگذار (Project Structure → Gradle JDK).
5. یک emulator بساز یا گوشی واقعی وصل کن (USB debugging).
6. Run بزن.

### روش ۲: خط فرمان (CLI)

#### پیش‌نیازها
- Java 17
- Android SDK (با `platform-tools`, `platforms;android-34`, `build-tools`)
- متغیرهای محیطی:
  - `ANDROID_HOME` یا `ANDROID_SDK_ROOT`
  - `JAVA_HOME`

#### دستورات
```bash
cd android-offline-bts
export JAVA_HOME=/path/to/jdk17
export ANDROID_SDK_ROOT=/path/to/Android/Sdk

# برای دیدن تسک‌ها
gradle tasks

# ساخت دیباگ APK
gradle :app:assembleDebug

# نصب روی دستگاه متصل
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### فایل نقشه آفلاین را کجا بگذارم؟
کد فعلی فایل را از مسیر زیر می‌خواند:
- `context.filesDir/iran-neighborhood.map`

پس باید فایل `iran-neighborhood.map` را قبل از اجرا به `filesDir` اپ کپی کنی (یا کد را طوری تغییر بدهی که از `assets/` لود کند).

### اگر Build خطا داد
- اگر خطای Java دیدی: نسخه JDK را روی 17 تنظیم کن.
- اگر خطای Android SDK دیدی: پکیج‌های SDK API 34 را نصب کن.
- اگر دانلود dependency مشکل داشت: VPN/Proxy یا mirror مخزن Google/Maven را بررسی کن.
