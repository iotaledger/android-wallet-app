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

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.greenrobot.eventbus.Subscribe;
import org.iota.wallet.R;
import org.iota.wallet.api.TaskManager;
import org.iota.wallet.api.requests.NodeInfoRequest;
import org.iota.wallet.api.responses.NodeInfoResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.helper.Utils;
import org.iota.wallet.model.NodeInfo;
import org.iota.wallet.ui.adapter.NodeInfoListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class NodeInfoFragment extends BaseSwipeRefreshLayoutFragment implements OnChartValueSelectedListener {

    private List<NodeInfo> nodeInfos;
    @BindView(R.id.node_info_toolbar)
    Toolbar nodeInfoToolbar;
    @BindView(R.id.iri_info_list)
    ListView list;
    @BindView(R.id.node_info_chart)
    PieChart chart;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node_info, container, false);
        unbinder = ButterKnife.bind(this, view);
        swipeRefreshLayout = view.findViewById(R.id.node_info_swipe_container);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).setSupportActionBar(nodeInfoToolbar);
        chart.setNoDataText(getString(R.string.messages_no_chart_data));
        chart.setNoDataTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        chart.setEntryLabelColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        Paint p = chart.getPaint(Chart.PAINT_INFO);
        p.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        initializeChart();
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
    public void onResume() {
        super.onResume();
        getNodeInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden)
            getNodeInfo();
    }

    private void getNodeInfo() {
        TaskManager rt = new TaskManager(getActivity());
        NodeInfoRequest nir = new NodeInfoRequest();
        rt.startNewRequestTask(nir);
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    @Subscribe
    public void onEvent(NodeInfoResponse nir) {
        swipeRefreshLayout.setRefreshing(false);
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        nodeInfos = new ArrayList<>();
        nodeInfos.add(new NodeInfo(getString(R.string.info_app_name), nir.getAppName()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_app_version), nir.getAppVersion()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_jre_version), nir.getJreVersion()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_max_processors), nir.getJreAvailableProcessors() + ""));
        nodeInfos.add(new NodeInfo(getString(R.string.info_free_memory), Formatter.formatShortFileSize(getActivity(), nir.getJreFreeMemory())));
        nodeInfos.add(new NodeInfo(getString(R.string.info_max_memory), Formatter.formatShortFileSize(getActivity(), nir.getJreMaxMemory())));
        nodeInfos.add(new NodeInfo(getString(R.string.info_total_memory), Formatter.formatShortFileSize(getActivity(), nir.getJreTotalMemory())));
        nodeInfos.add(new NodeInfo(getString(R.string.info_latest_milestone), nir.getLatestMilestone()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_latest_milestone_index), nir.getLatestMilestoneIndex()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_latest_milestone_solid_subtangle), nir.getLatestSolidSubtangleMilestone()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_latest_milestone_solid_subtangle_index), nir.getLatestSolidSubtangleMilestoneIndex()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_neighbors), nir.getNeighbors()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_packets_queue_size), nir.getPacketsQueueSize()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_time), df.format(new Date(nir.getTime()))));
        nodeInfos.add(new NodeInfo(getString(R.string.info_tips), nir.getTips()));
        nodeInfos.add(new NodeInfo(getString(R.string.info_transactions_to_request), nir.getTransactionsToRequest()));

        setAdapter();

        updateChart(nir.getTips(), nir.getTransactionsToRequest());
    }

    @Subscribe
    public void onEvent(NetworkError error) {
        switch (error.getErrorType()) {
            case REMOTE_NODE_ERROR:
                swipeRefreshLayout.setRefreshing(false);
                chart.setVisibility(View.INVISIBLE);
                nodeInfos.clear();
                setAdapter();
                break;
        }
    }

    @Override
    public void onRefresh() {
        getNodeInfo();
    }

    private void setAdapter() {
        NodeInfoListAdapter nodeInfoListAdapter = new NodeInfoListAdapter(getActivity(), R.layout.item_node_info, nodeInfos);
        Utils.fixListView(list, swipeRefreshLayout);
        list.setAdapter(nodeInfoListAdapter);
    }

    private void initializeChart() {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(generateCenterSpannableText());
        chart.setExtraOffsets(15.f, 15.f, 15.f, 15.f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void updateChart(long tips, long transactionsToRequest) {
        chart.setVisibility(View.VISIBLE);
        setData(tips, transactionsToRequest);
        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }


    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString(getString(R.string.transactions));
        s.setSpan(new RelativeSizeSpan(0.75f), 0, 0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorAccent)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    private void setData(long tips, long transactionsToRequest) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(tips, getString(R.string.tips) + " " + "(" + tips + ")"));
        entries.add(new PieEntry(transactionsToRequest, getString(R.string.transactions_to_request) + " " + "(" + transactionsToRequest + ")"));

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.transactions) + "\n(" + (tips + transactionsToRequest) + ")");

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        dataSet.setColors(colors);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);
        chart.invalidate();
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }
}
