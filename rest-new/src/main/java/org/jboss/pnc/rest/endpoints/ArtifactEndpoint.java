/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints;

import javax.inject.Inject;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.ArtifactProvider;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ArtifactEndpoint extends AbstractEndpoint<Artifact, ArtifactRef> implements
        org.jboss.pnc.rest.api.endpoints.ArtifactEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEndpoint.class);
    private ArtifactProvider artifactProvider;

    @Inject
    public ArtifactEndpoint(ArtifactProvider provider) {
        super(provider, Artifact.class);
        this.artifactProvider = provider;
    }

    @Override
    public Page<Artifact> getAll(PageParameters pageParams, String sha256, String md5, String sha1) {
        logger.debug("Retrieving Artifacts with these " + pageParams.toString() + "and checksums:" +
                ((sha256 == null) ? "" : " Sha256: " + sha256) +
                ((sha256 == null) ? "" : " Sha256: " + sha256) +
                ((sha1 == null) ? "" : " Sha1: " + sha1));
        return artifactProvider.getAll(pageParams.getPageIndex(),pageParams.getPageSize(), pageParams.getSort(),pageParams.getQ(),
                Optional.ofNullable(sha256),
                Optional.ofNullable(md5),
                Optional.ofNullable(sha1)
        );
    }

    @Override
    public Artifact getSpecific(int id) {
        return super.getSpecific(id);
    }
}
