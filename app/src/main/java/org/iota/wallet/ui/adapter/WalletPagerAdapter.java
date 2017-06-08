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

package org.iota.wallet.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import org.iota.wallet.R;
import org.iota.wallet.ui.fragment.WalletAddressesFragment;
import org.iota.wallet.ui.fragment.WalletTransfersFragment;

public class WalletPagerAdapter extends FragmentPagerAdapter {

    private static final int TAB_COUNT = 2;
    private final Context context;
    private WalletTransfersFragment transfersFragment;
    private WalletAddressesFragment addressesFragment;

    public WalletPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return transfersFragment = new WalletTransfersFragment();
            case 1:
                return addressesFragment = new WalletAddressesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.transfers);
            case 1:
                return context.getResources().getString(R.string.addresses);
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                transfersFragment = (WalletTransfersFragment) fragment;
                break;
            case 1:
                addressesFragment = (WalletAddressesFragment) fragment;
                break;
        }
        return fragment;
    }

    public void performFabClick(int position) {
        switch (position) {
            case 0:
                if (transfersFragment != null) {
                    transfersFragment.onFabClick();
                }
                break;
            case 1:
                if (addressesFragment != null) {
                    addressesFragment.onFabClick();
                }
                break;
        }
    }
}
