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
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.iota.wallet.R;
import org.iota.wallet.model.Address;
import org.iota.wallet.ui.dialog.WalletAddressesItemDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalletAddressCardAdapter extends RecyclerView.Adapter<WalletAddressCardAdapter.ViewHolder> {

    private final Context context;
    private List<Address> addresses;

    public WalletAddressCardAdapter(Context context, List<Address> listItems) {
        this.context = context;
        this.addresses = listItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_wallet_address, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Address address = getItem(adapterPosition - 1);

        holder.setIsRecyclable(false);

        holder.addressLabel.setText(address.getAddress());

        if (address.isUsed()) {
            holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.flatRed));
            holder.addressLabel.setPaintFlags(holder.addressLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else if (!address.isUsed()) {
            holder.addressImage.setColorFilter(ContextCompat.getColor(context, R.color.flatGreen));
        }
    }

    private Address getItem(int position) {
        return addresses.get(position + 1);
    }

    public void setAdapterList(List<Address> addresses) {
        this.addresses = addresses;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_wa_address)
        TextView addressLabel;
        @BindView(R.id.item_wa_address_image)
        ImageView addressImage;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("address", addressLabel.getText().toString());

                WalletAddressesItemDialog dialog = new WalletAddressesItemDialog();
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

        }
    }
}
