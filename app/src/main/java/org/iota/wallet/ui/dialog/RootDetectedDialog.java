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

package org.iota.wallet.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;

import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;

public class RootDetectedDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public RootDetectedDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.title_root_detected)
                .setMessage(R.string.message_root_detected)
                .setCancelable(false)
                .setPositiveButton(R.string.buttons_ok, null)
                .setNegativeButton(R.string.buttons_cancel, this)
                .setNeutralButton(R.string.buttons_again, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case AlertDialog.BUTTON_NEGATIVE:
                getActivity().finish();
                break;
            case AlertDialog.BUTTON_NEUTRAL:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().putBoolean(Constants.PREFERENCE_RUN_WITH_ROOT, true).apply();
                getDialog().dismiss();
                break;
        }
    }
}
