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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.FindTransactionRequest;
import org.iota.wallet.api.requests.GetBundleRequest;
import org.iota.wallet.api.responses.FindTransactionResponse;
import org.iota.wallet.api.responses.GetBundleResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.Transaction;
import org.iota.wallet.ui.adapter.TangleExplorerSearchCardAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jota.utils.InputValidator;

public class TangleExplorerSearchFragment extends BaseSwipeRefreshLayoutFragment implements SearchView.OnQueryTextListener, TextView.OnEditorActionListener {

    private static final String SEARCH_TEXT = "searchText";
    private static final String TRANSACTIONS_LIST = "transactions";

    @BindView(R.id.tangle_explorer_search_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    private List<Transaction> transactions;
    private InputMethodManager inputManager;
    private TangleExplorerSearchCardAdapter adapter;
    private SearchView searchView;
    private boolean trigger = false;
    private String hash = "";
    private String savedSearchText = "";

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tangle_explorer_search, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.tangle_explorer_search_swipe_container);
        return view;
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
        hash = bundle.getString(Constants.TANGLE_EXPLORER_SEARCH_ITEM);
        tvEmpty.setVisibility(transactions.size() == 0 ? View.VISIBLE : View.GONE);
        if (this.transactions != null) this.transactions.clear();
        trigger = true;
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tangle_explorer_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        if (trigger) {
            searchItem.expandActionView();
            searchView.setQuery(hash, false);
            trigger = false;
            searchTransactions();

            final View view = getView();
            if (view != null) {
                view.postDelayed(() -> inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0), 50);
            }
        }

        //focus the SearchView
        if (savedSearchText != null && !savedSearchText.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(savedSearchText, true);
            searchView.setIconified(false);
            searchView.clearFocus();
        }


        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;

            }
        });
    }

    @Subscribe
    public void onEvent(FindTransactionResponse ftr) {
        if (ftr != null && ftr.getHashes().length > 0) {
            TaskManager rt = new TaskManager(getActivity());
            GetBundleRequest gtr = new GetBundleRequest(ftr.getHashes()[0]);
            rt.startNewRequestTask(gtr);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_trx_not_found), Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onEvent(GetBundleResponse getBundleResponse) {
        swipeRefreshLayout.setRefreshing(false);

        transactions = getBundleResponse.getTransactions();

        setAdapter();

        if (transactions.isEmpty()) {
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_bundle_not_found), Snackbar.LENGTH_LONG).show();
        }
    }

    private void setAdapter() {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        adapter = new TangleExplorerSearchCardAdapter(getActivity(), transactions);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(transactions.size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void searchTransactions() {

        if (transactions != null)
            transactions.clear();
        if (adapter != null)
            adapter.notifyDataSetChanged();

        inputManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        if (searchView.getQuery().toString().isEmpty() || !InputValidator.isTrytes(searchView.getQuery().toString(), searchView.getQuery().toString().length())) {
            Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_invalid_search), Snackbar.LENGTH_LONG)
                    .setAction(null, null).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        TaskManager rt = new TaskManager(getActivity());

        if (!trigger) {
            GetBundleRequest gbr = new GetBundleRequest(searchView.getQuery().toString());
            rt.startNewRequestTask(gbr);
        } else {
            FindTransactionRequest ftr = new FindTransactionRequest(searchView.getQuery().toString());
            rt.startNewRequestTask(ftr);
        }

        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        }
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case NETWORK_ERROR:
            case INVALID_HASH_ERROR:
            case IOTA_COOL_NETWORK_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                if (transactions != null)
                    transactions.clear();
                if (adapter != null)
                    adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onRefresh() {
        searchTransactions();
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            searchTransactions();
            return true;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT, searchView == null ? "" : searchView.getQuery().toString().isEmpty() ? "" : searchView.getQuery().toString());
        if (transactions != null)
            outState.putParcelableArrayList(TRANSACTIONS_LIST, (ArrayList<Transaction>) transactions);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (transactions == null) {
                transactions = new ArrayList<>();
            }
            transactions = savedInstanceState.getParcelableArrayList(TRANSACTIONS_LIST);

            if (savedSearchText != null)
                savedSearchText = savedInstanceState.getString(SEARCH_TEXT);

            if (savedSearchText != null && !savedSearchText.isEmpty())
                if (searchView != null)
                    searchView.setQuery(savedInstanceState.getString(SEARCH_TEXT), false);
        }
        setAdapter();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchTransactions();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
