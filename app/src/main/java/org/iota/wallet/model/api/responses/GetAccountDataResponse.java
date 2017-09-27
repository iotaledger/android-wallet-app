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

import jota.error.InvalidAddressException;
import jota.model.Bundle;
import jota.model.Transaction;
import jota.utils.Checksum;

public class GetAccountDataResponse extends ApiResponse {

    private List<String> addresses = new ArrayList<>();
    private List<Transfer> transfers = new ArrayList<>();
    private long balance;

    public GetAccountDataResponse(jota.dto.response.GetAccountDataResponse apiResponse) throws InvalidAddressException {

        setAddresses(apiResponse.getAddresses());
        Collections.reverse(addresses);

        Bundle[] transferBundle = apiResponse.getTransfers();

        if (transferBundle != null) {
            for (Bundle aTransferBundle : transferBundle) {

                long totalValue = 0;
                long timestamp = 0;
                String address;
                String hash = null;
                Boolean persistence = null;
                long value;
                String tag = null;
                String destinationAddress = "";

                for (Transaction trx : aTransferBundle.getTransactions()) {

                    address = trx.getAddress();
                    persistence = trx.getPersistence();
                    value = trx.getValue();

                    if (value != 0 && addresses.contains(Checksum.addChecksum(address)))
                        totalValue += value;

                    if (trx.getCurrentIndex() == 0) {
                        timestamp = trx.getAttachmentTimestamp() / 1000;
                        tag = trx.getTag();
                        destinationAddress = address;
                        hash = trx.getHash();

                    }

                }
                transfers.add(new Transfer(timestamp, destinationAddress, hash, persistence, totalValue, "", tag));

            }
            Collections.sort(transfers);

            setBalance(apiResponse.getBalance());
            setDuration(apiResponse.getDuration());
        }
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

}
