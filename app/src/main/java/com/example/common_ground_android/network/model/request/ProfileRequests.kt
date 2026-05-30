package com.example.common_ground_android.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProfileRequest(
    @SerialName("username")
    val username: String,

    @SerialName("bio")
    val bio: String? = null
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("username")
    val username: String? = null,

    @SerialName("bio")
    val bio: String? = null
)

@Serializable
data class ProfileInterestsRequest(
    @SerialName("ids")
    val ids: List<String>
)

data class UploadAvatarRequest(
    val file: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadAvatarRequest

        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        return file.contentHashCode()
    }
}