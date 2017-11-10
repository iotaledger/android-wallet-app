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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.iota.wallet.R;
import org.iota.wallet.model.NodeInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NodeInfoListAdapter extends ArrayAdapter<NodeInfo> {

    private LayoutInflater inflator;

    public NodeInfoListAdapter(Activity context, int resource, List<NodeInfo> items) {
        super(context, resource, items);
        inflator = context.getLayoutInflater();
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (view == null) {
            view = inflator.inflate(R.layout.item_node_info, parent, false);
            viewHolder = new ViewHolderItem(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) view.getTag();
        }

        NodeInfo nodeInfo = getItem(position);
        if (nodeInfo != null) {
            if (viewHolder.label != null) {
                viewHolder.label.setText(nodeInfo.getParam());
            }
            if (viewHolder.value != null) {
                if (nodeInfo.getValue() != null)
                    viewHolder.value.setText(nodeInfo.getValue());
                else viewHolder.value.setText(String.valueOf(nodeInfo.getIndex()));
            }
        }
        return view;
    }

    public static class ViewHolderItem {
        @BindView(R.id.item_info_label)
        TextView label;
        @BindView(R.id.item_info_value)
        TextView value;

        private ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
