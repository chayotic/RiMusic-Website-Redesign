package it.fast4x.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.util.generateNonce
import it.fast4x.innertube.Innertube
import it.fast4x.innertube.models.Context
import it.fast4x.innertube.models.MediaType
import it.fast4x.innertube.models.PlayerResponse
import it.fast4x.innertube.models.bodies.PlayerBody

// TODO remove in the future
suspend fun Innertube.player(body: PlayerBody, withLogin: Boolean = false, signatureTimestamp: Int? = null): Result<PlayerResponse> = runCatching {
    val response = when (withLogin) {
        true -> try {
            println("Innertube newPlayer Player Response Try Player with login")
            player(body.videoId, body.playlistId, signatureTimestamp).body<PlayerResponse>()
        } catch (e: Exception) {
            println("Innertube newPlayer Player Response Error $e")
            println("Innertube newPlayer Player Response Try noLogInPlayer")
            noLogInPlayer(body.videoId).body<PlayerResponse>()
        }
        false -> {
            println("Innertube newPlayer Player Response Try noLogInPlayer")
            noLogInPlayer(body.videoId).body<PlayerResponse>()
        }
    }


    println("Innertube newPlayer withLogin $withLogin response adaptiveFormats ${response?.streamingData?.adaptiveFormats}")
    println("Innertube newPlayer withLogin $withLogin response Formats ${response?.streamingData?.formats}")
    println("Innertube newPlayer withLogin $withLogin response expire ${response?.streamingData?.expiresInSeconds}")

    return@runCatching response
}

suspend fun Innertube.playerAdvanced(body: PlayerBody): Result<Pair<String?, PlayerResponse?>> = runCatching {

    val maxRetries = 2
    var retryCount = 0
    var loop = true
    var response: Triple<String?, PlayerResponse?, MediaType?>? = null

    while (loop == true) {
        response = noLoginPlayerWithPoToken(body.videoId, body.playlistId).getOrNull()

        println("Innertube newPlayer response adaptiveFormats ${response?.second?.streamingData?.adaptiveFormats}")
        //println("Innertube newPlayer response Formats ${response?.second?.streamingData?.formats}")
        println("Innertube newPlayer response expire ${response?.second?.streamingData?.expiresInSeconds}")

        if (response?.second?.playabilityStatus?.status == "OK" || retryCount >= maxRetries)
            loop = false

        println("playerAdvanced retryCount: $retryCount")
        retryCount++
    }


    return@runCatching Pair(response?.first, response?.second)
}
