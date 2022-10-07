/*
 * Copyright 2022 zml
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.stellardrift.build.repository;

import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;

import javax.inject.Inject;

class StellardriftRepositoryExtensionImpl implements StellardriftRepositoryExtension {
    private static final String BASE_URL = "https://repo.stellardrift.ca/repository/";

    private static final String RELEASES_URL = BASE_URL + "internal/";
    private static final String OWN_RELEASES_URL = BASE_URL + "releases/";
    private static final String SNAPSHOTS_URL = BASE_URL + "snapshots/";

    private final RepositoryHandler repositories;

    @Inject
    public StellardriftRepositoryExtensionImpl(final RepositoryHandler handler) {
       this.repositories = handler;
    }

    @Override
    public MavenArtifactRepository releases() {
        return this.repositories.maven(repo -> {
            repo.setName("stellardriftReleases");
            repo.setUrl(RELEASES_URL);
            repo.mavenContent(MavenRepositoryContentDescriptor::releasesOnly);
        });
    }

    @Override
    public MavenArtifactRepository ownReleases() {
        return this.repositories.maven(repo -> {
            repo.setName("stellardriftOwnReleases");
            repo.setUrl(OWN_RELEASES_URL);
            repo.mavenContent(MavenRepositoryContentDescriptor::releasesOnly);
        });
    }

    @Override
    public MavenArtifactRepository snapshots() {
        return this.repositories.maven(repo -> {
            repo.setName("stellardriftSnapshots");
            repo.setUrl(SNAPSHOTS_URL);
            repo.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly);
        });
    }
}
