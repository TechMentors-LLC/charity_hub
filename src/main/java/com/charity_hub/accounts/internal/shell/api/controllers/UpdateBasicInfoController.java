package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo.UpdateBasicInfo;
import com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo.UpdateBasicInfoHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.BasicResponse;
import com.charity_hub.accounts.internal.shell.api.dtos.UpdateBasicInfoRequest;
import com.charity_hub.shared.api.DeferredResults;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class UpdateBasicInfoController {
    private final UpdateBasicInfoHandler updateBasicInfoHandler;

    public UpdateBasicInfoController(UpdateBasicInfoHandler updateBasicInfoHandler) {
        this.updateBasicInfoHandler = updateBasicInfoHandler;
    }

    @PostMapping("/v1/accounts/update-basic-info")
    public DeferredResult<ResponseEntity<?>> handle(@RequestBody UpdateBasicInfoRequest request, @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        UpdateBasicInfo command = new UpdateBasicInfo(accessTokenPayload.getUserId(), accessTokenPayload.getDeviceId(), request.fullName(), request.photoUrl());

        return DeferredResults.from(
                updateBasicInfoHandler.handle(command)
                        .thenApply(accessToken -> ResponseEntity.ok(new BasicResponse(accessToken)))
        );
    }
}
