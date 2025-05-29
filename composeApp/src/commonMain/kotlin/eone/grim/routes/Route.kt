package eone.grim.routes

sealed class Route (val path: String){

    object Auth : Route("auth")

    object Home : Route("home")

    data class Editor(val id: String) : Route("editor/$id") {
        companion object { const val Arg = "eid" }

    }
}