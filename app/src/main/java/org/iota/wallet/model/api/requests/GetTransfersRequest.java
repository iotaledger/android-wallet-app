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

package org.iota.wallet.model.api.requests;

import org.iota.wallet.IOTA;

/**
 * Created by Adrian on 29.04.2016.
 */
public class GetTransfersRequest extends ApiRequest {

    private String seed;
    private int security = 2;
    private int start = 0;
    private int end = 0;
    private boolean inclusionState = true;

    public GetTransfersRequest() {
        this.seed = String.valueOf(IOTA.seed);
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public boolean isInclusionState() {
        return inclusionState;
    }

    public void setInclusionState(boolean inclusionState) {
        this.inclusionState = inclusionState;
    }
}
