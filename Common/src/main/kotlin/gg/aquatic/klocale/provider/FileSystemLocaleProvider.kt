package gg.aquatic.klocale.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.LocaleSerializer
import gg.aquatic.klocale.message.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

class FileSystemLocaleProvider<M : Message<M>>(
    val path: Path,
    val serializer: LocaleSerializer<ByteArray, M>
) : LocaleProvider<M> {

    constructor(path: String, serializer: LocaleSerializer<ByteArray, M>) : this(Path.of(path), serializer)

    override suspend fun fetch(): Map<String, Map<String, M>> = withContext(Dispatchers.IO) {
        val content = try {
            if (!Files.isRegularFile(path)) {
                return@withContext emptyMap()
            }
            Files.readAllBytes(path)
        } catch (e: Exception) {
            return@withContext emptyMap()
        }

        if (content.isEmpty()) {
            return@withContext emptyMap()
        }

        serializer.parse(content) ?: emptyMap()
    }
}
