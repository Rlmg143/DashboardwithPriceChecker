package com.example.dashboardwithpricechecker;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PrefConfig {

    public static final String LIST_KEY = "store-key";

    public static void writeListInPref(Context context, List<Item> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        System.out.println("Write: " + jsonString);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LIST_KEY, jsonString);
        editor.apply();
    }

    public static List<Item> readListFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(LIST_KEY, "");
        System.out.println("Read: " + jsonString);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Item>>() {}.getType();
        List<Item> list = gson.fromJson(jsonString, type);
        return list;
    }
}
