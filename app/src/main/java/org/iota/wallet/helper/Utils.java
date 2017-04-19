/*
 * Copyright (C) 2017 IOTA Foundation
 *
 * Authors: pinpong, adrianziser, saschan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.iota.wallet.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.gson.Gson;

import org.knowm.xchange.currency.Currency;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner;


/**
 * This class provides some utility method used across the app
 */
public class Utils {

    public static void fixListView(final ListView lv, final SwipeRefreshLayout swipeLayout) {
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lv == null || lv.getChildCount() == 0) ? 0 : lv.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    /**
     * @return the currency of the wallet
     */
    public static Currency getBaseCurrency() {
        // TODO: replace with when iota is available at poloniex
        // return new Currency("IOTA"));
        return new Currency("XMR");
    }

    public static Currency getConfiguredAlternateCurrency(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Currency(prefs.getString(Constants.PREFERENCE_WALLET_VALUE_CURRENCY, "BTC"));
    }

    public static String timeStampToDate(long timestamp) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp * 1000);
        return df.format(date);
    }

    public static <T> List<T> getCachedList(Context context, Class<T> clazz, String prefs) {
        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(prefs, "");
        String encSeed = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFERENCE_ENC_SEED, "");

        Type type = newParameterizedTypeWithOwner(null, ArrayList.class, clazz);
        List<T> list = new ArrayList<>();
        if (!string.isEmpty() && !encSeed.isEmpty()) {
            list = (new Gson()).fromJson(string, type);
        }
        return list;
    }

    public static void setCachedList(Context context, String prefs, List list) {
        String encSeed = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFERENCE_ENC_SEED, "");

        if (!encSeed.isEmpty())
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(prefs, (new Gson()).toJson(list)).apply();
    }

    public static File getExternalIotaDirectory(Context context) {
        File previewDir = new File(context.getExternalFilesDir(null), "iota");
        if (!previewDir.exists() && previewDir.mkdir())
            return previewDir;
        else return null;
    }

    public static int createNewID(){
        Date now = new Date();
        return Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
    }
}

