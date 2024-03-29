package com.andreapivetta.blu.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.andreapivetta.blu.R;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class Common {

    public final static String ALWAYS = "always";
    public final static String WIFI_ONLY = "wifi";

    public static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36";
    private static final String FAVORITERS_URL = "https://twitter.com/i/activity/favorited_popup?id=";
    private static final String RETWEETERS_URL = "https://twitter.com/i/activity/retweeted_popup?id=";

    public static ArrayList<Long> getFavoriters(long tweetID) throws Exception {
        return getUsers(tweetID, FAVORITERS_URL);
    }

    public static ArrayList<Long> getRetweeters(long tweetID) throws Exception {
        return getUsers(tweetID, RETWEETERS_URL);
    }

    private static ArrayList<Long> getUsers(long tweetID, String url) throws Exception {
        ArrayList<Long> usersIDs = new ArrayList<>();
        Document doc = Jsoup.parse(getJson(tweetID, url).getString("htmlUsers"));

        if (doc != null) {
            for (Element element : doc.getElementsByTag("img")) {
                try {
                    if (!element.attr("data-user-id").equals(""))
                        usersIDs.add(Long.parseLong(element.attr("data-user-id")));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return usersIDs;
        }

        return null;
    }

    private static JSONObject getJson(long tweetId, String url) {
        try {
            URL obj = new URL(url + tweetId);

            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
            connection.setRequestProperty("Content-Type", "text/html");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("user-agent", USER_AGENT);
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");

            connection.disconnect();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getResourceColorPrimary(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_themes), "B")) {
            case "B":
                return ContextCompat.getColor(context, R.color.blueThemeColorPrimary);
            case "P":
                return ContextCompat.getColor(context, R.color.pinkThemeColorPrimary);
            case "G":
                return ContextCompat.getColor(context, R.color.greenThemeColorPrimary);
            case "D":
                return ContextCompat.getColor(context, R.color.darkThemeColorPrimary);
            default:
                return ContextCompat.getColor(context, R.color.blueThemeColorPrimary);
        }
    }

}
