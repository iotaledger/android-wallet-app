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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.price.AlternateValueManager;
import org.iota.wallet.ui.adapter.TangleExplorerPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TangleExplorerTabFragment extends Fragment {

    private TangleExplorerPagerAdapter adapter;
    @BindView(R.id.tangle_explorer_toolbar)
    Toolbar tangleExplorerToolbar;
    @BindView(R.id.tangle_explorer_tabs)
    TabLayout tabs;
    @BindView(R.id.tangle_explorer_tab_viewpager)
    ViewPager viewPager;

    private AlternateValueManager alternateValueManager;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new TangleExplorerPagerAdapter(getActivity(), getChildFragmentManager());
        alternateValueManager = new AlternateValueManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tangle_explorer_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(tangleExplorerToolbar);
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @Subscribe
    public void onEvent(Bundle bundle) {
        if (bundle != null && bundle.containsKey(Constants.TANGLE_EXPLORER_SEARCH_ITEM)) {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        requestExchangeRateUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        adapter.getItem(0).onHiddenChanged(hidden);
    }

    private void requestExchangeRateUpdate() {
        alternateValueManager.updateExchangeRatesAsync(false);
    }
}
