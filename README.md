# IVT Take 5

Native Android app for In Vitro Technologies technicians to complete a Take 5 / JHA before starting work.

## Included

- Job, site and sterile-services equipment details
- Hospital/CSSD hazard and control checklists
- Readiness questions
- Site/hazard photos
- GPS coordinates and automatic timestamps
- Finger signature
- Branded PDF report
- Email hand-off to Outlook/Gmail with the PDF and photos attached
- Completed Take 5 history stored on the device

## Configure the monitored inbox

Set `MONITORED_INBOX` in:

`app/src/main/java/au/com/invitro/take5/AppConfig.kt`

It is currently set to `david.bailey@invitro.com.au`.

## Build

Open this folder in Android Studio, or run:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

The installable development APK is written to:

`app/build/outputs/apk/debug/Take5-app-debug.apk`

## Sending

Completing an assessment saves it on the device, generates the PDF, and opens the technician's email app with the monitored inbox, subject, report summary, PDF and photos filled in. The technician must tap **Send** in Outlook/Gmail.

The history shows that an email was *prepared*; Android email apps do not report whether the user actually sent it.

## Privacy

Take 5s, signatures and photos are held in app-private device storage and excluded from Android cloud backup/device transfer. Deleting the app removes the local history. Copies already sent by email are unaffected.
