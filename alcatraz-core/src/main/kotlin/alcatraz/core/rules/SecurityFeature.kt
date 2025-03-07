package alcatraz.core.rules

enum class SecurityFeature {
    DEVICE_CHECK,
    INTEGRITY,
    BASIC_SIGNATURE,
    CERTIFICATE_SIGNATURE,
    RATE_LIMITER,
    HEADER_VERIFIER,
}
