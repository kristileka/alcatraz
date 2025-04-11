package alcatraz.integrity.validator

import alcatraz.integrity.model.PlayIntegrityEnvelope

interface PlayIntegrityService {
    fun validate(token: String): PlayIntegrityEnvelope
}
