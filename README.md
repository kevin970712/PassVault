<h1 align="center">
  <br>
    <img src="./metadata/en-US/images/icon.png" alt="PassVault icon" width="150" />
  <br>
  PassVault
  <br>
</h1>

<h4 align="center">A secure, lightweight, and offline password manager for Android built with Kotlin.</h4>

<h4 align="center">
<img alt="GitHub Downloads (all assets, all releases)" src="https://img.shields.io/github/downloads/jksalcedo/PassVault/total?logo=GitHub">
<img alt="SourceForge Downloads" src="https://img.shields.io/sourceforge/dt/passvault-app?logo=SourceForge">
<img alt="GitHub License" src="https://img.shields.io/github/license/jksalcedo/PassVault">
<img alt="GitHub Release" src="https://img.shields.io/github/v/release/jksalcedo/PassVault?include_prereleases">
<img src="https://img.shields.io/badge/Kotlin-2.2.21-7f52ff?logo=kotlin&logoColor=white" alt="Kotlin Version">
  <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/jksalcedo/PassVault?logo=Android">

</h4>

<h1 align="center">
    <img src="./metadata/en-US/images/phoneScreenshots/1.jpg" alt="PassVault Screenshot" width="250" />
  <img src="./metadata/en-US/images/phoneScreenshots/2.jpg" alt="PassVault Screenshot" width="250" />
  <img src="./metadata/en-US/images/phoneScreenshots/3.jpg" alt="PassVault Screenshot" width="250" />
    <img src="./metadata/en-US/images/phoneScreenshots/6.jpg" alt="PassVault Screenshot" width="250" />
    <img src="./metadata/en-US/images/phoneScreenshots/4.jpg" alt="PassVault Screenshot" width="250" />
  <img src="./metadata/en-US/images/phoneScreenshots/5.jpg" alt="PassVault Screenshot" width="250" />
  <br>
  
  <br>
</h1>

PassVault is a lightweight password manager that allows users to securely store credentials on their
device. All data is stored locally and sensitive information is encrypted using the Android Keystore
system. The application does not require internet permissions. User data never leaves the device.

## Core Features

- [x] Secure PIN Authentication
- [x] Biometric (Fingerprint) Login
- [x] Encrypted Database (AES-256)
- [x] Add, View, Edit, & Delete Passwords
- [x] In-app Password Generator
- [X] Encrypted Import/Export (for backups and transfer)
- [X] Encrypted Automatic Backups

## Planned Features

- [ ] Secure Notes (for storing non-password secrets)
- [ ] Desktop Version
- [ ] Categories/Labels
- [ ] Import from KeePass/Bitwarden

## Download

[![Get it on GitHub](https://img.shields.io/badge/Get_it_on-GitHub-24292e?style=for-the-badge&logo=github&logoColor=white)](https://github.com/jksalcedo/PassVault/releases) 
<br>

[![Download](https://img.shields.io/badge/Download-SourceForge-brightgreen?style=for-the-badge&logo=sourceforge&logoColor=white)](https://sourceforge.net/projects/passvault-app/files/latest/download)
<br>

[![F-Droid](https://img.shields.io/badge/F--Droid-Available_on_F--Droid-3465a4?style=for-the-badge&logo=f-droid&logoColor=white)](https://f-droid.org/packages/com.jksalcedo.passvault/)



## Tech Stack & Architecture

This project follows the MVVM (Model-View-ViewModel) architecture.

- **Language**: Kotlin
- **UI**: XML Layouts with ViewBinding and Material Design Components
- **Database**: Room Persistence Library
- **Architecture**: ViewModel, Repository, LiveData
- **Security**: Android Keystore, AES/CBC/PKCS7 Encryption
