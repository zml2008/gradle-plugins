/*
 * Copyright 2020 zml
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
package ca.stellardrift.build.configurate;

import org.gradle.util.GradleVersion;

public final class GradleVersionUtil {
    public static final boolean VERSION_CATALOGS_STABLE = currentIsOrNewer("7.4");

    private GradleVersionUtil() {
    }

    public static boolean currentIsOrNewer(final String ver) {
        return GradleVersion.current().compareTo(GradleVersion.version(ver)) >= 0;
    }
}
