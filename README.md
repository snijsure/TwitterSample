# About
This is a sample app to fetch data from twitter and display it.
Code searches for tweets with hash-tag #travel

* There are several features you will find in this application
  * This application uses RxJava to handle all network activity.
  * You will notice that class RowItem extends Parcelable and it is used
    to save restore data on orientation change.
  * You will see how implement fetch more data, upon swipe down.
  * You will see how to implement fetch more data, when scrollView hits the bottom.
  * More importantly how to get these two actions to work together.
  * This sample also has an automated unit test that you can run to drive the application
      * You can run command ./gradlew connectedDebugAndroidTest to run this test.
      * The unit test uses espresso.
      * The unit test tests application in landscape and portrait mode.

If you want to play with this app yourself  you will need to edit the
app/src/main/res/values/strings.xml and provide various tokens & secrets for Twitter Oauth2.

![Application Image](https://github.com/snijsure/TwitterApiSampleV2/blob/master/app-capture.png)
![Login Screen](https://github.com/snijsure/TwitterApiSampleV2/blob/master/login-screen.png)
