# Blum

Blum wants to be a simple Android Twitter Client, with a clean UI and without thousands of unnecessary options.
Available on [Google Play](https://play.google.com/store/apps/details?id=com.andreapivetta.blu).

### How to run the app

In order to test the application, you need to [setup a new twitter app](https://apps.twitter.com/). Then create a new class:
```
package com.andreapivetta.blu.twitter;


public final class TwitterK {

    public static final String CALLBACK_URL = "callback url";
    public static final String TWITTER_CONSUMER_KEY = "consumer key";
    public static final String TWITTER_CONSUMER_SECRET = "consumer secret";

}
```