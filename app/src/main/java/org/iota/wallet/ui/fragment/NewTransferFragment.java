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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.SendTransferRequest;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.PermissionRequestHelper;
import org.iota.wallet.model.QRCode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jota.error.InvalidAddressException;
import jota.utils.Checksum;
import jota.utils.InputValidator;
import jota.utils.IotaUnitConverter;
import jota.utils.IotaUnits;

public class NewTransferFragment extends Fragment {

    private static final String ADDRESS = "address";
    private static final String AMOUNT = "amount";
    private static final String MESSAGE = "message";
    private static final String TAG = "tag";
    private static final String SPINNER_POISTION = "spinnerPosition";
    private InputMethodManager inputManager;

    @BindView(R.id.new_transfer_toolbar)
    Toolbar newTransferToolbar;
    @BindView(R.id.new_transfer_amount_input)
    TextInputEditText amountEditText;
    @BindView(R.id.new_transfer_address_input)
    TextInputEditText addressEditText;
    @BindView(R.id.new_transfer_message_input)
    TextInputEditText messageEditText;
    @BindView(R.id.new_transfer_tag_input)
    TextInputEditText tagEditText;
    @BindView(R.id.new_transfer_address_text_input_layout)
    TextInputLayout addressEditTextInputLayout;
    @BindView(R.id.new_transfer_amount_text_input_layout)
    TextInputLayout amountEditTextInputLayout;
    @BindView(R.id.new_transfer_message_text_input_layout)
    TextInputLayout messageEditTextInputLayout;
    @BindView(R.id.new_transfer_tag_input_layout)
    TextInputLayout tagEditTextInputLayout;
    @BindView(R.id.new_transfer_units_spinner)
    Spinner unitsSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_transfer, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(newTransferToolbar);

        initUnitsSpinner();

