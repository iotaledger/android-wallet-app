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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.AESCrypt;
import org.iota.wallet.helper.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;

public class ShowSeedDialog extends DialogFragment {

    @BindView(R.id.password_input_layout)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.decrypted_seed)
    TextView textViewSeed;

    public ShowSeedDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_show_seed, null, false);
        ButterKnife.bind(this, dialogView);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.title_enter_password)
                .setPositiveButton(R.string.buttons_show, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Button bPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            final Button bNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // if seed is not password protected
            if (prefs.getString(Constants.PREFERENCE_ENC_SEED, "").isEmpty()) {

                textInputLayoutPassword.setVisibility(View.GONE);

                if (IOTA.seed != null)
                    textViewSeed.setText(String.valueOf(IOTA.seed));

                // update the dialog
                alertDialog.setTitle(getString(R.string.title_current_seed));
                bNegative.setText(R.string.buttons_ok);
                bPositive.setEnabled(false);
            }

            bPositive.setOnClickListener(view -> {
                showPassword();
                if (!textViewSeed.getText().toString().isEmpty()) {

                    textInputLayoutPassword.setVisibility(View.GONE);

                    // update the dialog
                    alertDialog.setTitle(getString(R.string.title_current_seed));
                    bNegative.setText(R.string.buttons_ok);
                    bPositive.setEnabled(false);
                }
            });
        });

        alertDialog.show();
        return alertDialog;
    }

    @OnEditorAction(R.id.password)
    public boolean onPasswordEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            showPassword();
        }
        return true;
    }

    private void showPassword() {
        String password = textInputEditTextPassword.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //reset errors
        textInputLayoutPassword.setError(null);

        try {
            AESCrypt aes = new AESCrypt(password);
            String encSeed = prefs.getString(Constants.PREFERENCE_ENC_SEED, "");
            textViewSeed.setText(aes.decrypt(encSeed));

        } catch (Exception e) {
            textInputLayoutPassword.setError(getActivity().getString(R.string.messages_invalid_password));
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            textInputEditTextPassword.startAnimation(shake);
            e.getStackTrace();
        }
    }

}
