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

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Extension to {@link RepositoryHandler} that allows easily declaring Stellardrift repos.
 *
 * @since 6.0.0
 */
public interface StellardriftRepositoryExtension {
    /**
     * Own and proxied releases.
     *
     * @return created repository for own and proxied releases
     * @since 6.0.0
     */
    MavenArtifactRepository releases();

    /**
     * Own and proxied releases.
     *
     * @return created repository for own and proxied releases
     * @param configureAction action to execute on the created repository
     * @since 6.0.0
     */
    default MavenArtifactRepository releases(final @NotNull Action<MavenArtifactRepository> configureAction) {
        final MavenArtifactRepository repo = this.releases();
        requireNonNull(configureAction, "configureAction").execute(repo);
        return repo;
    }

    /**
     * Own releases.
     *
     * @return created repository for own releases only
     * @since 6.0.0
     */
    MavenArtifactRepository ownReleases();

    /**
     * Own releases.
     *
     * @return created repository for own releases only
     * @param configureAction action to execute on the created repository
     * @since 6.0.0
     */
    default MavenArtifactRepository ownReleases(final @NotNull Action<MavenArtifactRepository> configureAction) {
        final MavenArtifactRepository repo = this.ownReleases();
        requireNonNull(configureAction, "configureAction").execute(repo);
        return repo;
    }

    /**
     * Own and proxied snapshots.
     *
     * @return created repository for own and proxied snapshots
     * @since 6.0.0
     */
    MavenArtifactRepository snapshots();

    /**
     * Own and proxied snapshots.
     *
     * @return created repository for own and proxied snapshots
     * @param configureAction action to execute on the created repository
     * @since 6.0.0
     */
    default MavenArtifactRepository snapshots(final @NotNull Action<MavenArtifactRepository> configureAction) {
        final MavenArtifactRepository repo = this.snapshots();
        requireNonNull(configureAction, "configureAction").execute(repo);
        return repo;
    }
}
