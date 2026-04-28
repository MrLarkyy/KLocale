package gg.aquatic.klocale.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.LocaleSerializer
import gg.aquatic.klocale.message.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class S3LocaleProvider<M : Message<M>>(
    val bucket: String,
    val key: String,
    val serializer: LocaleSerializer<ByteArray, M>,
    private val client: S3Client
) : LocaleProvider<M> {

    constructor(
        bucket: String,
        key: String,
        region: Region,
        serializer: LocaleSerializer<ByteArray, M>
    ) : this(
        bucket = bucket,
        key = key,
        serializer = serializer,
        client = S3Client.builder().region(region).build()
    )

    override suspend fun fetch(): Map<String, Map<String, M>> = withContext(Dispatchers.IO) {
        val content = try {
            val request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            val response: ResponseBytes<GetObjectResponse> = client.getObjectAsBytes(request)
            response.asByteArray()
        } catch (e: Exception) {
            return@withContext emptyMap()
        }

        if (content.isEmpty()) {
            return@withContext emptyMap()
        }

        serializer.parse(content) ?: emptyMap()
    }
}
