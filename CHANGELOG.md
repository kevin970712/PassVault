## [0.6.0-beta2] - 2025-11-18

### Refactor

- Replace SetPinDialog with SetPinFragment for a full-screen UI
- Improve Password Generator dialog and SharedPreferences usage
- Improve password dialogs and replace AlertDialog

## [0.6.0-beta] - 2025-11-17

### Features

- Add changelog and git-cliff config
- Update dependencies
- Enable R8 full mode and add dependency analysis plugin
- Add BackupWorker for automatic data backup
- Add Toolbar to Settings screen
- Encrypt automatic backups
- Add backup management screen
- Implement backup file sharing and deletion
- Add backup deletion and refactor BackupWorker
- Set entry title in Toolbar and update layout
- Add automatic app lock on inactivity
- Require password for import/export operations
- Enable ProGuard and add new dependencies
- Add changelog and git-cliff config
- Introduce automatic backups, security enhancements, and UI improvements

### Other

- Convert launcher icons to WebP and remove adaptive icon XML
- Remove unused resources
- Use withText instead of withId for button selection in tests
- Introduce repositories for data abstraction
- Use PreferenceRepository for settings
- Set correct title for AboutFragment
- Correct ActionBar title in AboutFragment
- Move PVAdapter to a new package
- Adjust padding and add dividers to entry view
- Remove toast messages from backup file copy operation
- Use `onSupportNavigateUp` for back navigation
- Include time in formatted date string
- Lazily initialize repositories in Application class
- Use `getPasswordForAutoBackups` in `BackupWorker`
- Prevent NullPointerException in BackupAdapter and disable androidx-startup

### Refactor

- Remove unused TestWorkerFactory

## [0.5.0] - 2025-11-15

### Features

- *(security)* Disable automatic backup and data extraction
- Introduce multiple enhancements and refactors

### Other

- Add Toolbar and Up Navigation to Add/Edit screen
- Simplify ViewEntryActivity and layout

## [0.4-beta] - 2025-11-08

### Features

- Add new features, improvements, and bug fixes for v5

### Other

- Add database size utility functions
- Rename APK artifact and update version

## [0.4-alpha] - 2025-11-07

### Other

- Rename "Passkey" to "Password" for clarity

## [0.3-alpha] - 2025-11-06

### Features

- Implement encrypted data import/export
- Add backup/restore and settings screen

### Other

- Implement FAB speed dial in ViewEntryActivity
- Add delete functionality and confirmation dialog
- Correct FAB layout and add content descriptions
- Correct FAB layout and add content descriptions

## [0.2-alpha] - 2025-11-04

### Other

- Rename AddEditActivity to AddActivity and move package
- Rename `AddActivity` to `AddEditActivity` and move files
- Implement comprehensive testing and improve Add/Edit functionality
- Update variable and preference key names for clarity

## [0.1-alpha] - 2025-11-02
