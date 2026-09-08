package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class AppUpdateDto(
    val success: Boolean = true,
    @SerializedName("latest_version_code")
    val latestVersionCode: Int = 1,
    @SerializedName("latest_version_name")
    val latestVersionName: String = "1.0.0",
    @SerializedName("download_url")
    val downloadUrl: String = "",
    val changelog: String? = null,
    @SerializedName("is_force_update")
    val isForceUpdate: Boolean = false
)
