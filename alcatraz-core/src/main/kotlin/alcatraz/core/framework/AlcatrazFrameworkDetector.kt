package alcatraz.core.framework

object AlcatrazFrameworkDetector {
    fun detectFramework(): FrameworkType =
        when {
            isClassPresent("org.springframework.web.servlet.DispatcherServlet") -> FrameworkType.SPRING

            isClassPresent("io.ktor.server.application.Application") -> FrameworkType.KTOR

            isClassPresent("play.api.mvc.Action") -> FrameworkType.PLAY

            isClassPresent("io.dropwizard.Application") -> FrameworkType.DROPWIZARD
            isClassPresent("alcatraz.core.framework.AlcatrazFrameworkDetector") -> FrameworkType.ALCATRAZ

            else -> throw IllegalStateException("No supported framework detected. Please specify a framework adapter explicitly.")
        }

    private fun isClassPresent(className: String): Boolean =
        try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
}
