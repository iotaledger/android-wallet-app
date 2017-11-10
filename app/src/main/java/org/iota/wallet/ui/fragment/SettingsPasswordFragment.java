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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceManager;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.ui.dialog.ChangeSeedPasswordDialog;
import org.iota.wallet.ui.dialog.EncryptSeedDialog;
import org.iota.wallet.ui.dialog.ShowSeedDialog;

public class SettingsPasswordFragment extends PreferenceFragment {

    private static final String PREFERENCE_SHOW_SEED = "preference_show_seed";
    private static final String PREFERENCE_CHANGE_PASSWORD = "preference_change_password";
    private static final String PREFERENCE_ENCRYPT_SEED = "preference_encrypt_seed";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_password_protection);
        checkPreferencesDependencies();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case PREFERENCE_SHOW_SEED:
                ShowSeedDialog showSeedDialog = new ShowSeedDialog();
                showSeedDialog.show(getActivity().getFragmentManager(), null);
                break;
            case PREFERENCE_CHANGE_PASSWORD:
                ChangeSeedPasswordDialog changeSeedPasswordDialog = new ChangeSeedPasswordDialog();
                changeSeedPasswordDialog.show(getActivity().getFragmentManager(), null);
                break;
            case PREFERENCE_ENCRYPT_SEED:
                Bundle bundle = new Bundle();
                bundle.putString("seed", String.valueOf(IOTA.seed));
                EncryptSeedDialog encryptSeedDialog = new EncryptSeedDialog();
                encryptSeedDialog.setArguments(bundle);
                encryptSeedDialog.show(getActivity().getFragmentManager(), null);
                break;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void checkPreferencesDependencies() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        PreferenceScreen screen = getPreferenceScreen();

        Preference showSeed = findPreference(PREFERENCE_SHOW_SEED);
        Preference changePassword = findPreference(PREFERENCE_CHANGE_PASSWORD);
        Preference setPassword = findPreference(PREFERENCE_ENCRYPT_SEED);

        if (prefs.getString(Constants.PREFERENCE_ENC_SEED, "").isEmpty() && IOTA.seed == null) {
            screen.removePreference(showSeed);
        }

        if (prefs.getString(Constants.PREFERENCE_ENC_SEED, "").isEmpty()) {
            screen.removePreference(changePassword);
        }

        if (!prefs.getString(Constants.PREFERENCE_ENC_SEED, "").isEmpty() || IOTA.seed == null) {
            screen.removePreference(setPassword);
        }
    }
}