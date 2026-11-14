# VolunteerHours

**An Android app for logging volunteer hours: who came, on which day, for how long, and who leads the month.**

Hebrew-first (RTL), works fully offline, no account.

**Download:** the latest APK is on the [Releases page](https://github.com/HananelSabag/VolunteerHours/releases/latest) (Android 8.0+).

## What it does

- **Volunteers.** Add a volunteer with a name and an optional phone. One-off helpers can be logged without saving them to the list.
- **Hour entries.** Log a date and hours for a volunteer from a bottom sheet, with validation. Search the list while picking.
- **Dashboard.** Total hours, number of entries and volunteers, plus a **volunteer ranking** by hours.
- **Records.** Every entry, newest first. Each volunteer gets a detail screen with their own history and stats.
- **Daily reminder.** An optional 09:00 notification (WorkManager) so nobody forgets to log the day.
- **CSV export.** Share all records as a spreadsheet-ready file.
- **Appearance.** Light, dark or system theme, and a glass-style Material 3 UI.

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Data | Room (volunteers ↔ entries with a cascading foreign key, aggregated stats queries), DataStore for settings |
| Background | WorkManager (`ReminderWorker`) |
| Architecture | MVVM: `VolunteerViewModel` exposes Room flows to the screens |

```
app/src/main/java/com/hananelsabag/volunteerhours/
  data/          Room entities, DAOs, stats projections, settings repository
  viewmodel/     VolunteerViewModel
  ui/            dashboard, records, volunteer detail, add sheets, onboarding, about
  notification/  daily reminder worker
```

## Build

Open the project in Android Studio (JDK 17), or:

```bash
./gradlew assembleDebug
```

## Author

Hananel Sabag · [GitHub](https://github.com/HananelSabag) · [LinkedIn](https://linkedin.com/in/hananel-sabag)
