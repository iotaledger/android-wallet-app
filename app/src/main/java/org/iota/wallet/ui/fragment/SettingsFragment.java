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

package org.iota.wallet.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.iota.wallet.R;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String PREFERENCE_SCREEN_NODE = "preference_screen_node";
    private static final String PREFERENCE_SCREEN_PASSWORD = "preference_screen_password_protection";
    private static final String PREFERENCE_SCREEN_MISC = "preference_screen_misc";
    private static final String[] ALL_PREFERENCES = {PREFERENCE_SCREEN_NODE,
            PREFERENCE_SCREEN_PASSWORD, PREFERENCE_SCREEN_MISC};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);

        for (String preference : ALL_PREFERENCES) {
            findPreference(preference)
                    .setOnPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment fragment = null;
        switch (preference.getKey()) {
            case PREFERENCE_SCREEN_NODE:
                fragment = new SettingsNodeFragment();
                break;
            case PREFERENCE_SCREEN_PASSWORD:
                fragment = new SettingsPasswordFragment();
                break;
            case PREFERENCE_SCREEN_MISC:
                fragment = new SettingsMiscFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                            R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }
}