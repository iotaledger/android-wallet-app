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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.iota.wallet.R;
import org.iota.wallet.model.NodeInfo;

import java.util.List;

public class NodeInfoListAdapter extends ArrayAdapter<NodeInfo> {

    public NodeInfoListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public NodeInfoListAdapter(Context context, int resource, List<NodeInfo> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolderItem viewHolder;
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_node_info, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.paramTextView = v.findViewById(R.id.item_info_label);
            viewHolder.valueTextView = v.findViewById(R.id.item_info_value);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        NodeInfo nodeInfo = getItem(position);
        if (nodeInfo != null) {
            if (viewHolder.paramTextView != null) {
                viewHolder.paramTextView.setText(nodeInfo.getParam());
            }
            if (viewHolder.valueTextView != null) {
                if (nodeInfo.getValue() != null)
                    viewHolder.valueTextView.setText(nodeInfo.getValue());
                else viewHolder.valueTextView.setText(String.valueOf(nodeInfo.getIndex()));
            }
        }
        return v;
    }

    private static class ViewHolderItem {
        TextView paramTextView;
        TextView valueTextView;
    }
}
