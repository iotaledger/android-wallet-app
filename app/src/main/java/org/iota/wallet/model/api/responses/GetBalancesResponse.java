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

package org.iota.wallet.model.api.responses;

/**
 * Created by pinpong on 21.10.16.
 */

public class GetBalancesResponse extends ApiResponse {

    private String[] balances;
    private String milestone;
    private Integer milestoneIndex;


    public GetBalancesResponse(jota.dto.response.GetBalancesResponse apiResponse) {
        balances = apiResponse.getBalances();
        milestone = apiResponse.getMilestone();
        milestoneIndex = apiResponse.getMilestoneIndex();
        setDuration(apiResponse.getDuration());
    }

    public String[] getBalances() {
        return balances;
    }

    public void setBalances(String[] balances) {
        this.balances = balances;
    }


    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public Integer getMilestoneIndex() {
        return milestoneIndex;
    }

    public void setMilestoneIndex(Integer milestoneIndex) {
        this.milestoneIndex = milestoneIndex;
    }

}
