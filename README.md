
# react-native-android-pdf-renderer2

## Getting started

`$ npm install react-native-android-pdf-renderer2 --save`

### Mostly automatic installation

`$ react-native link react-native-android-pdf-renderer2`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-android-pdf-renderer2` and add `RNAndroidPdfRenderer2.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAndroidPdfRenderer2.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import au.com.intellihealth.android.androidpdfrenderer.RNAndroidPdfRenderer2Package;` to the imports at the top of the file
  - Add `new RNAndroidPdfRenderer2Package()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-android-pdf-renderer2'
  	project(':react-native-android-pdf-renderer2').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-android-pdf-renderer2/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-android-pdf-renderer2')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNAndroidPdfRenderer2.sln` in `node_modules/react-native-android-pdf-renderer2/windows/RNAndroidPdfRenderer2.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Android.Pdf.Renderer2.RNAndroidPdfRenderer2;` to the usings at the top of the file
  - Add `new RNAndroidPdfRenderer2Package()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNAndroidPdfRenderer2 from 'react-native-android-pdf-renderer2';

// TODO: What to do with the module?
RNAndroidPdfRenderer2;
```
  