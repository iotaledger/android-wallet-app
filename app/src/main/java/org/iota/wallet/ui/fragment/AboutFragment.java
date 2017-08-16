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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.QRCode;
import org.iota.wallet.ui.dialog.ChangelogDialog;

import de.psdev.licensesdialog.LicensesDialog;

public class AboutFragment extends Fragment implements View.OnClickListener {

    public static final String IOTA_DONATION_ADDRESS = "DBPECSH9YLSSTQDGERUHJBBJTKVUDBMTJLG9WPHBINGHIFOSJMDJLARTVOXXWEFQJLLBINOHCZGYFSMUEXWPPMTOFW";
    private static final String IOTA_DONATION_TAG = "ANDROID9WALLET9DONATION9999";
    private static final String PACKAGE_WEBVIEW = "com.google.android.webview";

    private TextView versionTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) view.findViewById(R.id.about_toolbar));

        versionTextView = view.findViewById(R.id.about_version);
        TextView changelogTextView = view.findViewById(R.id.about_changelog);
        TextView licensesTextView = view.findViewById(R.id.about_licenses);

        TextView donationBtcTextView = view.findViewById(R.id.about_donation_btc);
        TextView donationIotaTextView = view.findViewById(R.id.about_donation_iota);
        TextView donationWebsiteTextView = view.findViewById(R.id.about_donation_website);

        TextView faqWebsiteTextView = view.findViewById(R.id.about_faq);

        changelogTextView.setOnClickListener(this);
        licensesTextView.setOnClickListener(this);

        donationWebsiteTextView.setOnClickListener(this);
        donationBtcTextView.setOnClickListener(this);
        donationIotaTextView.setOnClickListener(this);
        faqWebsiteTextView.setOnClickListener(this);
        initAppVersion();

        return view;
    }

    private void initAppVersion() {
        try {
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            versionTextView.setText(getString(R.string.about_version, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setText(R.string.about_version_unknown);
        }
    }

    private void showChangelogDialog() {
        ChangelogDialog changelogDialog = new ChangelogDialog();
        changelogDialog.show(getActivity().getFragmentManager(), null);
    }

    private void showLicenseDialog() {
        try {
            new LicensesDialog.Builder(getActivity())
                    .setNotices(R.raw.licenses)
                    .setTitle(R.string.about_licenses)
                    .setIncludeOwnLicense(true)
                    .setCloseText(R.string.buttons_ok)
                    .build()
                    .showAppCompat();

        } catch (AndroidRuntimeException e) {
            View contentView = getActivity().getWindow().getDecorView();
            Snackbar snackbar = Snackbar.make(contentView,
                    R.string.message_open_licenses_error, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.message_install_web_view,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openPlayStore();
                        }
                    });
            snackbar.show();
        }
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.about_changelog:
                showChangelogDialog();
                break;
            case R.id.about_licenses:
                showLicenseDialog();
                break;
            case R.id.about_donation_website:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://iotawallet.info")));
                } catch (android.content.ActivityNotFoundException ignored) {
                }
                break;
            case R.id.about_donation_btc:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blockchain.info/address/1MyCJP3yZtSJ3bMVEoQRPSY3D6Ev7CTvzo")));
                } catch (android.content.ActivityNotFoundException ignored) {
                }
                break;
            case R.id.about_donation_iota:
                if (IOTA.seed != null) {

                    QRCode qrCode = new QRCode();
                    qrCode.setAddress(IOTA_DONATION_ADDRESS);
                    qrCode.setTag(IOTA_DONATION_TAG);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.QRCODE, qrCode);

                    Fragment fragment = new NewTransferFragment();
                    fragment.setArguments(bundle);

                    getActivity().getFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, fragment, null)
                            .addToBackStack(null)
                            .commit();
                } else
                    Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_iota_donation_require_login), Snackbar.LENGTH_LONG).show();
                break;
            case R.id.about_faq:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://iotasupport.com")));
                } catch (android.content.ActivityNotFoundException ignored) {
                }
                break;
        }
    }
}