        Bundle bundle = getArguments();
        if (getArguments() != null) {
            QRCode qrCode = bundle.getParcelable(Constants.QRCODE);

            if (qrCode != null) {

                if (qrCode.getAddress() != null)
                    addressEditText.setText(qrCode.getAddress());

                //disable address text if its a donation
                if (qrCode.getAddress() != null)
                    if (qrCode.getAddress().equals(AboutFragment.IOTA_DONATION_ADDRESS))
                        addressEditText.setEnabled(false);

                if (qrCode.getAmount() != null && !qrCode.getAmount().isEmpty()) {
                    Long amount = Long.parseLong((qrCode.getAmount()));
                    IotaUnits unit = IotaUnitConverter.findOptimalIotaUnitToDisplay(amount);
                    String amountText = IotaUnitConverter.createAmountDisplayText(IotaUnitConverter.convertAmountTo(amount, unit), unit, false);
                    amountEditText.setText(amountText);
                    unitsSpinner.setSelection(toSpinnerItemIndex(unit));
                }

                if (qrCode.getMessage() != null)
                    messageEditText.setText(qrCode.getMessage());

                if (qrCode.getTag() != null)
                    tagEditText.setText(qrCode.getTag());

            }
        }
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.new_transfer_send_fab_button)
    public void onNewTransferSendFabClick(FloatingActionButton fab) {
        inputManager.hideSoftInputFromWindow(fab.getWindowToken(), 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        addressEditTextInputLayout.setError(null);
        tagEditTextInputLayout.setError(null);
        amountEditTextInputLayout.setError(null);
        messageEditTextInputLayout.setError(null);

        if (!isValidAddress()) {

        } else if (getAmount().isEmpty() || getAmount().equals("0")) {
            amountEditTextInputLayout.setError(getString(R.string.messages_enter_amount));

        } else if (prefs.getLong(Constants.PREFERENCES_CURRENT_IOTA_BALANCE, 0) < Long.parseLong(amountInSelectedUnit())) {
            amountEditTextInputLayout.setError(getString(R.string.messages_not_enough_balance));

        } else if (!InputValidator.isTrytes(getMessage(), getMessage().length()) && !getMessage().equals(getMessage().toUpperCase())) {
            messageEditTextInputLayout.setError(getString(R.string.messages_invalid_characters));

        } else if (!InputValidator.isTrytes(getTaG(), getTaG().length()) && !getTaG().equals(getTaG().toUpperCase())) {
            tagEditTextInputLayout.setError(getString(R.string.messages_invalid_characters));

        } else if (getTaG().length() > 27) {
            tagEditTextInputLayout.setError(getString(R.string.messages_tag_to_long));

        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.message_confirm_transfer)
                    .setCancelable(false)
                    .setPositiveButton(R.string.buttons_ok, null)
                    .setNegativeButton(R.string.buttons_cancel, null)
                    .create();

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttons_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SendTransferRequest tir = new SendTransferRequest(getAddress(),
                                    amountInSelectedUnit(), getMessage(), getTaG());

                            TaskManager rt = new TaskManager(getActivity());
                            rt.startNewRequestTask(tir);

                            getActivity().onBackPressed();
                        }
                    });

            alertDialog.show();
        }
    }

    private String amountInSelectedUnit() {
        String inputAmount = amountEditText.getText().toString();
        IotaUnits unit = toIotaUnit(unitsSpinner.getSelectedItemPosition());
        Long iota = Long.parseLong(inputAmount) * (long) Math.pow(10, unit.getValue());
        return iota.toString();
    }

    private IotaUnits toIotaUnit(int unitSpinnerItemIndex) {
        IotaUnits iotaUnits;

        switch (unitSpinnerItemIndex) {
            case 0:
                iotaUnits = IotaUnits.IOTA;
                break;
            case 1:
                iotaUnits = IotaUnits.KILO_IOTA;
                break;
            case 2:
                iotaUnits = IotaUnits.MEGA_IOTA;
                break;
            case 3:
                iotaUnits = IotaUnits.GIGA_IOTA;
                break;
            case 4:
                iotaUnits = IotaUnits.TERA_IOTA;
                break;
            case 5:
                iotaUnits = IotaUnits.PETA_IOTA;
                break;
            default:
                iotaUnits = IotaUnits.IOTA;
                break;
        }

        return iotaUnits;
    }

    private int toSpinnerItemIndex(IotaUnits unit) {
        int iotaUnits;

        if (unit == IotaUnits.IOTA) {
            iotaUnits = 0;
        } else if (unit == IotaUnits.KILO_IOTA) {
            iotaUnits = 1;
        } else if (unit == IotaUnits.MEGA_IOTA) {
            iotaUnits = 2;
        } else if (unit == IotaUnits.GIGA_IOTA) {
            iotaUnits = 3;
        } else if (unit == IotaUnits.TERA_IOTA) {
            iotaUnits = 4;
        } else if (unit == IotaUnits.PETA_IOTA) {
            iotaUnits = 5;
        } else {
            iotaUnits = 0;
        }

        return iotaUnits;
    }

    private void initUnitsSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.listIotaUnits));
        unitsSpinner.setAdapter(adapter);
        unitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_transfer_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_qr_code:
                openQRCodeScanner();
        }
        return false;
    }

    private void openQRCodeScanner() {
        if (!PermissionRequestHelper.hasCameraPermission(getActivity())) {
            checkPermissionCamera();
        } else {
            Fragment fragment = new QRScannerFragment();
            getActivity().getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.container, fragment, null)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openQRCodeScanner();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION);

            } else {

                //Camera permissions have not been granted yet so request them directly
                this.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        Constants.REQUEST_CAMERA_PERMISSION);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ADDRESS, getAddress());
        outState.putString(AMOUNT, getAmount());
        outState.putString(MESSAGE, getMessage());
        outState.putString(TAG, getTaG());
        outState.putInt(SPINNER_POISTION, unitsSpinner.getSelectedItemPosition());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            addressEditText.setText(savedInstanceState.getString(ADDRESS));
            amountEditText.setText(savedInstanceState.getString(AMOUNT));
            messageEditText.setText(savedInstanceState.getString(MESSAGE));
            tagEditText.setText(savedInstanceState.getString(TAG));
            unitsSpinner.setSelection(savedInstanceState.getInt(SPINNER_POISTION));
        }
    }

    private boolean isValidAddress() {
        String address = addressEditText.getText().toString();
        try {
            if (Checksum.isAddressWithoutChecksum(address)) {
            }
        } catch (InvalidAddressException e) {
            addressEditTextInputLayout.setError(getString(R.string.messages_enter_txaddress_with_checksum));
            return false;
        }
        return true;
    }

    private String getAddress() {
        try {
            return Checksum.removeChecksum(addressEditText.getText().toString());
        } catch (InvalidAddressException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getAmount() {
        return amountEditText.getText().toString();
    }

    private String getMessage() {
        return messageEditText.getText().toString();
    }

    private String getTaG() {
        if (tagEditText.getText().toString().isEmpty())
            return Constants.NEW_TRANSFER_TAG;
        else if (tagEditText.getText().toString().length() < 27)
            return StringUtils.rightPad(tagEditText.getText().toString(), 27, '9');
        else
            return tagEditText.getText().toString();
    }
}