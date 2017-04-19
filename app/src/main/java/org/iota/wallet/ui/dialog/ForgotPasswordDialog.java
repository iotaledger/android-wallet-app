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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;

import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;

public class ForgotPasswordDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public ForgotPasswordDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_forgot_password)
                .setMessage(R.string.message_forgot_password)
                .setPositiveButton(R.string.buttons_ok, this)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                prefs.edit().remove(Constants.PREFERENCE_ENC_SEED).apply();
                prefs.edit().remove(Constants.PREFERENCES_TRANSFER_CACHING).apply();
                prefs.edit().remove(Constants.PREFERENCES_ADDRESS_CACHING).apply();
                getDialog().dismiss();
                Intent intent = new Intent(getActivity().getIntent());
                getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
        }
    }
}
