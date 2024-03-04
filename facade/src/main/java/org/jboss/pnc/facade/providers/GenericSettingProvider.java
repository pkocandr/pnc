/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.PncStatus;
import org.jboss.pnc.dto.notification.GenericSettingNotification;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.jboss.util.Strings;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Instant;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;

@PermitAll
@Stateless
@Slf4j
public class GenericSettingProvider {
    public static final String ANNOUNCEMENT_BANNER = "ANNOUNCEMENT_BANNER";
    public static final String MAINTENANCE_MODE = "MAINTENANCE_MODE";
    public static final String PNC_VERSION = "PNC_VERSION";
    public static final String ANNOUNCEMENT_ETA = "ANNOUNCEMENT_ETA";

    @Inject
    private GenericSettingRepository genericSettingRepository;

    @Inject
    private UserService userService;

    @Inject
    private Notifier notifier;

    @Deprecated
    public GenericSettingProvider() {
    }

    @RolesAllowed(USERS_ADMIN)
    public void activateMaintenanceMode() {

        log.info("Activating Maintenance mode");
        GenericSetting maintenanceMode = createGenericParameterIfNotFound(MAINTENANCE_MODE);

        maintenanceMode.setValue(Boolean.TRUE.toString());
        genericSettingRepository.save(maintenanceMode);
    }

    @RolesAllowed(USERS_ADMIN)
    public void deactivateMaintenanceMode() {

        log.info("Deactivating Maintenance mode");
        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        if (maintenanceMode == null) {
            maintenanceMode = new GenericSetting();
            maintenanceMode.setKey(MAINTENANCE_MODE);
        }

        maintenanceMode.setValue(Boolean.FALSE.toString());
        genericSettingRepository.save(maintenanceMode);
    }

    public boolean isInMaintenanceMode() {

        GenericSetting maintenanceMode = genericSettingRepository.queryByKey(MAINTENANCE_MODE);

        if (maintenanceMode == null) {
            return false;
        } else {
            return Boolean.parseBoolean(maintenanceMode.getValue());
        }
    }

    public boolean isCurrentUserAllowedToTriggerBuilds() {

        boolean isLoggedInUserSystemUser = userService.hasLoggedInUserRole(USERS_ADMIN);
        if (isLoggedInUserSystemUser) {
            log.info("Users with system-role are always allowed to trigger builds");
            return true;
        }

        return !isInMaintenanceMode();
    }

    @RolesAllowed(USERS_ADMIN)
    public void setPNCVersion(String version) {

        log.info("PNC System version set to: '{}'", version);
        GenericSetting pncVersion = createGenericParameterIfNotFound(PNC_VERSION);
        pncVersion.setValue(version);
        genericSettingRepository.save(pncVersion);
    }

    public String getPNCVersion() {

        GenericSetting pncVersion = genericSettingRepository.queryByKey(PNC_VERSION);

        if (pncVersion == null) {
            return Strings.EMPTY;
        } else {
            return pncVersion.getValue();
        }
    }

    @RolesAllowed(USERS_ADMIN)
    public void setAnnouncementBanner(String banner) {

        log.info("Announcement banner set to: '{}'", banner);
        GenericSetting announcementBanner = createGenericParameterIfNotFound(ANNOUNCEMENT_BANNER);
        announcementBanner.setValue(banner);
        genericSettingRepository.save(announcementBanner);
    }

    public String getAnnouncementBanner() {

        GenericSetting announcementBanner = genericSettingRepository.queryByKey(ANNOUNCEMENT_BANNER);

        if (announcementBanner == null) {
            return Strings.EMPTY;
        } else {
            return announcementBanner.getValue();
        }
    }

    @RolesAllowed(USERS_ADMIN)
    public void clearAnnouncementBanner() {
        clearByKey(ANNOUNCEMENT_BANNER);
    }

    public PncStatus getPncStatus() {
        GenericSetting banner = genericSettingRepository.queryByKey(ANNOUNCEMENT_BANNER);
        GenericSetting eta = genericSettingRepository.queryByKey(ANNOUNCEMENT_ETA);

        return PncStatus.builder()
                .banner(banner == null ? null : banner.getValue())
                .eta(eta == null ? null : Instant.parse(eta.getValue()))
                .isMaintenanceMode(isInMaintenanceMode())
                .build();
    }

    @RolesAllowed(USERS_ADMIN)
    public void setEta(String eta) {
        log.info("ETA set to: '{}'", eta);
        GenericSetting pncEta = createGenericParameterIfNotFound(ANNOUNCEMENT_ETA);
        pncEta.setValue(eta);
        genericSettingRepository.save(pncEta);
    }

    @RolesAllowed(USERS_ADMIN)
    public void clearEta() {
        clearByKey(ANNOUNCEMENT_ETA);
    }

    public void notifyListeners() {
        notifier.sendMessage(GenericSettingNotification.pncStatusChanged(getPncStatus()));
    }

    private GenericSetting createGenericParameterIfNotFound(String key) {

        GenericSetting genericSetting = genericSettingRepository.queryByKey(key);

        if (genericSetting == null) {
            genericSetting = new GenericSetting();
            genericSetting.setKey(key);
        }

        return genericSetting;
    }

    private void clearByKey(String key) {
        GenericSetting dbEntry = genericSettingRepository.queryByKey(key);
        if (dbEntry != null) {
            genericSettingRepository.delete(dbEntry.getId());
        }
    }
}
