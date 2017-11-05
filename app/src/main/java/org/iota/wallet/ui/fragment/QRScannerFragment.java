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
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;

import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.QRCode;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        scannerView = new ZXingScannerView(getActivity());
        return scannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        QRCode qrCode = new QRCode();
        try {
            JSONObject json = new JSONObject(String.valueOf(result));
            qrCode.setAddress(json.getString("address"));
            qrCode.setAmount(json.getString("amount"));
            qrCode.setMessage(json.getString("message"));
            qrCode.setTag(json.getString("tag"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //remove all fragment from backStack, right, 2 times
        getActivity().onBackPressed();
        getActivity().onBackPressed();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.QRCODE, qrCode);

        Fragment fragment = new NewTransferFragment();
        fragment.setArguments(bundle);
        getActivity().getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment, null)
                .addToBackStack(null)
                .commit();
    }
}
