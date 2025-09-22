# 🔐 PassVault – Project Overview

## 📌 Project Description

**PassVault** is a lightweight **offline Android password manager** that securely stores user
credentials.
The app allows users to add, view, edit, and delete password entries while ensuring that sensitive
data (passwords) is **encrypted using Android Keystore + AES encryption**.

PassVault is designed with simplicity and privacy in mind:

* No internet permissions (all data stays on device).
* Secure storage via Room database with encrypted password fields.
* PIN or biometric authentication to access the vault.

---

## 🎯 Key Features

1. **Unlock Screen**

    * Authenticate with **PIN** or **BiometricPrompt**.
    * Prevent unauthorized access.

2. **Main Vault (Password List)**

    * Displays all saved entries (title, username, last updated date).
    * Floating Action Button (FAB) to quickly add new entries.

3. **Add/Edit Password Entry**

    * Save credentials: `title`, `username`, `password`, `notes`.
    * Built-in **password generator**.
    * Automatically timestamps entries (`createdAt`, `updatedAt`).

4. **View Password Entry**

    * Show entry details.
    * Password hidden by default (`•••••`).
    * Option to **reveal** (after authentication) or **copy** to clipboard.

5. **Security**

    * Passwords stored as **cipher text** with unique IVs.
    * Encryption key stored in **Android Keystore** (never exposed to app or database).
    * Database = Room (`PasswordEntry` entity).

---

## 🏗 Tech Stack

* **Language**: Kotlin
* **UI**: XML Layout + ViewBinding (Material Components)
* **Database**: Room Persistence Library
* **Architecture**: MVVM (ViewModel + Repository + LiveData)
* **Security**: Android Keystore + AES/CBC/PKCS7 encryption
* **Other**: RecyclerView, CardView, BiometricPrompt

---

## 📂 Project Structure

```
com.example.passvault
│
├── data
│   ├── PasswordEntry.kt        # Entity
│   ├── PasswordDao.kt          # DAO
│   ├── PassVaultDatabase.kt    # Room Database
│   ├── PasswordRepository.kt   # Repository
│
├── ui
│   ├── UnlockActivity.kt       # PIN/Biometric unlock
│   ├── MainActivity.kt         # Entry list (RecyclerView + FAB)
│   ├── AddEditActivity.kt      # Add/Edit form
│   ├── ViewEntryActivity.kt    # Show details + reveal/copy
│   ├── adapter
│   │   └── PasswordAdapter.kt  # RecyclerView Adapter
│
├── viewmodel
│   └── PasswordViewModel.kt    # ViewModel for DB ops
│
├── util
│   └── CryptoHelper.kt         # AES/Keystore utils
```

---

## 🚀 Future Enhancements

* Export/Import encrypted backup.
* Password strength meter.
* Tagging & search for entries.
* Auto-lock on inactivity.
* Dark mode UI.

---
