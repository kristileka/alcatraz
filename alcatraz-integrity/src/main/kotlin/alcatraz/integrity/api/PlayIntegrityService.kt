package alcatraz.integrity.api

import alcatraz.integrity.model.PlayIntegrityEnvelope

interface PlayIntegrityService {
    fun validate(token: String): PlayIntegrityEnvelope
}