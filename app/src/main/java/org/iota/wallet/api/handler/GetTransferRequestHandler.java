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

package org.iota.wallet.api.handler;

import android.content.Context;

import org.iota.wallet.model.api.requests.ApiRequest;
import org.iota.wallet.model.api.requests.GetTransfersRequest;
import org.iota.wallet.model.api.responses.ApiResponse;
import org.iota.wallet.model.api.responses.GetTransferResponse;
import org.iota.wallet.model.api.responses.error.NetworkError;

import jota.IotaAPI;
import jota.error.ArgumentException;
import jota.error.InvalidAddressException;
import jota.error.InvalidBundleException;
import jota.error.InvalidSecurityLevelException;
import jota.error.InvalidSignatureException;
import jota.error.NoInclusionStatesException;
import jota.error.NoNodeInfoException;

public class GetTransferRequestHandler extends IotaRequestHandler {
    public GetTransferRequestHandler(IotaAPI iotaApi, Context context) {
        super(iotaApi, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return GetTransfersRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        ApiResponse response;

        try {
            response = new GetTransferResponse(apiProxy.getTransfers(((GetTransfersRequest) request).getSeed(),
                    ((GetTransfersRequest) request).getSecurity(),
                    ((GetTransfersRequest) request).getStart(),
                    ((GetTransfersRequest) request).getEnd(),
                    ((GetTransfersRequest) request).isInclusionState()));
        } catch (ArgumentException | InvalidSecurityLevelException | InvalidAddressException | InvalidBundleException | InvalidSignatureException | NoNodeInfoException | NoInclusionStatesException e) {
            response = new NetworkError();
        }
        return response;
    }
}
