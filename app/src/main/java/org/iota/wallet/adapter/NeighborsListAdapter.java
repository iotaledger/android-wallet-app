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

package org.iota.wallet.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.model.Neighbor;
import org.iota.wallet.model.api.requests.RemoveNeighborsRequest;

import java.util.ArrayList;
import java.util.List;

public class NeighborsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Neighbor> neighbors;

    public NeighborsListAdapter(Context context, List<Neighbor> neighborList) {
        this.context = context;
        this.neighbors = neighborList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_neighborlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;
            Neighbor neighbor = getItem(position);
            if (neighbor != null) {
                if (vh.ipAddressTextView != null) {
                    vh.ipAddressTextView.setText(neighbor.getAddress());
                }
                if (vh.numberOfAllTransactionsTextView != null && vh.numberOfInvalidTransactionsTextView != null && vh.numberOfNewTransactionsTextView != null) {
                    if (neighbor.isOnline()) {
                        vh.statusView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.indicator_online, null));
                        if (neighbor.getNumberOfAllTransactions() != null && neighbor.getNumberOfInvalidTransactions() != null && neighbor.getNumberOfNewTransactions() != null) {
                            vh.numberOfAllTransactionsTextView.setText(context.getString(R.string.all_transactions) + " " + neighbor.getNumberOfAllTransactions());
                            vh.numberOfInvalidTransactionsTextView.setText(context.getString(R.string.invalid_transactions) + " " + neighbor.getNumberOfInvalidTransactions());
                            vh.numberOfNewTransactionsTextView.setText(context.getString(R.string.new_transactions) + " " + neighbor.getNumberOfNewTransactions());

                        } else {
                            vh.numberOfAllTransactionsTextView.setText(context.getString(R.string.all_transactions) + " " + context.getString(R.string.na));
                            vh.numberOfInvalidTransactionsTextView.setText(context.getString(R.string.invalid_transactions) + " " + context.getString(R.string.na));
                        }
                    } else {
                        vh.statusView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.indicator_offline, null));
                        vh.numberOfInvalidTransactionsTextView.setText("-");
                        vh.numberOfAllTransactionsTextView.setText("-");
                        vh.numberOfNewTransactionsTextView.setText("-");
                    }
                }
            }

        }

    }

    @Override
    public int getItemCount() {
        return neighbors.size();
    }

    private Neighbor getItem(int position) {
        return neighbors.get(position);
    }

    public void removeItem(Context context, int position) {
        TaskManager rt = new TaskManager(context);
        RemoveNeighborsRequest rnr = new RemoveNeighborsRequest(new String[]{Constants.UDP + neighbors.get(position).getAddress()});
        rt.startNewRequestTask(rnr);

        neighbors.remove(position);
        notifyItemRemoved(position);
    }

    public void setAdapterList(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
        notifyDataSetChanged();
    }

    public void filter(final List<Neighbor> neighbors, String searchText) {
        final String sText = searchText.toLowerCase();

        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<Neighbor> filteredNeighborList = new ArrayList<>();
                for (Neighbor neighbor : neighbors) {
                    final String text = neighbor.getAddress().toLowerCase();
                    if (text.contains(sText)) {
                        filteredNeighborList.add(neighbor);
                    }
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapterList(filteredNeighborList);

                    }
                });

            }
        }).start();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        final TextView ipAddressTextView;
        final TextView numberOfAllTransactionsTextView;
        final TextView numberOfInvalidTransactionsTextView;
        final TextView numberOfNewTransactionsTextView;
        final ImageView statusView;

        private ViewHolder(View itemView) {
            super(itemView);

            this.ipAddressTextView = (TextView) itemView.findViewById(R.id.item_neighbor_address);
            this.numberOfAllTransactionsTextView = (TextView) itemView.findViewById(R.id.item_neighbor_number_of_all_transactions);
            this.numberOfInvalidTransactionsTextView = (TextView) itemView.findViewById(R.id.item_neighbor_number_of_invalid_transactions);
            this.numberOfNewTransactionsTextView = (TextView) itemView.findViewById(R.id.item_neighbor_number_of_new_transactions);
            this.statusView = (ImageView) itemView.findViewById(R.id.item_neighbor_status);
        }
    }
}