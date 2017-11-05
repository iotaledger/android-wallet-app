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

import android.app.NotificationManager;
import android.content.Context;

import org.iota.wallet.R;
import org.iota.wallet.api.requests.ApiRequest;
import org.iota.wallet.api.requests.SendTransferRequest;
import org.iota.wallet.api.responses.ApiResponse;
import org.iota.wallet.api.responses.SendTransferResponse;
import org.iota.wallet.api.responses.error.NetworkError;
import org.iota.wallet.api.responses.error.NetworkErrorType;
import org.iota.wallet.helper.Constants;
import org.iota.wallet.helper.NotificationHelper;
import org.iota.wallet.helper.Utils;

import java.util.Arrays;

import jota.IotaAPI;
import jota.error.InvalidAddressException;
import jota.error.InvalidSecurityLevelException;
import jota.error.InvalidTransferException;
import jota.error.InvalidTrytesException;
import jota.error.NotEnoughBalanceException;

public class SendTransferRequestHandler extends IotaRequestHandler {
    public SendTransferRequestHandler(IotaAPI apiProxy, Context context) {
        super(apiProxy, context);
    }

    @Override
    public Class<? extends ApiRequest> getType() {
        return SendTransferRequest.class;
    }

    @Override
    public ApiResponse handle(ApiRequest request) {
        int notificationId = Utils.createNewID();
        ApiResponse response;
        // if we generate a new address the tag == address
        if (((SendTransferRequest) request).getValue().equals("0")
                && ((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
            NotificationHelper.requestNotification(context,
                    R.drawable.ic_add, context.getString(R.string.notification_attaching_new_address_request_title), notificationId);
        } else {
            NotificationHelper.requestNotification(context,
                    R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_request_title), notificationId);
        }

        try {
            response = new SendTransferResponse(apiProxy.sendTransfer(((SendTransferRequest) request).getSeed(),
                    ((SendTransferRequest) request).getSecurity(),
                    ((SendTransferRequest) request).getDepth(),
                    ((SendTransferRequest) request).getMinWeightMagnitude(),
                    ((SendTransferRequest) request).prepareTransfer(),
                    //inputs
                    null,
                    //remainder address
                    null));
        } catch (NotEnoughBalanceException | InvalidSecurityLevelException | InvalidTrytesException | InvalidAddressException | InvalidTransferException | IllegalAccessError e) {
            NetworkError error = new NetworkError();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notificationId);

            if (((SendTransferRequest) request).getValue().equals("0")
                    && ((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
                NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_failed_title), notificationId);

            } else {
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
            }

            if (e instanceof IllegalAccessError) {
                error.setErrorType(NetworkErrorType.ACCESS_ERROR);
                mNotificationManager.cancel(notificationId);
                if (((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG))
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_address_attach_to_tangle_blocked_title), notificationId);
                else
                    NotificationHelper.responseNotification(context, R.drawable.ic_error, context.getString(R.string.notification_transfer_attach_to_tangle_blocked_title), notificationId);
            } else
                error.setErrorType(NetworkErrorType.NETWORK_ERROR);

            response = error;
        }

        if (response instanceof SendTransferResponse && ((SendTransferRequest) request).getValue().equals("0")
                && ((SendTransferRequest) request).getTag().equals(Constants.NEW_ADDRESS_TAG)) {
            if (Arrays.asList(((SendTransferResponse) response).getSuccessfully()).contains(true))
                NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_succeeded_title), notificationId);
            else
                NotificationHelper.responseNotification(context, R.drawable.ic_address, context.getString(R.string.notification_attaching_new_address_response_failed_title), notificationId);

        } else if (response instanceof SendTransferResponse) {
            if (Arrays.asList(((SendTransferResponse) response).getSuccessfully()).contains(true))
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_succeeded_title), notificationId);
            else
                NotificationHelper.responseNotification(context, R.drawable.ic_fab_send, context.getString(R.string.notification_send_transfer_response_failed_title), notificationId);
        }

        return response;
    }
}
