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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.iota.wallet.R;
import org.iota.wallet.ui.activity.MainActivity;
import org.iota.wallet.ui.fragment.GenerateQRCodeFragment;
import org.iota.wallet.ui.fragment.TangleExplorerTabFragment;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.QRCode;

public class WalletAddressesItemDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private String address;

    public WalletAddressesItemDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        address = bundle.getString("address");

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.address))
                .setItems(R.array.listOnWalletAddressesRecyclerViewClickDialog, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        Fragment fragment;
        final Bundle bundle = new Bundle();
        switch (which) {
            case 0:
                ClipData clipAddress = ClipData.newPlainText(getActivity().getString(R.string.address), address);
                clipboard.setPrimaryClip(clipAddress);
                break;
            case 1:
                fragment = new TangleExplorerTabFragment();
                bundle.putString(Constants.TANGLE_EXPLORER_SEARCH_ITEM, address);

                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showFragment(fragment);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(bundle);

                    }
                }, 300);

                break;
            case 2:
                QRCode qrCode = new QRCode();
                qrCode.setAddress(address);
                bundle.putParcelable(Constants.QRCODE, qrCode);

                fragment = new GenerateQRCodeFragment();
                fragment.setArguments(bundle);

                getActivity().getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment, null)
                        .addToBackStack(null)
                        .commit();
                break;
            default:
                break;
        }
    }
}