# QuickPermissions-Kotlin [![Release](https://jitpack.io/v/cyb3rko/QuickPermissions-Kotlin.svg)](https://jitpack.io/#cyb3rko/QuickPermissions-Kotlin)

The easiest way to handle Android Runtime Permissions in Kotlin.

![Example](/media/example.png)

* [Inspiration](#inspiration)
* [Add it to your app](#add-it-to-your-app)
* [How to do it?](#how-to-do-it)
  * [Let the library do all the hard stuff](#let-the-library-do-all-the-hard-stuff)
  * [Advanced](#advanced)
* [Summary](#summary)

## Inspiration

Android runtime permissions is pain. 

Android runtime permissions was introduced in the Marshmallow (v 6.0) version of Android. It asks for permissions to user when they are running the app instead of asking for all the permissions while installing the app. It gives more control to users as they can give the permissions they want and deny to those who they do not fill comfortable with. 

With this, it has increased the pain in the neck for the developer, to enable/disable features based on what permissions user has granted or denied. The model asking for permissions was design to function asynchronously, which increased the complexity of an app largely. 

To make it with that, google has created it's own library [easypermissions](https://github.com/googlesamples/easypermissions). (*I didn't find it easy, but that's what they said*). Still, you have to do handful of things and it's asynchronous way of handling things make it hard to manage. There are many other libraries as well to help developers easily handle it. **But,** all libraries has to manage it with proxy classes or managing and passing callbacks and all. 

So, to solve this issue [QuickPermissions](https://github.com/QuickPermissions/QuickPermissions) was created. But, it turns out that, it doesn't work with instant-run and lots of developers can not afford to disable instant run, because it dramatically increased the build time. So, if you are working with Kotlin, you are lucky. This library is created to solve that problem in Kotlin. No gradle plugin and no long running build times. Asking for permissions synchronous way (*It looks like that, but won't block the main thread, don't worry, no ANRs, promise!*). And after the permissions are granted, it will continue executing the method block which was on hold. As simple as that.

## Add it to your app

In your `settings.gradle` file (or wherever you've set up repositories):

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io/" }  // <-- THIS MUST BE ADDED
    }
}
```

As this library is using jitpack to publish this, you need to add jitpack url. If you already have, do not add that again.

In your **app**'s `build.gradle` file, add the following dependency: 

```groovy
dependencies {
   implementation 'com.github.cyb3rko:quickpermissions-kotlin:VERSION'
}
```

## How to use it?

The following call has to be made in an `Activity` or `Fragment` context: 

Use `runWithPermissions` block, and you are all done. Library will manage everything, will ask for permissions, will show rationale dialog if denied and also ask to user to allow permissions from settings if user has permanently denied some permission(s) required by the method.

```kotlin
fun methodWithPermissions() = runWithPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) {
    Toast.makeText(this, "Camera and audio recording permissions granted", Toast.LENGTH_SHORT).show()
    // Do the stuff with permissions safely
}
```

That's it! You are good to go.

Your inner code will not be executed until the permission mentioned will not be granted. If it already has the permissions, it will simply be executed right away. If not, it will ask for permissions and executes it after it was granted.

**NOTE:** The one thing is to make sure, your function should not return anything (return type should be Unit), as the asking permissions is asynchronous behavior, that can not be handled in a true synchronous way.-

### Advanced

You can optionally pass on the `options` object to that method:

Things you can do with it:

* `handleRationale` : Boolean indicating whether the library should handle rationale dialog or not
* `rationaleMessage` : Custom rational message overriding default value if specified
* `handlePermanentlyDenied` : Boolean indicating whether the library should handle permissions permanently denied dialog or not
* `permanentlyDeniedMessage` : Custom permanently denied message overriding default value if specified
* `preRationaleAction` : Function to be called when permissions are missing and before starting flow
* `rationaleMethod` : Function to handle rational callback yourself. It will prvoide a `QuickPermissionsRequest` instance on which you can call `proceed()` to continue asking permissions again, or call `cancel()` to cancel the flow
* `permanentDeniedMethod` : Function to handle permissions permanently denied callback yourself. It will prvoide a `QuickPermissionsRequest` instance on which you can call `openAppSettings()` to continue or `cancel()` to cancel the flow
* `permissionsDeniedMethod` : Function to handle cases when some/all permissions are denied at the end of the flow. It will prvoide a `QuickPermissionsRequest` instance from which you can retrieve the permissions which were denied.

```kotlin
val quickPermissionsOption = QuickPermissionsOptions(
    handleRationale = false
    rationaleMessage = "Custom rational message",
    permanentlyDeniedMessage = "Custom permanently denied message",
    preRationaleAction = { showToast() },
    rationaleMethod = { req -> rationaleCallback(req) },
    permanentDeniedMethod = { req -> permissionsPermanentlyDenied(req) }
)

private fun methodRequiresPermissions() = runWithPermissions(Manifest.permission.WRITE_CALENDAR, Manifest.permission.RECORD_AUDIO, options = quickPermissionsOption) {
    Toast.makeText(this, "Cal and microphone permissions granted", Toast.LENGTH_LONG).show()
}
```

## Sample

There is an sample `app` available in this repo. Just clone it and run `app` to check it out.

----

Have any questions or any trouble? Create an issue.

## License

    Copyright 2023-2025, Cyb3rKo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
