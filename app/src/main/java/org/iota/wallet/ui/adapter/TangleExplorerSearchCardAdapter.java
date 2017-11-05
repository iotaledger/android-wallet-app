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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import org.apache.commons.lang3.math.NumberUtils;
import org.iota.wallet.R;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.helper.price.AlternateValueManager;
import org.iota.wallet.helper.price.AlternateValueUtils;
import org.iota.wallet.helper.price.ExchangeRateNotAvailableException;
import org.iota.wallet.model.Transaction;
import org.knowm.xchange.currency.Currency;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jota.utils.IotaUnitConverter;

public class TangleExplorerSearchCardAdapter extends RecyclerView.Adapter<TangleExplorerSearchCardAdapter.ViewHolder> {

    private final List<Transaction> transactions;
    private final Context context;
    private final SparseBooleanArray expandState = new SparseBooleanArray();

    public TangleExplorerSearchCardAdapter(Context context, List<Transaction> listItems) {
        this.context = context;
        this.transactions = listItems;
        for (int i = 0; i < listItems.size(); i++) {
            expandState.append(i, false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tangle_explorer_search, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Transaction transaction = getItem(adapterPosition - 1);

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

        holder.expandableLayout.setExpanded(expandState.get(adapterPosition));
        holder.expandableLayout.invalidate();
    }

    private Transaction getItem(int position) {
        return transactions.get(position + 1);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_es_hash)
        TextView hashLabel;
        @BindView(R.id.item_es_address)
        TextView addressLabel;
        @BindView(R.id.item_es_value)
        TextView valueLabel;
        @BindView(R.id.item_es_alternate_value)
        TextView alternativeValueLabel;
        @BindView(R.id.item_es_tag)
        TextView tagLabel;
        @BindView(R.id.item_es_timestamp)
        TextView timestampLabel;
        @BindView(R.id.item_es_bundle)
        TextView bundleLabel;
        @BindView(R.id.item_es_persistence)
        TextView persistenceLabel;
        @BindView(R.id.item_es_expand_button)
        ImageButton expandButton;
        @BindView(R.id.item_es_expand_layout)
        ExpandableRelativeLayout expandableLayout;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            expandableLayout.collapse();
            expandButton.setOnClickListener(view -> expandableLayout.toggle());
            expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
                @Override
                public void onPreOpen() {
                    expandButton.setImageResource(R.drawable.ic_expand_less);
                    expandState.put(getAdapterPosition(), true);
                }

                @Override
                public void onPreClose() {
                    expandButton.setImageResource(R.drawable.ic_expand_more);
                    expandState.put(getAdapterPosition(), false);
                }
            });
        }
    }
}