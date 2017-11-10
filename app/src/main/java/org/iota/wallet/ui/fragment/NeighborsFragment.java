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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.AddNeighborsRequest;
import org.iota.wallet.api.requests.GetNeighborsRequest;
import org.iota.wallet.api.responses.AddNeighborsResponse;
import org.iota.wallet.api.responses.GetNeighborsResponse;
import org.iota.wallet.api.responses.RemoveNeighborsResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.Neighbor;
import org.iota.wallet.ui.activity.MainActivity;
import org.iota.wallet.ui.adapter.NeighborsListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NeighborsFragment extends BaseSwipeRefreshLayoutFragment
        implements SearchView.OnQueryTextListener, TextView.OnEditorActionListener, MainActivity.OnBackPressedClickListener {

    private static final int AUTOMATICALLY_DISMISS_ITEM = 3000;
    private static final String SEARCH_TEXT = "searchText";
    private static final String NEIGHBORS_LIST = "neighbors";
    private static final String REAVEAL_VIEW_STATE = "revealViewState";
    private static final String NEW_ADDRESS_TEXT = "newAddress";

    @BindView(R.id.neighbor_toolbar)
    Toolbar neighborToolbar;
    @BindView(R.id.connected_neighbors)
    TextView neighborsHeaderTextView;
    @BindView(R.id.neighbor_recycler_view)
    RecyclerView recyclerView;
    private List<Neighbor> neighbors;
    @BindView(R.id.neighborImageFrameLayout)
    FrameLayout frameLayout;
    @BindView(R.id.fab_add_neighbor)
    FloatingActionButton fabAddButton;
    @BindView(R.id.reavel_linearlayout)
    FrameLayout revealView;
    @BindView(R.id.neighbor_edit_text_new_ip)
    EditText editTextNewAddress;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    private boolean isEditTextVisible;
    private InputMethodManager inputManager;
    private SearchView searchView;
    private String savedSearchText = "";

    private NeighborsListAdapter adapter;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_neighbors, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.neighbors_swipe_container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(neighborToolbar);
        neighbors = new ArrayList<>();
        fabAddButton.setVisibility(View.VISIBLE);

        revealView.setVisibility(View.INVISIBLE);
        isEditTextVisible = false;

        editTextNewAddress.setOnEditorActionListener(this);
    }

    @OnClick(R.id.fab_add_neighbor)
    public void onFabAddNeighborClick(FloatingActionButton fab) {
        if (!isEditTextVisible) {
            showRevealEditText(revealView);
            editTextNewAddress.requestFocus();
            inputManager.showSoftInput(editTextNewAddress, InputMethodManager.SHOW_IMPLICIT);
            fabAddButton.setImageResource(R.drawable.ic_done);
        } else {
            if (editTextNewAddress.getText().toString().isEmpty()) {
                Snackbar.make(fab, getString(R.string.messages_enter_neighbor_address), Snackbar.LENGTH_LONG)
                        .setAction(null, null).show();
                return;
            }
            for (Neighbor neighbor : neighbors) {
                String address = neighbor.getAddress();
                if (address.equals(editTextNewAddress.getText().toString())) {
                    Snackbar.make(fab, getString(R.string.messages_neighbor_address_forgiven), Snackbar.LENGTH_LONG)
                            .setAction(null, null).show();
                    return;
                }
            }
            addNeighbor();
        }
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onBackPressedClickListener() {
        if (revealView != null && revealView.isShown()) {
            editTextNewAddress.getText().clear();
            inputManager.hideSoftInputFromWindow(editTextNewAddress.getWindowToken(), 0);

            hideReavelEditText(revealView);
            fabAddButton.setImageResource(R.drawable.ic_add);
        } else
            getActivity().finish();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.neighbors, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(this);

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
                frameLayout.setVisibility(View.GONE);
                fabAddButton.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.setAdapterList(neighbors);
                frameLayout.setVisibility(View.VISIBLE);
                fabAddButton.setVisibility(View.VISIBLE);
                return true;

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getNeighbors();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            getNeighbors();
    }

    private void showRevealEditText(FrameLayout view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getRight() - 30;
            int cy = view.getBottom() - 60;
            int finalRadius = Math.max(view.getWidth(), view.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            view.setVisibility(View.VISIBLE);
            isEditTextVisible = true;
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
            isEditTextVisible = true;
        }

    }

    private void hideReavelEditText(final FrameLayout view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getRight() - 30;
            int cy = view.getBottom() - 60;
            int initialRadius = view.getWidth();

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.INVISIBLE);
                }
            });
            isEditTextVisible = false;
            anim.start();
        } else {
            view.setVisibility(View.INVISIBLE);
            isEditTextVisible = false;
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onEvent(GetNeighborsResponse gpr) {
        swipeRefreshLayout.setRefreshing(false);
        // clear all online states
        neighbors.clear();

        for (Neighbor neighbor : gpr.getNeighbors()) {
            neighbor.setOnline(true);
            int index;
            if ((index = gpr.getNeighbors().indexOf(neighbor)) <= -1) {
                // place online neighbors at the beginning
                neighbors.remove(index);
                neighbors.add(0, neighbor);
            } else {
                neighbors.add(0, neighbor);
            }
        }
        tvEmpty.setVisibility(neighbors.size() == 0 ? View.VISIBLE : View.GONE);
        neighborsHeaderTextView.setText(getString(R.string.menu_neighbors) + " (" + gpr.getNeighbors().size() + ")");
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case ACCESS_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                if (neighbors != null)
                    neighbors.clear();
                if (adapter != null)
                    adapter.notifyDataSetChanged();
                break;
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                if (neighbors != null)
                    neighbors.clear();
                setAdapter();
                break;
        }
    }

    @Subscribe
    public void onEvent(AddNeighborsResponse anr) {
        getNeighbors();
    }

    @Subscribe
    public void onEvent(RemoveNeighborsResponse rnr) {
        getNeighbors();
    }

    private void getNeighbors() {
        GetNeighborsRequest nar = new GetNeighborsRequest();
        TaskManager rt = new TaskManager(getActivity());
        rt.startNewRequestTask(nar);
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        }
    }

    @Override
    public void onRefresh() {
        getNeighbors();
    }

    private void setAdapter() {
        if (adapter == null) {
            adapter = new NeighborsListAdapter(getActivity(), neighbors);
            recyclerView.setAdapter(adapter);
            final SwipeToDismissTouchListener<RecyclerViewAdapter> touchListener =
                    new SwipeToDismissTouchListener<>(
                            new RecyclerViewAdapter(recyclerView),
                            new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>() {
                                @Override
                                public boolean canDismiss(int position) {
                                    return true;
                                }

                                @Override
                                public void onPendingDismiss(RecyclerViewAdapter recyclerView, int position) {
                                }

                                @Override
                                public void onDismiss(RecyclerViewAdapter view, int position) {
                                    adapter.removeItem(getActivity(), position);
                                }
                            });
            touchListener.setDismissDelay(AUTOMATICALLY_DISMISS_ITEM);
            recyclerView.setOnTouchListener(touchListener);
            // Setting this scroll listener is required to ensure that during ListView scrolling,
            // we don't look for swipes.
            recyclerView.addOnScrollListener((RecyclerView.OnScrollListener) touchListener.makeScrollListener());
            recyclerView.addOnItemTouchListener(new SwipeableItemClickListener(getActivity(), (view, position) -> {
                if (view.getId() == R.id.neighbor_item_delete) {
                    touchListener.processPendingDismisses();
                } else if (view.getId() == R.id.neighbor_item_undo) {
                    touchListener.undoPendingDismiss();
                }
            }));
        } else {
            adapter.notifyDataSetChanged();
        }
        getNeighbors();
    }

    @Override
    public boolean onQueryTextSubmit(String searchText) {
        return false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onQueryTextChange(String searchText) {
        adapter.filter(neighbors, searchText);
        neighborsHeaderTextView.setText(getString(R.string.menu_neighbors) + " (" + neighbors.size() + ")");
        return true;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((actionId == EditorInfo.IME_ACTION_DONE)
                || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
            if (editTextNewAddress.getText().toString().isEmpty()) {
                Snackbar.make(getActivity().findViewById(R.id.drawer_layout), getString(R.string.messages_enter_neighbor_address), Snackbar.LENGTH_LONG)
                        .setAction(null, null).show();
                return false;
            }
            addNeighbor();
        }
        return true;
    }

    private void addNeighbor() {
        Neighbor neighbor = new Neighbor(editTextNewAddress.getText().toString());
        neighbors.add(neighbor);

        TaskManager rt = new TaskManager(getActivity());
        AddNeighborsRequest anr = new AddNeighborsRequest(new String[]{Constants.UDP + editTextNewAddress.getText().toString()});

        rt.startNewRequestTask(anr);

        editTextNewAddress.getText().clear();
        inputManager.hideSoftInputFromWindow(editTextNewAddress.getWindowToken(), 0);
        hideReavelEditText(revealView);
        fabAddButton.setImageResource(R.drawable.ic_add);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT, searchView == null ? "" : searchView.getQuery().toString().isEmpty() ? "" : searchView.getQuery().toString());
        outState.putString(NEW_ADDRESS_TEXT, editTextNewAddress == null ? "" : editTextNewAddress.getText().toString().isEmpty() ? "" : editTextNewAddress.getText().toString());
        outState.putBoolean(REAVEAL_VIEW_STATE, revealView.isShown());

        if (neighbors != null)
            outState.putParcelableArrayList(NEIGHBORS_LIST, (ArrayList<Neighbor>) neighbors);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (neighbors == null) {
                neighbors = new ArrayList<>();
            }

            neighbors = savedInstanceState.getParcelableArrayList(NEIGHBORS_LIST);

            if (neighborsHeaderTextView != null)
                neighborsHeaderTextView.setText(getString(R.string.menu_neighbors) + " (" + neighbors.size() + ")");

            if (savedInstanceState.getBoolean(REAVEAL_VIEW_STATE)) {

                final View view = getView();
                if (view != null) {
                    view.postDelayed(() -> {
                        showRevealEditText(revealView);
                        editTextNewAddress.requestFocus();
                        editTextNewAddress.setText(savedInstanceState.getString(NEW_ADDRESS_TEXT));
                        inputManager.showSoftInput(editTextNewAddress, InputMethodManager.SHOW_IMPLICIT);
                        fabAddButton.setImageResource(R.drawable.ic_done);
                    }, 50);
                }
            }

            if (savedSearchText != null)
                savedSearchText = savedInstanceState.getString(SEARCH_TEXT);

            if (savedSearchText != null && !savedSearchText.isEmpty())
                if (searchView != null)
                    searchView.setQuery(savedInstanceState.getString(SEARCH_TEXT), false);
        }
        setAdapter();
    }
}
