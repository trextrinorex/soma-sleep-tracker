# Somna Privacy Declaration (Play Store)

Somna is a fully on-device passive sleep tracker.

- No personal data is uploaded to any server by default.
- All telemetry (screen, power, app-category events) stays encrypted in a local SQLCipher database.
- Raw events older than 14 days are automatically purged.
- The app requests only the minimum permissions required for passive observation.
- Usage Stats permission is used solely to classify the last foreground app category before sleep onset (entropy feature).
- Battery Stats is used for charging correlation only.
- No location, contacts, microphone, or camera access.

Users may export or delete all local data from within the future Settings screen.
