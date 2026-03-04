package br.com.scrubs

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform