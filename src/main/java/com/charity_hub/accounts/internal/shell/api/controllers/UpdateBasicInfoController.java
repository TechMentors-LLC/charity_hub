package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo.UpdateBasicInfo;
import com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo.UpdateBasicInfoHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.BasicResponse;
import com.charity_hub.accounts.internal.shell.api.dtos.UpdateBasicInfoRequest;
import com.charity_hub.shared.auth.AccessTokenPayload;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateBasicInfoController {
    private static final Logger log = LoggerFactory.getLogger(UpdateBasicInfoController.class);
    private final UpdateBasicInfoHandler updateBasicInfoHandler;

    public UpdateBasicInfoController(UpdateBasicInfoHandler updateBasicInfoHandler) {
        this.updateBasicInfoHandler = updateBasicInfoHandler;
    }

    @PostMapping("/v1/accounts/update-basic-info")
    @Timed(value = "charity_hub.accounts.update_basic_info", description = "Time taken to update basic info")
    @Observed(name = "accounts.update_basic_info", contextualName = "update-basic-info")
    public ResponseEntity<BasicResponse> handle(@RequestBody UpdateBasicInfoRequest request, @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        log.info("Updating basic info for user: {}", accessTokenPayload.getUserId());
        UpdateBasicInfo command = new UpdateBasicInfo(accessTokenPayload.getUserId(), accessTokenPayload.getDeviceId(), request.fullName(), request.photoUrl());
        String accessToken = updateBasicInfoHandler.handle(command);
        log.info("Basic info updated successfully for user: {}", accessTokenPayload.getUserId());
        return ResponseEntity.ok(new BasicResponse(accessToken));
    }
}
