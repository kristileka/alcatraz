package alcatraz.server.devicecheck

import alcatraz.devicecheck.assertion.AssertionChallengeValidator
import alcatraz.devicecheck.assertion.AssertionValidator
import alcatraz.devicecheck.assertion.AssertionValidatorImpl
import alcatraz.devicecheck.attestation.AttestationValidator
import alcatraz.devicecheck.attestation.AttestationValidatorImpl
import alcatraz.devicecheck.common.AppleAppAttestEnvironment
import alcatraz.devicecheck.receipt.*
import alcatraz.server.Environment
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.name.Named
import java.net.URI
import java.time.Clock

class AlcatrazDeviceCheckModule(
    private val alcatrazDeviceCheckProperties: AlcatrazDeviceCheckProperties,
) : AbstractModule() {
    @Provides
    fun provideAppleEnvironment(environment: Environment): AppleAppAttestEnvironment =
        when (environment) {
            Environment.PRODUCTION -> AppleAppAttestEnvironment.PRODUCTION
            Environment.DEVELOPMENT -> AppleAppAttestEnvironment.DEVELOPMENT
        }

    @Provides
    @Named("exchangeReceiptURI")
    fun providesAppleReceiptExchangeURI(environment: Environment): URI =
        when (environment) {
            Environment.PRODUCTION -> ReceiptExchange.APPLE_DEVICE_CHECK_APP_ATTEST_PRODUCTION_URL
            Environment.DEVELOPMENT -> ReceiptExchange.APPLE_DEVICE_CHECK_APP_ATTEST_DEVELOPMENT_URL
        }

    @Provides
    fun providesAttestationValidatorImpl(
        appIdentifier: String,
        appAttestEnvironment: AppleAppAttestEnvironment,
        receiptValidator: ReceiptValidator,
    ): AttestationValidator =
        AttestationValidatorImpl(
            appIdentifier = appIdentifier,
            appleAppAttestEnvironment = appAttestEnvironment,
            clock = Clock.systemUTC(),
            receiptValidator = receiptValidator,
        )

    @Provides
    fun providesAttestationValidatorImpl(
        appIdentifier: String,
        assertionChallengeValidator: AssertionChallengeValidator,
    ): AssertionValidator =
        AssertionValidatorImpl(
            appIdentifier = appIdentifier,
            assertionChallengeValidator = assertionChallengeValidator,
        )

    @Provides
    @Named("appIdentifier")
    fun providesAppIdentifier(): String =
        with(alcatrazDeviceCheckProperties) {
            "$teamIdentifier.$bundleIdentifier"
        }

    @Provides
    fun providesReceiptValidator(appIdentifier: String): ReceiptValidator =
        ReceiptValidatorImpl(
            appIdentifier = appIdentifier,
            clock = Clock.systemUTC(),
            maxAge = ReceiptValidator.APPLE_RECOMMENDED_MAX_AGE,
        )

    @Provides
    fun providesAppleJWSGenerator(): AppleJwsGenerator = alcatrazDeviceCheckProperties.appleJwsGenerator

    @Provides
    fun providesReceiptExchange(
        exchangeRecipeURI: URI,
        appleJwsGenerator: AppleJwsGenerator,
        receiptValidator: ReceiptValidator,
    ): ReceiptExchange =
        ReceiptExchangeImpl(
            appleJwsGenerator,
            receiptValidator,
            exchangeRecipeURI,
        )
}
