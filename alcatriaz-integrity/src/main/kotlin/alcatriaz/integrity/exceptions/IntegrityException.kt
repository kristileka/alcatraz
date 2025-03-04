package alcatriaz.integrity.exceptions

sealed class IntegrityException(
    message: String,
    cause: Throwable?,
) : RuntimeException(message, cause) {
    class InvalidAppIntegrity(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("App integrity is invalid. App Integrity Type: $type", cause)

    class InvalidDeviceIntegrity(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("Device integrity is invalid. Device Type: $type", cause)

    class InvalidRecentActivity(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("Device Recent Activity is invalid. Recent Activity Type> $type", cause)

    class InvalidEnvironmentDetails(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("Device Environment Details is invalid. Environment details Type: $type", cause)

    class WeakDevice(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("Device integrity is not strong Type: $type", cause)

    class InvalidPackageName(
        cause: Throwable? = null,
        packageName: String? = "",
        requestedPackageName: String? = "",
    ) : IntegrityException(
        "Play Integrity is invalid. Package Name: $packageName Requested Package Name: $requestedPackageName ",
        cause,
    )

    class InvalidNonce(
        cause: Throwable? = null,
        nonce: String? = "",
    ) : IntegrityException("Nonce is not base64 parsable. Nonce: $nonce", cause)

    class InvalidPlayIntegrity(
        cause: Throwable? = null,
    ) : IntegrityException("Integrity Response is invalid,", cause)

    class InvalidLicenseIntegrity(
        cause: Throwable? = null,
        type: String? = "",
    ) : IntegrityException("License integrity is invalid. License Type: $type", cause)
}
