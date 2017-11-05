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

import butterknife.BindView;
import butterknife.ButterKnife;
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
        int adapterPosition = holder.getAdapterPosition();
        Transfer transfer = getItem(adapterPosition - 1);

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
        } else
            holder.valueLabel.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));

        holder.expandableLayout.setExpanded(expandState.get(adapterPosition));
        holder.expandableLayout.invalidate();
    }

    @Override
    public int getItemCount() {
        return transfers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_wt_value)
        TextView valueLabel;
        @BindView(R.id.item_wt_alternate_value)
        TextView alternativeValueLabel;
        @BindView(R.id.item_wt_address)
        TextView addressLabel;
        @BindView(R.id.item_wt_message)
        TextView messageLabel;
        @BindView(R.id.item_wt_tag)
        TextView tagLabel;
        @BindView(R.id.item_wt_time)
        TextView timeLabel;
        @BindView(R.id.item_wt_hash)
        TextView hashLabel;
        @BindView(R.id.item_wt_persistence)
        TextView persistenceLabel;
        @BindView(R.id.item_wt_expand_button)
        ImageButton expandButton;
        @BindView(R.id.item_wt_expand_layout)
        ExpandableRelativeLayout expandableLayout;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            expandableLayout.collapse();

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("address", addressLabel.getText().toString());
                bundle.putString("hash", hashLabel.getText().toString());

                WalletTransfersItemDialog dialog = new WalletTransfersItemDialog();
                dialog.setArguments(bundle);
                dialog.show(((Activity) context).getFragmentManager(), null);
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Toast.makeText(v.getContext(), context.getString(R.string.messages_not_yet_implemented), Toast.LENGTH_SHORT).show();

                }
                return true;
            });

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