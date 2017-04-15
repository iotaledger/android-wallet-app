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

import org.iota.wallet.model.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jota.model.Bundle;
import jota.model.Transaction;

/**
 * Created by Adrian on 28.04.2016.
 */
public class GetTransferResponse extends ApiResponse {

    private List<Transfer> transfers = new ArrayList<>();

    public GetTransferResponse(jota.dto.response.GetTransferResponse apiResponse) {

        Bundle[] transferBundle = apiResponse.getTransfers();

        if (transferBundle != null) {
            for (Bundle aTransferBundle : transferBundle) {

                for (Transaction trx : aTransferBundle.getTransactions()) {
                    long timestamp = trx.getTimestamp();
                    String address = trx.getAddress();
                    String hash = trx.getHash();
                    Boolean persistence = trx.getPersistence();
                    long value = trx.getValue();
                    String tag = trx.getTag();

                    transfers.add(new Transfer(timestamp, address, hash, persistence, value, "", tag));
                }
            }
            Collections.sort(transfers);
            setDuration(apiResponse.getDuration());
        }
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }

}
