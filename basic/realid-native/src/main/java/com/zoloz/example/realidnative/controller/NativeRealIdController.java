/*
 * Copyright (c) 2020 ZOLOZ PTE.LTD.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zoloz.example.realidnative.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.zoloz.api.sdk.client.OpenApiClient;
import com.zoloz.example.realidnative.autoconfig.ProductConfig;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * client mode controller
 *
 * @author Zhongyang MA
  */
@CrossOrigin
@RestController
@RequestMapping(value = {"/api"})
public class NativeRealIdController {

    private static final Logger logger = LoggerFactory.getLogger(NativeRealIdController.class);

    @Autowired
    private OpenApiClient openApiClient;

    @Autowired
    private ProductConfig realIdConfig;

    @RequestMapping(value = {"/realid/initialize","/realIdDemoService/initialize"}, method = RequestMethod.POST)
    public JSONObject realIdInit(@RequestBody JSONObject request) {

        if (logger.isInfoEnabled()) {
            logger.info("request=" + request);
        }

        String metaInfo = request.getString("metaInfo");
        String businessId = "dummy_bizid_" + System.currentTimeMillis();
        String userId = "dummy_userid_" + System.currentTimeMillis();

        JSONObject apiReq = new JSONObject();
        apiReq.put("bizId", businessId);
        apiReq.put("flowType", "REALIDLITE_KYC");
        apiReq.put("docType", realIdConfig.getDocType());
        if(request.get("docType") != null){
            apiReq.put("docType",request.get("docType"));
        }

        JSONObject productConfig = new JSONObject();
        ArrayList<CheckItem> pageInfoCheck = new ArrayList<>();
        JSONArray pageInfoCheckItem = request.getJSONArray("pageInfoCheck");
        if (pageInfoCheckItem != null && pageInfoCheckItem.size() > 0) {
            for (int i = 0; i < pageInfoCheckItem.size(); i++) {
                String item = (String) pageInfoCheckItem.get(i);
                CheckItem checkItem = new CheckItem();
                checkItem.setName(item);
                pageInfoCheck.add(checkItem);
            }
            productConfig.put("pageInfoCheck", pageInfoCheck);
        }
        if (request.get("preciseTamperCheck") != null) {
            productConfig.put("preciseTamperCheck", request.get("preciseTamperCheck"));
        }
        if (productConfig.containsKey("pageInfoCheck") || productConfig.containsKey("preciseTamperCheck")) {
            apiReq.put("productConfig", productConfig);
        }

        // use server side configured default pages
        // apiReq.put("pages", "1");
        apiReq.put("metaInfo", metaInfo);
        apiReq.put("userId", userId);
        if(StringUtils.isNotBlank(realIdConfig.getServiceLevel())){
            apiReq.put("serviceLevel",realIdConfig.getServiceLevel());
        }

        if(request.get("serviceLevel") != null){
            apiReq.put("serviceLevel",request.get("serviceLevel"));
        }


        String apiRespStr = openApiClient.callOpenApi(
                "v1.zoloz.realid.initialize",
                JSON.toJSONString(apiReq)
        );

        JSONObject apiResp = JSON.parseObject(apiRespStr);

        JSONObject response = new JSONObject(apiResp);
        response.put("rsaPubKey", openApiClient.getOpenApiPublicKey());
        response.put("transactionId", apiResp.getString("transactionId"));
        response.put("clientCfg", apiResp.getString("clientCfg"));
        if (logger.isInfoEnabled()) {
            logger.info("response=" + apiRespStr);
        }

        return response;
    }

    @RequestMapping(value = "/realid/checkresult", method = RequestMethod.POST)
    public JSONObject realIdCheck(@RequestBody JSONObject request) {

        if (logger.isInfoEnabled()) {
            logger.info("request=" + request);
        }

        String businessId = "dummy_bizid_" + System.currentTimeMillis();
        String transactionId = request.getString("transactionId");
        String isReturnImage = request.getString("isReturnImage");

        JSONObject apiReq = new JSONObject();
        apiReq.put("bizId", businessId);
        apiReq.put("transactionId", transactionId);
        apiReq.put("isReturnImage", isReturnImage);

        String apiRespStr = openApiClient.callOpenApi(
                "v1.zoloz.realid.checkresult",
                JSON.toJSONString(apiReq)
        );

        JSONObject apiResp = JSON.parseObject(apiRespStr);

        JSONObject response = new JSONObject(apiResp);
        return response;
    }


    @RequestMapping(value = "/privacyinfo/delete", method = RequestMethod.POST)
    public JSONObject privacyInfoDelete(@RequestBody JSONObject request) {

        if (logger.isInfoEnabled()) {
            logger.info("request=" + request);
        }

        String businessId = "dummy_bizid_" + System.currentTimeMillis();
        String transactionId = request.getString("transactionId");

        JSONObject apiReq = new JSONObject();
        apiReq.put("bizId", businessId);
        apiReq.put("transactionId", transactionId);

        String apiRespStr = openApiClient.callOpenApi(
                "v1.zoloz.privacyinfo.delete",
                JSON.toJSONString(apiReq)
        );

        JSONObject apiResp = JSON.parseObject(apiRespStr);

        JSONObject response = new JSONObject(apiResp);
        return response;
    }

    @Data
    public static class CheckItem{
        private String name;
    }
}
