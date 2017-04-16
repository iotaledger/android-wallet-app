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
        Transaction transaction = getItem(position - 1);

        holder.setIsRecyclable(true);

        holder.hashLabel.setText(transaction.getHash());
        holder.signatureFragmentsLabel.setText(transaction.getSignatureFragments());
        holder.addressLabel.setText(transaction.getAddress());
        if (NumberUtils.isNumber(String.valueOf(transaction.getValue()))) {
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
        holder.currentIndexLabel.setText(String.valueOf(transaction.getCurrentIndex()));
        holder.lastIndexLabel.setText(String.valueOf(transaction.getLastIndex()));
        holder.bundleLabel.setText(transaction.getBundle());
        holder.trunkTransactionLabel.setText(transaction.getTrunkTransaction());
        holder.branchTransactionLabel.setText(transaction.getBranchTransaction());
        holder.nonceLabel.setText(transaction.getNonce());
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

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView hashLabel;
        final TextView signatureFragmentsLabel;
        final TextView addressLabel;
        final TextView valueLabel;
        final TextView alternativeValueLabel;
        final TextView tagLabel;
        final TextView timestampLabel;
        final TextView currentIndexLabel;
        final TextView lastIndexLabel;
        final TextView bundleLabel;
        final TextView trunkTransactionLabel;
        final TextView branchTransactionLabel;
        final TextView nonceLabel;
        final TextView persistenceLabel;
        final ImageButton expandButton;
        final ExpandableRelativeLayout expandableLayout;

        private ViewHolder(View itemView) {
            super(itemView);

            hashLabel = (TextView) itemView.findViewById(R.id.item_es_hash);
            signatureFragmentsLabel = (TextView) itemView.findViewById(R.id.item_es_signatureFragments);
            addressLabel = (TextView) itemView.findViewById(R.id.item_es_address);
            valueLabel = (TextView) itemView.findViewById(R.id.item_es_value);
            alternativeValueLabel = (TextView) itemView.findViewById(R.id.item_es_alternate_value);
            tagLabel = (TextView) itemView.findViewById(R.id.item_es_tag);
            timestampLabel = (TextView) itemView.findViewById(R.id.item_es_timestamp);
            currentIndexLabel = (TextView) itemView.findViewById(R.id.item_es_currentIndex);
            lastIndexLabel = (TextView) itemView.findViewById(R.id.item_es_lastIndex);
            bundleLabel = (TextView) itemView.findViewById(R.id.item_es_bundle);
            trunkTransactionLabel = (TextView) itemView.findViewById(R.id.item_es_trunkTransaction);
            branchTransactionLabel = (TextView) itemView.findViewById(R.id.item_es_branchTransaction);
            nonceLabel = (TextView) itemView.findViewById(R.id.item_es_nonce);
            persistenceLabel = (TextView) itemView.findViewById(R.id.item_es_persistence);
            expandButton = (ImageButton) itemView.findViewById(R.id.item_es_expand_button);
            expandableLayout = (ExpandableRelativeLayout) itemView.findViewById(R.id.item_es_expand_layout);
            expandableLayout.collapse();
            expandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandableLayout.toggle();
                }
            });
        }
    }
}