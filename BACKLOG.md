# VolunteerHours — Backlog v2 ✅ מאושר
> עודכן: 20/06/2026 | כל השאלות נענו

---

## 🐛 באגים

- [ ] **B1** — טקסט טאב נבחר בלתי נראה (blue pill, no text) → הוספת `color` מפורש ל-Text

---

## 🎨 UI

- [ ] **U1** — VolunteerDetailScreen עיצוב מחדש
  - Header עם circle avatar (אותיות ראשונות, צבע נגזר מהשם)
  - שם גדול + טלפון מתחת
  - stats cards קומפקטיות
  - היררכיה ברורה, מסודר מלמעלה, חיות

- [ ] **U2** — כותרות מסכים: אם אפשר קטנות ויפות → כן, אם לא → מוסרות
  ↳ **החלטה**: `titleMedium` + `onSurfaceVariant` במקום `headlineMedium Bold`

---

## ✨ פיצ'רים

- [ ] **F1** — AddEntrySheet ריסטרקט לשני מצבים ברורים:
  ```
  [● קיים] [○ חדש]    ← chip row בראש
  
  מצב "קיים": כפתור [בחר מתנדב ▾] → פותח AlertDialog עם רשימה
  מצב "חדש":  שדה שם חופשי → שמירה אוטומטית ברשימה (ללא toggle)
  ```
  - אם הרשימה ריקה → קופץ ישר למצב "חדש"
  - אין autocomplete (מפורש על ידי המשתמש)

- [ ] **F2** — שדה טלפון בהוספת רשומה, רק במצב "חדש" + אופציונלי

- [ ] **F3** — פורמט שעות: **מוסיפים** אפשרות "משעה לשעה" (לא מחליפים):
  ```
  FilterChips: [שעות] [משעה לשעה]
  
  משעה לשעה: [09:00] → [12:30] = 3.5 שע׳ (חישוב אוטומטי)
  ```

- [ ] **F4** — Onboarding: **החלטה שלי** = חד-פעמי (DataStore key), מוצג ב-install ראשון
  - BottomSheet עם הסבר קצר + כפתור "הוסף רשומה ראשונה"

- [ ] **F5** — "מתנדב חדש" FAB מוסר מבית → נשאר רק בעמוד מתנדבים

---

## 📋 סדר ביצוע

| # | משימה | קובץ |
|---|---|---|
| 1 | B1 טקסט טאב | GlassComponents.kt |
| 2 | U2 כותרות | Dashboard + About |
| 3 | F5 הסרת FAB מבית | DashboardScreen.kt |
| 4 | F1+F2+F3 AddEntrySheet | AddEntrySheet.kt |
| 5 | U1 VolunteerDetail | VolunteerDetailScreen.kt |
| 6 | F4 Onboarding | SettingsRepository + OnboardingSheet + HomeScreen |
| 7 | ViewModel עדכון | VolunteerViewModel.kt |
| 8 | Build + APK | — |
