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

package org.iota.wallet.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.RootDetector;
import org.iota.wallet.model.QRCode;
import org.iota.wallet.ui.dialog.RootDetectedDialog;
import org.iota.wallet.ui.fragment.AboutFragment;
import org.iota.wallet.ui.fragment.GenerateQRCodeFragment;
import org.iota.wallet.ui.fragment.NeighborsFragment;
import org.iota.wallet.ui.fragment.NewTransferFragment;
import org.iota.wallet.ui.fragment.NodeInfoFragment;
import org.iota.wallet.ui.fragment.PasswordLoginFragment;
import org.iota.wallet.ui.fragment.QRScannerFragment;
import org.iota.wallet.ui.fragment.SeedLoginFragment;
import org.iota.wallet.ui.fragment.SettingsFragment;
import org.iota.wallet.ui.fragment.TangleExplorerTabFragment;
import org.iota.wallet.ui.fragment.WalletAddressesFragment;
import org.iota.wallet.ui.fragment.WalletTabFragment;
import org.iota.wallet.ui.fragment.WalletTransfersFragment;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String STATE_CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private static final String SHORTCUT_ID_GENERATE_QR_CODE = "generateQrCode";
    private static final String SHORTCUT_ID_SEND_TRANSFER = "sendTransfer";
    private static final int FRAGMENT_CONTAINER_ID = R.id.container;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private SharedPreferences prefs;
    private InputMethodManager inputManager;
    private String currentFragmentTag = null;
    private boolean killFragments = false;
    private OnBackPressedClickListener onBackPressedClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navigationView.setNavigationItemSelectedListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_FRAGMENT_TAG)) {
                currentFragmentTag = savedInstanceState.getString(STATE_CURRENT_FRAGMENT_TAG);
        }

        if (savedInstanceState == null || IOTA.seed == null) {
            navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
        }

        if (!prefs.getBoolean(Constants.PREFERENCE_RUN_WITH_ROOT, false)) {
            if (RootDetector.isDeviceRooted()) {
                RootDetectedDialog dialog = new RootDetectedDialog();
                dialog.show(this.getFragmentManager(), null);
            }
        }

        // shortcut intents
        if (savedInstanceState == null) {
            if (IOTA.seed != null) {
                if (Constants.ACTION_GENERATE_QR_CODE.equals(getIntent().getAction())) {
                    showFragment(new GenerateQRCodeFragment());
                } else if (Constants.ACTION_SEND_TRANSFER.equals(getIntent().getAction())) {
                    showFragment(new NewTransferFragment());
                }
            } else
                Snackbar.make(this.findViewById(R.id.drawer_layout), getString(R.string.messages_shortcuts_not_available), Snackbar.LENGTH_LONG);
        }


        drawer.addDrawerListener(drawerListener);
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            super.setSupportActionBar(toolbar);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            inputManager.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
        }
    };


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (onBackPressedClickListener != null)
                onBackPressedClickListener.onBackPressedClickListener();
            else if (getFragmentManager().getBackStackEntryCount() > 0)
                getFragmentManager().popBackStack();
            else
                super.onBackPressed();
        }
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        String errorMessage = "";
        switch (error.getErrorType()) {
            case REMOTE_NODE_ERROR:
                errorMessage = getString(R.string.messages_network_remote_error);
                break;
            case NETWORK_ERROR:
                errorMessage = getString(R.string.messages_network_error);
                break;
            case ACCESS_ERROR:
                errorMessage = getString(R.string.messages_network_access_error);
                break;
            case INVALID_HASH_ERROR:
                errorMessage = getString(R.string.messages_invalid_hash_error);
                break;
            case EXCHANGE_RATE_ERROR:
                errorMessage = getString(R.string.messages_exchange_rate_error);
                break;
            case IOTA_COOL_NETWORK_ERROR:
                errorMessage = getString(R.string.messages_network_iota_cool_error);
                break;
        }

        Snackbar.make(findViewById(R.id.drawer_layout), errorMessage, Snackbar.LENGTH_LONG)
                .setAction(null, null).show();
    }

    /**
     * Shows a fragment and hides the old one if there was a fragment previously visible
     */
    private void showFragment(Fragment fragment, boolean addToBackStack, boolean killFragments) {

        if (fragment == null) {
            // Do nothing
            return;
        }

        FragmentManager fragmentManager = getFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);

        if (currentFragment != null && currentFragment.getClass().getName().equals(fragment.getClass().getName())) {
            // Fragment already shown, do nothing
            return;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (killFragments) {
            Class[] fragmentsToKill = {
                    AboutFragment.class,
                    GenerateQRCodeFragment.class,
                    NeighborsFragment.class,
                    NodeInfoFragment.class,
                    PasswordLoginFragment.class,
                    QRScannerFragment.class,
                    SeedLoginFragment.class,
                    SettingsFragment.class,
                    TangleExplorerTabFragment.class,
                    NewTransferFragment.class,
                    WalletAddressesFragment.class,
                    WalletTabFragment.class,
                    WalletTransfersFragment.class
            };
            for (Class fragmentClass : fragmentsToKill) {
                String tag = fragmentClass.getSimpleName();
                if (tag.equals(fragment.getClass().getSimpleName())) {
                    continue;
                }
                Fragment fragmentToKill = fragmentManager.findFragmentByTag(tag);
                if (fragmentToKill != null) {
                    fragmentTransaction.remove(fragmentToKill);
                }
            }
        }

        fragmentTransaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out,
                R.animator.fade_in, R.animator.fade_out);

        if (currentFragment != null) {
            // Hide old fragment
            fragmentTransaction.hide(currentFragment);
        }

        String tag = fragment.getClass().getSimpleName();
        Fragment cachedFragment = fragmentManager.findFragmentByTag(tag);
        if (cachedFragment != null) {
            // Cached fragment available
            fragmentTransaction.show(cachedFragment);
        } else {
            fragmentTransaction.add(FRAGMENT_CONTAINER_ID, fragment, tag);
        }
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

        if (fragment instanceof OnBackPressedClickListener) {
            onBackPressedClickListener = (OnBackPressedClickListener) fragment;
        } else
            onBackPressedClickListener = null;

        // setChecked if open from WalletItemDialog
        if (fragment instanceof TangleExplorerTabFragment)
            navigationView.getMenu().findItem(R.id.nav_tangle_explorer).setChecked(true);

        currentFragmentTag = tag;
    }

    private void showFragment(Fragment fragment, boolean addToBackStack) {
        showFragment(fragment, addToBackStack, false);
    }

    public void showFragment(Fragment fragment) {
        showFragment(fragment, false);
    }

    private void showLogoutNavigationItem() {
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(IOTA.seed != null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        getFragmentManager().popBackStack();

        switch (item.getItemId()) {
            case R.id.nav_wallet:
                String encSeed = prefs.getString(Constants.PREFERENCE_ENC_SEED, "");
                if (!encSeed.isEmpty() && IOTA.seed == null) {
                    showLogoutNavigationItem();
                    fragment = new PasswordLoginFragment();
                    killFragments = true;
                } else if (IOTA.seed == null) {
                    showLogoutNavigationItem();
                    fragment = new SeedLoginFragment();
                    killFragments = true;
                } else {
                    showLogoutNavigationItem();
                    fragment = getFragmentManager().findFragmentById(R.id.container);
                    if (fragment != null && fragment instanceof PasswordLoginFragment || fragment instanceof SeedLoginFragment)
                        killFragments = true;
                    fragment = new WalletTabFragment();
                }
                break;

            case R.id.nav_tangle_explorer:
                fragment = new TangleExplorerTabFragment();
                break;

            case R.id.nav_node_info:
                fragment = new NodeInfoFragment();
                break;

            case R.id.nav_neighbors:
                fragment = new NeighborsFragment();
                break;

            case R.id.nav_settings:
                Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
                break;

            case R.id.nav_about:
                fragment = new AboutFragment();
                break;

            case R.id.nav_logout:
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.message_confirm_logout)
                        .setCancelable(false)
                        .setPositiveButton(R.string.buttons_ok, null)
                        .setNegativeButton(R.string.buttons_cancel, null)
                        .setNeutralButton(R.string.buttons_backup, null)
                        .create();

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttons_ok), (dialog, which) -> {
                            prefs.edit().remove(Constants.PREFERENCE_ENC_SEED).apply();
                            IOTA.seed = null;
                            TaskManager.stopAndDestroyAllTasks(MainActivity.this);
                            killFragments = true;
                            navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
                        });

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.buttons_backup), (dialog, which) -> {
                    Intent settings1 = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settings1);
                });

                alertDialog.show();

                break;
        }

        if (fragment != null) {
            showFragment(fragment, false, killFragments);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateDynamicShortcuts() {
        ShortcutManager shortcutManager;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {

            Intent intentGenerateQrCode = new Intent(this, MainActivity.class);
            intentGenerateQrCode.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            intentGenerateQrCode.setAction(Constants.ACTION_GENERATE_QR_CODE);

            ShortcutInfo shortcutGenerateQrCode = new ShortcutInfo.Builder(this, SHORTCUT_ID_GENERATE_QR_CODE)
                    .setShortLabel(getString(R.string.shortcut_generate_qr_code))
                    .setLongLabel(getString(R.string.shortcut_generate_qr_code))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_qr))
                    .setIntent(intentGenerateQrCode)
                    .build();

            Intent intentTransferIotas = new Intent(this, MainActivity.class);
            intentTransferIotas.setFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            intentTransferIotas.setAction(Constants.ACTION_SEND_TRANSFER);

            ShortcutInfo shortcutTransferIotas = new ShortcutInfo.Builder(this, SHORTCUT_ID_SEND_TRANSFER)
                    .setShortLabel(getString(R.string.shortcut_send_transfer))
                    .setLongLabel(getString(R.string.shortcut_send_transfer))
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_transaction))
                    .setIntent(intentTransferIotas)
                    .build();

            shortcutManager = getSystemService(ShortcutManager.class);

            if (shortcutManager != null) {
                if (IOTA.seed != null) {
                    shortcutManager.setDynamicShortcuts(Arrays.asList(shortcutGenerateQrCode, shortcutTransferIotas));
                    shortcutManager.enableShortcuts(Arrays.asList(SHORTCUT_ID_GENERATE_QR_CODE, SHORTCUT_ID_SEND_TRANSFER));
                } else {
                    // remove shortcuts if Iota.seed.isEmpty()
                    shortcutManager.disableShortcuts(Arrays.asList(SHORTCUT_ID_GENERATE_QR_CODE, SHORTCUT_ID_SEND_TRANSFER));
                    shortcutManager.removeAllDynamicShortcuts();
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted!

                } else {

                    // permission denied!

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                        Snackbar.make(findViewById(R.id.drawer_layout), R.string.messages_permission_camera,
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.buttons_ok, view -> {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivityForResult(intent, Constants.REQUEST_CAMERA_PERMISSION);
                                })
                                .show();
                    }
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CODE_LOGIN:
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                navigationView.getMenu().performIdentifierAction(R.id.nav_wallet, 0);
        }
        if (data != null) {
            if (Intent.ACTION_VIEW.equals(data.getAction())) {
                QRCode qrCode = new QRCode();
                Uri uri = data.getData();
                qrCode.setAddress(uri.getQueryParameter("address:"));
                qrCode.setAddress(uri.getQueryParameter("amount:"));
                qrCode.setAddress(uri.getQueryParameter("message:"));

                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.QRCODE, qrCode);

                Fragment fragment = new NewTransferFragment();
                fragment.setArguments(bundle);
                showFragment(fragment, true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        showLogoutNavigationItem();
        updateDynamicShortcuts();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateDynamicShortcuts();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CURRENT_FRAGMENT_TAG, currentFragmentTag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentFragmentTag = savedInstanceState.getString(STATE_CURRENT_FRAGMENT_TAG);
    }

    @Override
    public void onDestroy() {
        drawer.removeDrawerListener(drawerListener);
        super.onDestroy();
    }

    public interface OnBackPressedClickListener {
        void onBackPressedClickListener();
    }
}
