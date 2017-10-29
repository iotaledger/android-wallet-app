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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.apache.commons.lang3.math.NumberUtils;
import org.greenrobot.eventbus.EventBus;
import org.iota.wallet.R;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.helper.price.AlternateValueManager;
import org.iota.wallet.helper.price.AlternateValueUtils;
import org.iota.wallet.helper.price.ExchangeRateNotAvailableException;
import org.iota.wallet.model.Transaction;
import org.knowm.xchange.currency.Currency;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import jota.utils.IotaUnitConverter;

public class TangleExplorerTransactionsCardAdapter extends RecyclerView.Adapter<TangleExplorerTransactionsCardAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final Context context;
    private final SparseBooleanArray expandState = new SparseBooleanArray();
    private List<Transaction> transactions;

    public TangleExplorerTransactionsCardAdapter(Context context, List<Transaction> listItems) {
        this.context = context;
        this.transactions = listItems;
        for (int i = 0; i < listItems.size(); i++) {
            expandState.append(i, false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tangle_explorer_transactions, parent, false);
        return new ViewHolder(v);
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(position + 1);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Transaction transaction = getItem(position - 1);

        holder.setIsRecyclable(true);

        holder.hashLabel.setText(transaction.getHash());
        holder.addressLabel.setText(transaction.getAddress());
        if (NumberUtils.isCreatable(String.valueOf(transaction.getValue()))) {
            holder.valueLabel.setText(IotaUnitConverter.convertRawIotaAmountToDisplayText(transaction.getValue(), false));
            try {
                Currency currency = Utils.getConfiguredAlternateCurrency(context);
                String text = AlternateValueUtils.formatAlternateBalanceText(
                        new AlternateValueManager(context).convert(transaction.getValue(), currency), currency);
                holder.alternativeValueLabel.setText(text);
            } catch (ExchangeRateNotAvailableException e) {
                holder.alternativeValueLabel.setText(R.string.na);
            }
            if (transaction.getValue() < 0) {
                holder.valueLabel.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
            } else if (transaction.getValue() > 0) {
                holder.valueLabel.setTextColor(ContextCompat.getColor(context, R.color.flatGreen));
            }

        } else {
            holder.valueLabel.setText(String.valueOf(transaction.getValue()));
        }
        holder.tagLabel.setText(transaction.getTag());
        holder.timestampLabel.setText(Utils.timeStampToDate(transaction.getTimestamp()));
        holder.bundleLabel.setText(transaction.getBundle());

        if (transaction.getPersistence() == null) {
            holder.persistenceLabel.setVisibility(View.GONE);
        } else {
            holder.persistenceLabel.setVisibility(View.VISIBLE);
            holder.persistenceLabel.setText(context.getResources().getString(
                    transaction.getPersistence() ? R.string.card_label_persistence_yes :
                            R.string.card_label_persistence_no));
        }

        holder.expandableLayout.setExpanded(expandState.get(position));
        holder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                holder.expandButton.setImageResource(R.drawable.ic_expand_less);
                expandState.put(holder.getAdapterPosition(), true);
            }

            @Override
            public void onPreClose() {
                holder.expandButton.setImageResource(R.drawable.ic_expand_more);
                expandState.put(holder.getAdapterPosition(), false);
            }
        });
        holder.expandableLayout.invalidate();

    }

    private Transaction getItem(int position) {
        return transactions.get(position + 1);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void setAdapterList(List<Transaction> coolTransactions) {
        this.transactions = coolTransactions;
        notifyDataSetChanged();
    }

    public void filter(final List<Transaction> coolTransactions, String searchText) {
        final String sText = searchText.toLowerCase();
        final List<Transaction> filteredCoolTransactionList = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    for (Transaction coolTransaction : coolTransactions) {

                        List<String> list = new ArrayList<>();
                    list.add(coolTransaction.getAddress().toLowerCase());
                    list.add(coolTransaction.getBundle().toLowerCase());
                    list.add(coolTransaction.getHash().toLowerCase());
                    list.add(String.valueOf(coolTransaction.getValue()).toLowerCase());
                    list.add(coolTransaction.getTag().toLowerCase());

                    for (String str : list) {
                        if (str.trim().contains(sText)) {
                            filteredCoolTransactionList.add(coolTransaction);
                            break;
                        }
                    }
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapterList(filteredCoolTransactionList);
                    }
                });
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView hashLabel;
        final TextView addressLabel;
        final TextView valueLabel;
        final TextView alternativeValueLabel;
        final TextView tagLabel;
        final TextView timestampLabel;
        final TextView bundleLabel;
        final TextView persistenceLabel;
        final ImageButton expandButton;
        final ExpandableRelativeLayout expandableLayout;

        private ViewHolder(View itemView) {
            super(itemView);

            hashLabel = itemView.findViewById(R.id.item_et_hash);
            addressLabel = itemView.findViewById(R.id.item_et_address);
            valueLabel = itemView.findViewById(R.id.item_et_value);
            alternativeValueLabel = itemView.findViewById(R.id.item_et_alternate_value);
            tagLabel = itemView.findViewById(R.id.item_et_tag);
            timestampLabel = itemView.findViewById(R.id.item_et_timestamp);
            bundleLabel = itemView.findViewById(R.id.item_et_bundle);
            persistenceLabel = itemView.findViewById(R.id.item_et_persistence);

            itemView.setOnClickListener(this);

            expandButton = itemView.findViewById(R.id.item_et_expand_button);
            expandableLayout = itemView.findViewById(R.id.item_et_expand_layout);
            expandableLayout.collapse();

            expandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandableLayout.toggle();
                }
            });
        }

        @Override
        public void onClick(final View v) {
            ViewPager viewPager = ((Activity) context).findViewById(R.id.tangle_explorer_tab_viewpager);
            viewPager.setCurrentItem(1);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TANGLE_EXPLORER_SEARCH_ITEM, hashLabel.getText().toString());
            EventBus.getDefault().post(bundle);
        }
    }
}