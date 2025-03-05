package alcatraz.core.framework

import alcatraz.core.http.HttpMethod

typealias RouteHandler = (Any, Any) -> Unit

interface FrameworkAdapter {
    fun setupInterception()

    fun registerRoute(
        path: String,
        method: HttpMethod,
        handler: RouteHandler,
    )
}
