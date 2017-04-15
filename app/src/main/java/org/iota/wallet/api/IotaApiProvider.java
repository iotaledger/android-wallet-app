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

package org.iota.wallet.api;

import android.content.Context;

import org.iota.wallet.api.handler.AddNeighborsRequestHandler;
import org.iota.wallet.api.handler.CoolTransactionsRequestHandler;
import org.iota.wallet.api.handler.FindTransactionsRequestHandler;
import org.iota.wallet.api.handler.GetBalancesRequestHandler;
import org.iota.wallet.api.handler.GetBundleRequestHandler;
import org.iota.wallet.api.handler.GetInputsRequestHandler;
import org.iota.wallet.api.handler.GetNeighborsRequestHandler;
import org.iota.wallet.api.handler.GetNewAddressRequestHandler;
import org.iota.wallet.api.handler.GetTransferRequestHandler;
import org.iota.wallet.api.handler.NodeInfoRequestHandler;
import org.iota.wallet.api.handler.RemoveNeighborsRequestHandler;
import org.iota.wallet.api.handler.ReplayBundleRequestHandler;
import org.iota.wallet.api.handler.RequestHandler;
import org.iota.wallet.api.handler.SendTransferRequestHandler;
import org.iota.wallet.model.api.requests.ApiRequest;
import org.iota.wallet.model.api.responses.ApiResponse;
import org.iota.wallet.model.api.responses.error.NetworkError;
import org.iota.wallet.model.api.responses.error.NetworkErrorType;

import java.util.HashMap;
import java.util.Map;

import jota.IotaAPI;
import jota.pow.JCurl;

public class IotaApiProvider implements ApiProvider {
    private final IotaAPI iotaApi;
    private final Context context;
    private Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap;

    public IotaApiProvider(String host, int port, Context context) {
        JCurl curl = new JCurl();
        this.iotaApi = new IotaAPI.Builder().protocol("http").host(host).port(((Integer) port).toString()).withCustomCurl(curl).build();
        this.context = context;
        loadRequestMap();
    }

    private void loadRequestMap() {
        Map<Class<? extends ApiRequest>, RequestHandler> requestHandlerMap = new HashMap<>();

        AddNeighborsRequestHandler addNeighborsAction = new AddNeighborsRequestHandler(iotaApi, context);
        CoolTransactionsRequestHandler coolTransactionsAction = new CoolTransactionsRequestHandler(iotaApi, context);
        FindTransactionsRequestHandler findTransactionsAction = new FindTransactionsRequestHandler(iotaApi, context);
        GetBalancesRequestHandler getBalancesAction = new GetBalancesRequestHandler(iotaApi, context);
        GetBundleRequestHandler getBundleAction = new GetBundleRequestHandler(iotaApi, context);
        GetInputsRequestHandler getInputsAction = new GetInputsRequestHandler(iotaApi, context);
        GetNeighborsRequestHandler getNeighborsAction = new GetNeighborsRequestHandler(iotaApi, context);
        GetNewAddressRequestHandler getNewAddressAction = new GetNewAddressRequestHandler(iotaApi, context);
        GetTransferRequestHandler getTransferAction = new GetTransferRequestHandler(iotaApi, context);
        RemoveNeighborsRequestHandler removeNeighborsAction = new RemoveNeighborsRequestHandler(iotaApi, context);
        ReplayBundleRequestHandler replayBundleAction = new ReplayBundleRequestHandler(iotaApi, context);
        SendTransferRequestHandler sendTransferAction = new SendTransferRequestHandler(iotaApi, context);
        NodeInfoRequestHandler nodeInfoAction = new NodeInfoRequestHandler(iotaApi, context);

        requestHandlerMap.put(addNeighborsAction.getType(), addNeighborsAction);
        requestHandlerMap.put(coolTransactionsAction.getType(), coolTransactionsAction);
        requestHandlerMap.put(findTransactionsAction.getType(), findTransactionsAction);
        requestHandlerMap.put(getBalancesAction.getType(), getBalancesAction);
        requestHandlerMap.put(getBundleAction.getType(), getBundleAction);
        requestHandlerMap.put(getInputsAction.getType(), getInputsAction);
        requestHandlerMap.put(getNeighborsAction.getType(), getNeighborsAction);
        requestHandlerMap.put(getNewAddressAction.getType(), getNewAddressAction);
        requestHandlerMap.put(getTransferAction.getType(), getTransferAction);
        requestHandlerMap.put(removeNeighborsAction.getType(), removeNeighborsAction);
        requestHandlerMap.put(replayBundleAction.getType(), replayBundleAction);
        requestHandlerMap.put(sendTransferAction.getType(), sendTransferAction);
        requestHandlerMap.put(nodeInfoAction.getType(), nodeInfoAction);

        this.requestHandlerMap = requestHandlerMap;
    }

    @Override
    public ApiResponse processRequest(ApiRequest apiRequest) {
        ApiResponse response = null;

        try {
            if (this.requestHandlerMap.containsKey(apiRequest.getClass())) {
                RequestHandler requestHandler = this.requestHandlerMap.get(apiRequest.getClass());
                response = requestHandler.handle(apiRequest);
            }
        } catch (IllegalAccessError e) {
            NetworkError error = new NetworkError();
            error.setErrorType(NetworkErrorType.ACCESS_ERROR);
            response = error;
            e.printStackTrace();
        } catch (Exception e) {
            response = new NetworkError();
        }
        return response == null ? new NetworkError() : response;
    }
}
