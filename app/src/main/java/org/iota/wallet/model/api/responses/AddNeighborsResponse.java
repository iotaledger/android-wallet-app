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

public class AddNeighborsResponse extends ApiResponse {

    private Integer addedNeighbors;

    public AddNeighborsResponse(jota.dto.response.AddNeighborsResponse apiResponse) {
        addedNeighbors = apiResponse.getAddedNeighbors();
        setDuration(apiResponse.getDuration());
    }

    public Integer getAddedNeighbors() {
        return addedNeighbors;
    }

    public void setAddedNeighbors(Integer addedNeighbors) {
        this.addedNeighbors = addedNeighbors;
    }
}