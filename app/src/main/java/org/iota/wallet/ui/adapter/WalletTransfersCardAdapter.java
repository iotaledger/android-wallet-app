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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import org.iota.wallet.R;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.helper.price.AlternateValueManager;
import org.iota.wallet.helper.price.AlternateValueUtils;
import org.iota.wallet.helper.price.ExchangeRateNotAvailableException;
import org.iota.wallet.model.Transfer;
import org.iota.wallet.ui.dialog.WalletTransfersItemDialog;
import org.knowm.xchange.currency.Currency;

import java.util.List;

import jota.utils.IotaUnitConverter;

public class WalletTransfersCardAdapter extends RecyclerView.Adapter<WalletTransfersCardAdapter.ViewHolder> {

    private final Context context;
    private final SparseBooleanArray expandState = new SparseBooleanArray();
    private List<Transfer> transfers;

    public WalletTransfersCardAdapter(Context context, List<Transfer> listItems) {
        this.context = context;
        this.transfers = listItems;
        for (int i = 0; i < listItems.size(); i++) {
            expandState.append(i, false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_wallet_transfer, parent, false);
        return new ViewHolder(v);
    }

    private Transfer getItem(int position) {
        return transfers.get(position + 1);
    }

    public void setAdapterList(List<Transfer> transfers) {
        this.transfers = transfers;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Transfer transfer = getItem(position - 1);

        holder.setIsRecyclable(true);

        holder.valueLabel.setText(IotaUnitConverter.convertRawIotaAmountToDisplayText(transfer.getValue(), false));
        holder.addressLabel.setText(transfer.getAddress());
        holder.messageLabel.setText(TextUtils.isEmpty(transfer.getMessage()) ? "-" : transfer.getMessage());
        holder.tagLabel.setText(transfer.getTag());
        holder.timeLabel.setText(Utils.timeStampToDate(transfer.getTimestamp()));
        holder.hashLabel.setText(transfer.getHash());
        holder.persistenceLabel.setText(context.getResources().getString(
                transfer.getPersistence() ? R.string.card_label_persistence_yes :
                        R.string.card_label_persistence_no));
        try {
            Currency currency = Utils.getConfiguredAlternateCurrency(context);
            String text = AlternateValueUtils.formatAlternateBalanceText(
                    new AlternateValueManager(context).convert(transfer.getValue(), currency), currency);
            holder.alternativeValueLabel.setText(text);
        } catch (ExchangeRateNotAvailableException e) {
            holder.alternativeValueLabel.setText(R.string.na);
        }

        if (transfer.getValue() < 0) {
            holder.valueLabel.setTextColor(ContextCompat.getColor(context, R.color.flatRed));
        } else if (transfer.getValue() > 0) {
            holder.valueLabel.setTextColor(ContextCompat.getColor(context, R.color.flatGreen));
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

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView valueLabel;
        final TextView alternativeValueLabel;
        final TextView addressLabel;
        final TextView messageLabel;
        final TextView tagLabel;
        final TextView timeLabel;
        final TextView hashLabel;
        final TextView persistenceLabel;
        final ImageButton expandButton;
        final ExpandableRelativeLayout expandableLayout;

        private ViewHolder(View itemView) {
            super(itemView);

            valueLabel = itemView.findViewById(R.id.item_wt_value);
            alternativeValueLabel = itemView.findViewById(R.id.item_wt_alternate_value);
            addressLabel = itemView.findViewById(R.id.item_wt_address);
            messageLabel = itemView.findViewById(R.id.item_wt_message);
            tagLabel = itemView.findViewById(R.id.item_wt_tag);
            timeLabel = itemView.findViewById(R.id.item_wt_time);
            hashLabel = itemView.findViewById(R.id.item_wt_hash);
            persistenceLabel = itemView.findViewById(R.id.item_wt_persistence);
            expandButton = itemView.findViewById(R.id.item_wt_expand_button);
            expandableLayout = itemView.findViewById(R.id.item_wt_expand_layout);
            expandableLayout.collapse();

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            expandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandableLayout.toggle();
                }
            });
        }

        @Override
        public void onClick(final View v) {
            Bundle bundle = new Bundle();
            bundle.putString("address", addressLabel.getText().toString());
            bundle.putString("hash", hashLabel.getText().toString());

            WalletTransfersItemDialog dialog = new WalletTransfersItemDialog();
            dialog.setArguments(bundle);
            dialog.show(((Activity) context).getFragmentManager(), null);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Toast.makeText(v.getContext(), context.getString(R.string.messages_not_yet_implemented), Toast.LENGTH_SHORT).show();

            }
            return true;
        }
    }
}