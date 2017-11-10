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
import android.content.Intent;
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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.AESCrypt;
import org.iota.wallet.helper.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;

public class EncryptSeedDialog extends DialogFragment {

    @BindView(R.id.password_input_layout)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password_confirm_input_layout)
    TextInputLayout textInputLayoutPasswordConfirm;
    @BindView(R.id.password)
    TextInputEditText textInputEditTextPassword;
    @BindView(R.id.password_confirm)
    TextInputEditText textInputEditTextPasswordConfirm;
    private String seed;

    public EncryptSeedDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_encrypt_seed_password, null, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        seed = bundle.getString("seed");

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.title_enter_password)
                .setMessage(R.string.message_enter_password)
                .setCancelable(false)
                .setPositiveButton(R.string.buttons_save, null)
                .setNegativeButton(R.string.buttons_cancel, null)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> encryptSeed());
        });

        alertDialog.show();
        return alertDialog;

    }

    @OnEditorAction(R.id.password_confirm)
    public boolean onPasswordConfirmEditorAction(int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            encryptSeed();
        }
        return true;
    }

    private void encryptSeed() {
        String password = textInputEditTextPassword.getText().toString();
        String passwordConfirm = textInputEditTextPasswordConfirm.getText().toString();

        //reset errors
        textInputLayoutPassword.setError(null);
        textInputLayoutPasswordConfirm.setError(null);

        if (password.isEmpty())
            textInputLayoutPassword.setError(getActivity().getString(R.string.messages_empty_password));
        else if (!password.equals(passwordConfirm))
            textInputLayoutPasswordConfirm.setError(getActivity().getString(R.string.messages_match_password));
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            try {
                AESCrypt aes = new AESCrypt(passwordConfirm);
                prefs.edit().putString(Constants.PREFERENCE_ENC_SEED, aes.encrypt(seed)).apply();
                IOTA.seed = seed.toCharArray();
                getDialog().dismiss();
                Intent intent = new Intent(getActivity().getIntent());
                getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

}
