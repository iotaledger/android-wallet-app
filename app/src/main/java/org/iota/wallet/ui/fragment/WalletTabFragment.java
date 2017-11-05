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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.GetAccountDataRequest;
import org.iota.wallet.api.responses.GetAccountDataResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.helper.price.AlternateValueManager;
import org.iota.wallet.helper.price.AlternateValueUtils;
import org.iota.wallet.helper.price.ExchangeRateNotAvailableException;
import org.iota.wallet.helper.price.ExchangeRateUpdateTaskHandler;
import org.iota.wallet.model.Transfer;
import org.iota.wallet.ui.adapter.WalletPagerAdapter;
import org.knowm.xchange.currency.Currency;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jota.utils.IotaUnitConverter;

public class WalletTabFragment extends Fragment {

    private static final String BALANCE = "balance";
    private static final String ALTERNATE_BALANCE = "alternateBalance";
    private static final String WALLET_FAB_STATE = "walletFabState";
    @BindView(R.id.wallet_toolbar)
    Toolbar walletToolbar;
    @BindView(R.id.wallet_tab_viewpager)
    ViewPager viewPager;
    @BindView(R.id.wallet_tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar_title_layout_balance)
    TextView balanceTextView;
    @BindView(R.id.toolbar_title_layout_alternate_balance)
    TextView alternateBalanceTextView;
    @BindView(R.id.fab_wallet)
    FloatingActionButton fabWallet;
    private long walletBalanceIota;
    private List<Transfer> transfers;
    private AlternateValueManager alternateValueManager;
    private boolean isConnected = false;
    private WalletPagerAdapter adapter;
    private int currentPagerPosition = 0;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alternateValueManager = new AlternateValueManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(walletToolbar);
        setViewPager();
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.fab_wallet)
    public void onFabWalletClick() {
        if (isConnected && adapter != null) {
            adapter.performFabClick(currentPagerPosition);
        }
    }

    private void setViewPager() {
        adapter = new WalletPagerAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPagerPosition = position;
                updateFab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Subscribe
    public void onEvent(GetAccountDataResponse gad) {
        if (transfers == null) {
            transfers = new ArrayList<>();
        }

        transfers = gad.getTransfers();

        isConnected = true;

        currentPagerPosition = viewPager.getCurrentItem();
        updateFab();

        walletBalanceIota = 0;

        walletBalanceIota = walletBalanceIota + gad.getBalance();

        String balanceText = IotaUnitConverter.convertRawIotaAmountToDisplayText(walletBalanceIota, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.edit().putLong(Constants.PREFERENCES_CURRENT_IOTA_BALANCE, walletBalanceIota).apply();
        if (!TextUtils.isEmpty(balanceText)) {
            balanceTextView.setText(balanceText);
        } else {
            balanceTextView.setText(R.string.account_balance_default);
        }
        updateAlternateBalance();
    }

    private void updateFab() {
        if (!isConnected) {
            fabWallet.hide();
            fabWallet.setEnabled(false);
        } else {
            fabWallet.show();
            fabWallet.setEnabled(true);

            switch (currentPagerPosition) {
                case 0:
                    fabWallet.setImageResource(R.drawable.ic_fab_send);
                    break;
                case 1:
                    fabWallet.setImageResource(R.drawable.ic_add);
                    break;
            }
        }
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case REMOTE_NODE_ERROR:
            case IOTA_COOL_NETWORK_ERROR:
            case NETWORK_ERROR:
                isConnected = false;
                currentPagerPosition = viewPager.getCurrentItem();
                updateFab();
                break;
        }
    }

    @Subscribe
    public void onEvent(ExchangeRateUpdateTaskHandler.ExchangeRateUpdateCompleted event) {
        updateAlternateBalance();
    }

    private void requestExchangeRateUpdate() {
        alternateValueManager.updateExchangeRatesAsync(false);
    }

    private void updateAlternateBalance() {
        Currency alternateCurrency = Utils.getConfiguredAlternateCurrency(getActivity());

        try {
            float alternateCurrencyValue = alternateValueManager.convert(this.walletBalanceIota, alternateCurrency);
            alternateBalanceTextView.setText(AlternateValueUtils.formatAlternateBalanceText(alternateCurrencyValue, alternateCurrency));
        } catch (ExchangeRateNotAvailableException e) {
            // handle the case in which we have no stored exchange rate
            alternateBalanceTextView.setText("");

            // additionally we could do the following
            // but we need to limit/control the update rate somehow first!
            // requestExchangeRateUpdate();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BALANCE, balanceTextView == null ? "" : TextUtils.isEmpty(balanceTextView.getText()) ?
                getString(R.string.account_balance_default) : balanceTextView.getText().toString());
        outState.putString(ALTERNATE_BALANCE, alternateBalanceTextView == null ? "" : alternateBalanceTextView.getText().toString());
        outState.putBoolean(WALLET_FAB_STATE, isConnected);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            balanceTextView.setText(savedInstanceState.getString(BALANCE));
            alternateBalanceTextView.setText(savedInstanceState.getString(ALTERNATE_BALANCE));
            isConnected = savedInstanceState.getBoolean(WALLET_FAB_STATE);
            updateFab();
        }
    }

    private void getAccountData() {
        TaskManager rt = new TaskManager(getActivity());
        GetAccountDataRequest gir = new GetAccountDataRequest();
        rt.startNewRequestTask(gir);
        requestExchangeRateUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        getAccountData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (isConnected) {
                getAccountData();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public interface OnFabClickListener {
        void onFabClick();
    }
}