
This is sample app to fetch data from twitter and display it.
This is slightly different from my original attempt almost 2 years ago.
The original APP I had created as interview take-home question...

This version still uses ListView, I will create branch that uses recycleView.

If you want to play with this app yourself  you will need to edit the
app/src/main/res/values/strings.xml and provide various tokens & secrets for Twitter Oauth2.

You will find my original attempt here --

https://github.com/snijsure/TwitterApiSample.git

* Yes I know, I could have just branched from the above repo my gradel related changes etc. I just wanted
  to leave that old code behind. Hopefully I will get more time to work on this code going forward. May
  be try different twitter library etc.

* Changes
  ** Dec 31, 2015
     Changed layout to use recyclerviewer.
     Added support for swipe to delete
     Implemented infinite scroll


*Todo
   * Show geolocation on where tweet was generated.
   * Sort tweets by geolocation, show nearest to you on top.
