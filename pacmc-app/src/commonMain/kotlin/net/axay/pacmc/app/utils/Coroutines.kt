package net.axay.pacmc.app.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Does the same as [map], but in parallel.
 */
suspend inline fun <T, R> Iterable<T>.pmap(crossinline transform: suspend (T) -> R): List<R> =
    coroutineScope { map { async { transform(it) } }.awaitAll() }
