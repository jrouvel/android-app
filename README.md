Caribe Wave Android App
---
An Android app to get realtime sensor status and notifications

![](https://raw.githubusercontent.com/caribewave/android-app/master/screenshots/app1.png)
![](https://raw.githubusercontent.com/caribewave/android-app/master/screenshots/app2.png)

The safe (highest) areas are displayed in green on the map so it's easier to identify where to go in case of seismic activity.

#### Messaging

The app leverages the **Google Cloud Messaging** platform to send notifications even when the app is not running in background.

This allows us to send alert messages even when the user has not launched the app or if the user is doing something else on his phone or tablet.

![](https://raw.githubusercontent.com/caribewave/android-app/master/screenshots/app3.png)

#### Compiling and Installation

You need **Android Studio 1.5.1**, and the **API 23 SDK**. Gradle is used to keep the dependencies up to date.

The app uses the stable [Mapbox Android SDK](https://www.mapbox.com/android-sdk/) for the map, [MQTT](http://mqtt.org/) for sending and receiving the messages in realtime and the **pheromon** REST Api for the sensor list.

#### Licence

MIT.