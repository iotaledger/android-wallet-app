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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.AESCrypt;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.ui.dialog.ForgotPasswordDialog;

public class PasswordLoginFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final String PASSWORD = "password";
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_login, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.password_login_toolbar));

        AppCompatButton buttonPasswordLogin = (AppCompatButton) view.findViewById(R.id.password_login_button);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.password_forgot_text_input_layout);
        textInputEditTextPassword = (TextInputEditText) view.findViewById(R.id.password_login);
        TextView textViewForgotPassword = (TextView) view.findViewById(R.id.password_forgot);
        buttonPasswordLogin.setOnClickListener(this);
        textViewForgotPassword.setOnClickListener(this);
        textInputEditTextPassword.setOnEditorActionListener(this);

        return view;
    }

    private void login() {
        String password = textInputEditTextPassword.getText().toString();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            AESCrypt aes = new AESCrypt(password);
            String encSeed = prefs.getString(Constants.PREFERENCE_ENC_SEED, "");
            IOTA.seed = aes.decrypt(encSeed).toCharArray();
            Intent intent = new Intent(getActivity().getIntent());
            getActivity().startActivityForResult(intent, Constants.REQUEST_CODE_LOGIN);
        } catch (Exception e) {
            e.getStackTrace();
            textInputLayoutPassword.setError(getString(R.string.messages_invalid_password));
            Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            textInputEditTextPassword.startAnimation(shake);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.password_login_button:
                login();
                break;
            case R.id.password_forgot:
                ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
                forgotPasswordDialog.show(getActivity().getFragmentManager(), null);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            login();
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PASSWORD, textInputEditTextPassword.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            textInputEditTextPassword.setText(savedInstanceState.getString(PASSWORD));
        }
    }
}
