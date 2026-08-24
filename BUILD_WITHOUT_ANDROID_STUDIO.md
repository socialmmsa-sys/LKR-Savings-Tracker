# Build the APK without Android Studio

You can build this project completely online with GitHub Actions.

## Phone-only method

1. Create/sign in to a GitHub account.
2. Create a new repository.
3. Upload the contents of the `LKRSavingsTrackerAndroidPremium` folder to the repository.
   - Make sure the hidden `.github` folder is also uploaded.
4. Open the repository's **Actions** tab.
5. Choose **Build Android APK**.
6. Tap **Run workflow**.
7. After the workflow finishes, open the completed run.
8. Scroll to **Artifacts**.
9. Download **LKR-Savings-Tracker-APK**.
10. Extract the downloaded ZIP.
11. Inside it is `app-debug.apk`.
12. Open the APK on your Android phone and install it.

Android may ask you to allow **Install unknown apps** for the browser or file manager you use.

## What happens in the cloud

GitHub automatically:
- installs Java 17
- sets up Android SDK 35
- uses Gradle 8.9
- builds the native Android app
- gives you the APK as a downloadable artifact

You do not need Android Studio on your phone or computer.
