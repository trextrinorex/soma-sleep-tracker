# Somna — Zero-Wearable Passive Sleep Engine

**Production-grade Android application** for passive, on-device sleep tracking without wearables.

## Overview
Somna infers sleep sessions entirely from device telemetry (screen on/off, power events, app usage categories) using a multi-variate heuristic engine with sigmoid confidence scoring, micro-awakening absorption, Bayesian personalization, and robust OEM resilience.

- **Target**: Android 10+ (API 29–35)
- **Architecture**: Clean Architecture + MVI (Unidirectional Data Flow)
- **UI**: Jetpack Compose + Circadian OLED Design System
- **Persistence**: Room + SQLCipher (encrypted)
- **Background**: Sticky Guardian Service + WorkManager
- **Battery target**: < 1.2% daily drain
- **Privacy**: Fully on-device, no cloud required for core inference

## Key Features (All 7 Phases)
1. Project Architecture, Manifest, Permissions & Signal Harvesting
2. Algorithmic Inference Engine (sigmoid + 5 feature vectors + micro-awakening merge)
3. Background Automation, WorkManager & Guardian Service
4. Presentation Layer (Compose UI/UX)
5. Adaptive Personalization (Bayesian / Welford + workday clustering + social jetlag)
6. Edge Cases, OEM Killers, Reboot Stitching & Time Drift Guards
7. Automated Testing, Battery QA Protocol & GitHub Actions CI/CD

## Building
```bash
./gradlew assembleDebug
```

## Credits
Implementation follows the complete Product Development Specification & Implementation Blueprint (PDS) Phases 1–7.

**Repository**: https://github.com/trextrinorex/soma-sleep-tracker
