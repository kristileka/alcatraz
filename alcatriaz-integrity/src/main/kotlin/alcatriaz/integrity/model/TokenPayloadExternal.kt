package alcatriaz.integrity.model

data class TokenPayloadExternal(
    var requestDetails: RequestDetails,
    var appIntegrity: AppIntegrity,
    var deviceIntegrity: DeviceIntegrity,
    var accountDetails: AccountDetails,
    var environmentDetails: EnvironmentDetails?,
) {
    data class RequestDetails(
        var requestPackageName: String,
        var timestampMillis: String,
        var nonce: String,
    )

    data class AppIntegrity(
        var appRecognitionVerdict: String,
        var packageName: String,
        var certificateSha256Digest: ArrayList<String>,
        var versionCode: String,
    )

    data class DeviceIntegrity(
        var deviceRecognitionVerdict: ArrayList<String>,
        var recentDeviceActivity: RecentDeviceActivity?,
    ) {
        data class RecentDeviceActivity(
            var deviceActivityLevel: String,
        )
    }

    data class AccountDetails(
        var appLicensingVerdict: String,
    )

    data class EnvironmentDetails(
        var playProtectVerdict: String,
    )
}
