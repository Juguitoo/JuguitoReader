<div align="center">

![JuguitoReader](docs/icons/JuguitoReaderBanner.png)  

**English** · [Español](README.es.md)  

**Digital books and physical ones. One local app.**  
Import EPUBs, read them in the app, and log books you read on paper — dates, rating, notes, status, and session stats — without a backend.  
Built with **Kotlin, Jetpack Compose, Hilt and Room**.   

![Tests](https://github.com/Juguitoo/JuguitoReader/actions/workflows/test.yml/badge.svg)&nbsp;
![Android API 26+](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?style=flat-square&logo=android&logoColor=white)&nbsp;
![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?style=flat-square&logo=kotlin&logoColor=white)&nbsp;
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)&nbsp;
![Room](https://img.shields.io/badge/Room-2.8-FF6F00?style=flat-square&logo=android&logoColor=white)&nbsp;
![Proprietary license](https://img.shields.io/badge/License-Proprietary-red?style=flat-square)  

*Banner and app icon by Daniela.*

</div>

---



# Why this app

Most Android reading apps are EPUB viewers. I also read paper books, and I wanted one place for both.

JuguitoReader is a **local-first** library and reading log: files stay on the device, there is no account, and physical books are a first-class feature (`isPhysical`), not a hack.

Instead of adapting to a cloud reader, I built the manager I actually use.

---



# Features

- Digital library — import EPUBs, folders and genres
- Built-in reader — chapter progress, themes, zoom, session time and WPM
- Reading manager — status, dates, rating and notes for digital *and* physical books
- Registry — spreadsheet-like metadata editing
- Per-book stats — daily progress and reading sessions
- Local-first — Room + DataStore, no backend
- ES / EN — in-app language switch

---



# Screenshots

<div align="center">

<img src="docs/screenshots/home.png" alt="Home" width="180" hspace="10">&nbsp;&nbsp;
<img src="docs/screenshots/library.png" alt="Library" width="180" hspace="10">&nbsp;&nbsp;
<img src="docs/screenshots/reader.png" alt="Reader" width="180" hspace="10">&nbsp;&nbsp;
<img src="docs/screenshots/registry.png" alt="Registry" width="180" hspace="10">

Home · Library · Reader · Registry

</div>

## Demo

<div align="center">

<img src="docs/screenshots/demo.gif" alt="JuguitoReader demo" width="320">

</div>

---



# Architecture

JuguitoReader follows **Clean Architecture + MVVM**, all on-device.

```
UI (Compose + ViewModel) → UseCase → Repository → Room / DataStore
```

The EPUB viewer is a WebView with a local asset loader. Physical books skip the file and still use the same library, detail, and stats flow.

```mermaid
flowchart LR
  UI[Compose_UI] --> UC[UseCases]
  UC --> Repo[Repository]
  Repo --> Room[(Room)]
  Repo --> DS[DataStore]
```



Layers and constraints: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) (Spanish).

---



# Tech stack


| Category     | Technologies                      |
| ------------ | --------------------------------- |
| Language     | Kotlin 2.4 · JVM 11 bytecode      |
| UI           | Jetpack Compose · Material 3      |
| Architecture | Clean Architecture · MVVM · Hilt  |
| Persistence  | Room 2.8 · DataStore              |
| Async        | Coroutines · Flow                 |
| Tests        | JUnit 4 · MockK · Turbine · Truth |


Single Gradle module `:app`. minSdk 26 · targetSdk 37.

---



# Running locally

```bash
git clone https://github.com/Juguitoo/JuguitoReader.git
cd JuguitoReader
```

Open in Android Studio (AGP 9.3+, JDK 17+) and sync Gradle.

```bash
./gradlew test
```

Optional pre-push hook: `.\scripts\install-git-hooks.ps1`

Instrumented tests: `./gradlew connectedAndroidTest`

---



# Documentation

Working docs (Spanish) live in `[docs/](docs/)`: architecture, database, roadmap, backlog, known issues. `[AGENTS.md](AGENTS.md)` is for AI assistants in this repo.

---



# Project status

Pre-release. Closed testing; not on the Play Store yet.

---



# License

Copyright © 2026 Hugo. All rights reserved.

Source is published for **portfolio / reference**. You may not copy, modify, distribute, or use it in another product without written permission. See [LICENSE](LICENSE).