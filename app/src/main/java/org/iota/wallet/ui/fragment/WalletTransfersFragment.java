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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.databinding.FragmentWalletTransfersBinding;
import org.iota.wallet.model.Transfer;
import org.iota.wallet.model.api.requests.GetTransfersRequest;
import org.iota.wallet.model.api.requests.NodeInfoRequest;
import org.iota.wallet.model.api.requests.SendTransferRequest;
import org.iota.wallet.model.api.responses.GetTransferResponse;
import org.iota.wallet.model.api.responses.NodeInfoResponse;
import org.iota.wallet.model.api.responses.SendTransferResponse;
import org.iota.wallet.model.api.responses.error.NetworkError;
import org.iota.wallet.ui.adapter.WalletTransfersCardAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalletTransfersFragment extends BaseSwipeRefreshLayoutFragment implements WalletTabFragment.OnFabClickListener {

    private static final String TRANSFERS_LIST = "transfers";
    private FragmentWalletTransfersBinding transferBinding;
    private WalletTransfersCardAdapter adapter;
    private RecyclerView recyclerView;
    private List<Transfer> transfers;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        transferBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_transfers, container, false);
        View view = transferBinding.getRoot();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.wallet_transfers_swipe_container);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_wallet).setChecked(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.wallet_transfers_recycler_view);

        return view;
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                getNodeInfo();
                break;
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                transfers.clear();
                setAdapter();
                break;
        }
    }

    private void getTransfers() {
        TaskManager rt = new TaskManager(getActivity());
        GetTransfersRequest gtr = new GetTransfersRequest();
        rt.startNewRequestTask(gtr);

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
    public void onEvent(SendTransferRequest tir) {
        TaskManager rt = new TaskManager(getActivity());
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
    public void onEvent(GetTransferResponse transferResponse) {
        swipeRefreshLayout.setRefreshing(false);

        //TODO show a bundle card instead of all transfers as a card
        transfers = transferResponse.getTransfers();

        adapter.setAdapterList(transfers);

        setAdapter();
    }

    @Subscribe
    public void onEvent(SendTransferResponse sendTransferResponse) {
        if (Arrays.asList(sendTransferResponse.getSuccessfully()).contains(true))
            getTransfers();
    }

    @Subscribe
    public void onEvent(NodeInfoResponse nodeInfoResponse) {

        if (nodeInfoResponse.getLatestMilestoneIndex() == (nodeInfoResponse.getLatestSolidSubtangleMilestoneIndex())) {
            getTransfers();

        } else {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_not_fully_synced_yet), Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAdapter() {
        if (transfers == null) {
            transfers = new ArrayList<>();
        }

        adapter = new WalletTransfersCardAdapter(getActivity(), transfers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setAdapterList(transfers);

        transferBinding.setTransfers(transfers);
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
        Fragment fragment = new NewTransferFragment();
        getActivity().getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.container, fragment, null)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (transfers != null)
            outState.putParcelableArrayList(TRANSFERS_LIST, (ArrayList<Transfer>) transfers);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (transfers == null)
                transfers = new ArrayList<>();
            transfers = savedInstanceState.getParcelableArrayList(TRANSFERS_LIST);
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
