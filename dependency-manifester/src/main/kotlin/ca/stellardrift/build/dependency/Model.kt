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

package ca.stellardrift.build.dependency

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.net.URL

/**
 * A manifest containing repositories
 */
@Serializable
data class DependencyManifest(val version: String, val libraries: List<Dependency>)

@Serializable
data class Dependency(val name: String, val sha1: String, val relocate: Relocation? = null, val path: String, @Serializable(with=UrlSerializer::class) val url: URL)

@Serializable
data class Relocation(val source: String, val destination: String) {
    @Serializer(forClass = Relocation::class)
    companion object : KSerializer<Relocation> {
        override fun deserialize(decoder: Decoder): Relocation {
            val (source, dest) = decoder.decodeString().split(":", limit = 2)
            return Relocation(source, dest)
        }

        override fun serialize(encoder: Encoder, value: Relocation) {
            encoder.encodeString("${value.source}:${value.destination}")
        }
    }
}

@Serializer(forClass = URL::class)
class UrlSerializer : KSerializer<URL> {
    override fun deserialize(decoder: Decoder): URL {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: URL) {
        encoder.encodeString(value.toExternalForm())
    }
}
