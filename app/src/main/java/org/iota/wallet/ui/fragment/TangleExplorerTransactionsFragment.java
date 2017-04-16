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

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.ui.adapter.TangleExplorerTransactionsCardAdapter;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.databinding.FragmentTangleExplorerTransactionsBinding;
import org.iota.wallet.model.Transaction;
import org.iota.wallet.model.api.requests.CoolTransationsRequest;
import org.iota.wallet.model.api.responses.CoolTransactionResponse;
import org.iota.wallet.model.api.responses.error.NetworkError;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TangleExplorerTransactionsFragment extends BaseSwipeRefreshLayoutFragment implements SearchView.OnQueryTextListener, TextView.OnEditorActionListener {

    private static final String SEARCH_TEXT = "searchText";
    private static final String TRANSACTIONS_LIST = "transactions";
    private final Runnable getCoolTransactions = new Runnable() {
        public void run() {
            TaskManager rt = new TaskManager(getActivity());
            rt.startNewRequestTask(new CoolTransationsRequest());

            if (!swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
            }
        }
    };
    private FragmentTangleExplorerTransactionsBinding coolTransactionsBinding;
    private FastScrollRecyclerView recyclerView;
    private List<Transaction> transactions;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private SearchView searchView;
    private TangleExplorerTransactionsCardAdapter adapter;
    private String savedSearchText = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable android.os.Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        super.setHasOptionsMenu(true);
        coolTransactionsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tangle_explorer_transactions, container, false);
        View view = coolTransactionsBinding.getRoot();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.tangle_explorer_transactions_swipe_container);
        swipeRefreshLayout.setEnabled(false);

        recyclerView = (FastScrollRecyclerView) view.findViewById(R.id.tangle_explorer_transactions_fast_recycler_view);

        return view;
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tangle_explorer_transactions, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        searchView.setOnQueryTextListener(this);
        this.searchView = searchView;

        //focus the SearchView
        if (savedSearchText != null && !savedSearchText.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(savedSearchText, true);
            searchView.setIconified(false);
            searchView.clearFocus();
        }

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        adapter.setAdapterList(transactions);
                        addExecutor();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        removeExecutor();
                        return true;
                    }
                });
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        swipeRefreshLayout.setRefreshing(false);
        /*if (coolTransactions != null) coolTransactions.clear();
        setAdapter();*/
        switch (error.getErrorType()) {
            case NETWORK_ERROR:
                transactions.clear();
                break;
            default:
                break;
        }
    }

    private void setAdapter() {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        adapter = new TangleExplorerTransactionsCardAdapter(getActivity(), transactions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        coolTransactionsBinding.setTransactions(transactions);
    }

    @Override
    public void onResume() {
        super.onResume();
        addExecutor();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            addExecutor();
        } else
            removeExecutor();
    }

    @Override
    public void onPause() {
        super.onPause();
        removeExecutor();
    }

    private void removeExecutor() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private void addExecutor() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(getCoolTransactions, 0, 5, TimeUnit.SECONDS);
    }

    @Subscribe
    public void onEvent(CoolTransactionResponse transactionResponse) {
        swipeRefreshLayout.setRefreshing(false);
        if (transactions != null) {
            this.transactions = Arrays.asList(transactionResponse.getTransactions());
            coolTransactionsBinding.setTransactions(transactions);
            adapter.setAdapterList(transactions);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT, searchView == null ? "" : searchView.getQuery().toString().isEmpty() ? "" : searchView.getQuery().toString());
        if (transactions != null) {
            String jsonCurProduct = new Gson().toJson(transactions);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            prefs.edit().putString(TRANSACTIONS_LIST, jsonCurProduct).apply();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (transactions == null) {
                transactions = new ArrayList<>();
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String jsonPreferences = prefs.getString(TRANSACTIONS_LIST, "");

            Type type = new TypeToken<List<Transaction>>() {
            }.getType();
            transactions = new Gson().fromJson(jsonPreferences, type);

            prefs.edit().remove(TRANSACTIONS_LIST).apply();

            if (savedSearchText != null)
                savedSearchText = savedInstanceState.getString(SEARCH_TEXT);

            if (savedSearchText != null && !savedSearchText.isEmpty())
                if (searchView != null)
                    searchView.setQuery(savedInstanceState.getString(SEARCH_TEXT), false);
        }
        setAdapter();
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            return true;
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String searchText) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {
        adapter.filter(transactions, searchText);
        return true;
    }
}
