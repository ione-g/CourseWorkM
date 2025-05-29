package eone.grim

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform