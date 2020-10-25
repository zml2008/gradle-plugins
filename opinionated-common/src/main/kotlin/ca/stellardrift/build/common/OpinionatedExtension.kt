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

package ca.stellardrift.build.common

import net.kyori.indra.IndraExtension
import net.kyori.indra.data.License
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

open class OpinionatedExtension(objects: ObjectFactory) {
    /**
     * Create an automatic module name using the format `<group>.<name>`,
     * where name has all dashes replaced with dots.
     */
    var automaticModuleNames = false
}

fun IndraExtension.gpl3() = license.set(
    License(
        "GPL-3.0",
        "GNU General Public License, Version 3",
        "https://www.gnu.org/licenses/gpl-3.0.html"
    )
)

fun IndraExtension.agpl3() = license.set(
    License(
        "AGPL-V3",
        "GNU Affero General Public License, Version 3",
        "https://www.gnu.org/licenses/agpl-3.0.html"
    )
)

internal const val EXTENSION_NAME = "opinionated"

fun Project.getOrCreateOpinionatedExtension(): OpinionatedExtension {
    return extensions.findByType(OpinionatedExtension::class.java)
            ?: extensions.create(EXTENSION_NAME, OpinionatedExtension::class.java)
}
