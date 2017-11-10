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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.psdev.licensesdialog.LicensesDialog;

public class AboutFragment extends Fragment {

    public static final String IOTA_DONATION_ADDRESS = "TBH9CSFWUHACJSWGA9XDDMNPJ9USPRLJ9FCHDEYDYGOWPQTQUWXMUBCUKTFJRESNBHGJOISFJOLXTLZOBRLLGVTROD";
    private static final String IOTA_DONATION_TAG = "ANDROID9WALLET9DONATION9999";
    private static final String PACKAGE_WEBVIEW = "com.google.android.webview";

    @BindView(R.id.about_toolbar)
    Toolbar aboutToolbar;
    @BindView(R.id.about_version)
    TextView versionTextView;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        unbinder = ButterKnife.bind(this, view);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(aboutToolbar);
        initAppVersion();

    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + AboutFragment.PACKAGE_WEBVIEW)));
        }
    }

    @OnClick(R.id.about_changelog)
    public void onAboutChangeLogClick() {
        ChangelogDialog changelogDialog = new ChangelogDialog();
        changelogDialog.show(getActivity().getFragmentManager(), null);
    }

    @OnClick(R.id.about_licenses)
    public void onAboutLicensesClick() {
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
            snackbar.setAction(R.string.message_install_web_view, v -> openPlayStore());
            snackbar.show();
        }
    }

    @OnClick(R.id.about_donation_btc)
    public void onAboutDonationsBtcClick() {
        Uri uri = Uri.parse("https://blockchain.info/address/1MyCJP3yZtSJ3bMVEoQRPSY3D6Ev7CTvzo");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.about_donation_iota)
    public void onAboutDonationIotaClick() {
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
    }

    @OnClick(R.id.about_donation_website)
    public void onAboutDonationWebsite() {
        Uri uri = Uri.parse("https://iotawallet.info");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @OnClick(R.id.about_faq)
    public void onAboutFaqClick() {
        Uri uri = Uri.parse("http://iotasupport.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
