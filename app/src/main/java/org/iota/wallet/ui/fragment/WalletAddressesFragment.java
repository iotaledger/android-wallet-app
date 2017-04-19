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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.IOTA;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.databinding.FragmentWalletAddressesBinding;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.model.api.requests.GetNewAddressRequest;
import org.iota.wallet.model.api.requests.NodeInfoRequest;
import org.iota.wallet.model.api.requests.SendTransferRequest;
import org.iota.wallet.model.api.responses.GetNewAddressResponse;
import org.iota.wallet.model.api.responses.NodeInfoResponse;
import org.iota.wallet.model.api.responses.SendTransferResponse;
import org.iota.wallet.model.api.responses.error.NetworkError;
import org.iota.wallet.ui.adapter.WalletAddressCardAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WalletAddressesFragment extends BaseSwipeRefreshLayoutFragment implements WalletTabFragment.OnFabClickListener {

    private static final String ADDRESSES_LIST = "addresses";
    private FragmentWalletAddressesBinding addressBinding;
    private RecyclerView recyclerView;
    private List<String> addresses;
    private boolean isNewAddressGenerating = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        addressBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_addresses, container, false);
        View view = addressBinding.getRoot();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.wallet_addresses_swipe_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.wallet_addresses_recycler_view);

        return view;
    }

    private void setAdapter() {
        if (addresses == null) {
            addresses = new ArrayList<>();
            addresses = Utils.getCachedList(getActivity(), String.class, Constants.PREFERENCES_ADDRESS_CACHING);
        }

        WalletAddressCardAdapter adapter = new WalletAddressCardAdapter(getActivity(), addresses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        addressBinding.setAddresses(addresses);
    }

    private void generateNewAddress() {
        isNewAddressGenerating = true;
        TaskManager rt = new TaskManager(getActivity());
        GetNewAddressRequest gtr = new GetNewAddressRequest(0, false, 0, false);
        gtr.setSeed(String.valueOf(IOTA.seed));
        rt.startNewRequestTask(gtr);
    }

    private void attachNewAddress(String address) {
        //0 value transfer is required to attachToTangle
        TaskManager rt = new TaskManager(getActivity());
        SendTransferRequest tir = new SendTransferRequest(address, "0", "", Constants.NEW_ADDRESS_TAG);
        rt.startNewRequestTask(tir);
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    @Subscribe
    public void onEvent(GetNewAddressResponse gnar) {
        swipeRefreshLayout.setRefreshing(false);

        if (isNewAddressGenerating) {
            attachNewAddress(gnar.getAddresses().get(0));
            isNewAddressGenerating = false;
        } else {
            addresses = gnar.getAddresses();
            //remove last address because its not attached
            addresses.remove(addresses.size() - 1);
            //reverse to show lat added first
            Collections.reverse(addresses);

            setAdapter();

            Utils.setCachedList(getActivity(), Constants.PREFERENCES_ADDRESS_CACHING, addresses);
        }
    }

    @Subscribe
    public void onEvent(SendTransferResponse sendTransferResponse) {
        if (Arrays.asList(sendTransferResponse.getSuccessfully()).contains(true))
            getAddresses();
    }

    private void getAddresses() {
        isNewAddressGenerating = false;
        TaskManager rt = new TaskManager(getActivity());
        GetNewAddressRequest gna = new GetNewAddressRequest(0, true, 0, true);
        gna.setSeed(String.valueOf(IOTA.seed));
        gna.setShouldAttachAddress(false);
        rt.startNewRequestTask(gna);
    }

    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {
        if (nodeInfoResponse.getLatestMilestoneIndex() == (nodeInfoResponse.getLatestSolidSubtangleMilestoneIndex())) {
            getAddresses();
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_not_fully_synced_yet), Snackbar.LENGTH_LONG).show();
        }
    }

    private void getNodeInfo() {
        TaskManager rt = new TaskManager(getActivity());
        NodeInfoRequest nir = new NodeInfoRequest();
        rt.startNewRequestTask(nir);

        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    public void onFabClick() {
        generateNewAddress();
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        swipeRefreshLayout.setRefreshing(false);
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                getNodeInfo();
                break;
            case REMOTE_NODE_ERROR:
                addresses.clear();
                setAdapter();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (addresses != null)
            outState.putStringArrayList(ADDRESSES_LIST, (ArrayList<String>) addresses);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (addresses == null) {
                addresses = new ArrayList<>();
            }
            addresses = savedInstanceState.getStringArrayList(ADDRESSES_LIST);
        }
        setAdapter();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        getNodeInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        getNodeInfo();
    }
}
