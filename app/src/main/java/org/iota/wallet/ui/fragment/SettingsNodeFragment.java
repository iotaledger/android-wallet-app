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
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;

import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;

public class SettingsNodeFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_node);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREFERENCE_NODE_PROTOCOL:
                String nodeProtocol = sharedPreferences.getString(key, "");
                if (nodeProtocol.isEmpty()) {
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText(Constants.PREFERENCE_NODE_DEFAULT_PROTOCOL);
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.settings_messages_empty_node_protocol, Snackbar.LENGTH_LONG).show();
                } else if (!nodeProtocol.equals("http") || !nodeProtocol.equals("https")) {
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText(Constants.PREFERENCE_NODE_DEFAULT_PROTOCOL);
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.settings_messages_wrong_node_protocol, Snackbar.LENGTH_LONG).show();
                }
                break;
            case Constants.PREFERENCE_NODE_IP:
                String nodeIp = sharedPreferences.getString(key, "");
                if (nodeIp.isEmpty()) {
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText(Constants.PREFERENCE_NODE_DEFAULT_IP);
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.settings_messages_empty_node_ip, Snackbar.LENGTH_LONG).show();
                }
                break;
            case Constants.PREFERENCE_NODE_PORT:
                String nodePort = sharedPreferences.getString(key, "");
                if (nodePort.isEmpty()) {
                    EditTextPreference p = (EditTextPreference) findPreference(key);
                    p.setText(Constants.PREFERENCE_NODE_DEFAULT_PORT);
                    if (getView() != null)
                        Snackbar.make(getView(), R.string.settings_messages_empty_node_port, Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}