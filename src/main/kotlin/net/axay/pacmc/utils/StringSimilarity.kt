package net.axay.pacmc.utils

import org.apache.commons.text.similarity.LevenshteinDistance

/**
 * Calculates the similarity between two [CharSequence]s. If
 * both are empty, the similiraty will be 1.0.
 *
 * @return a number between 0.0 and 1.0
 */
fun CharSequence.similarity(other: CharSequence): Double {
    val (longer, shorter) = if (this.length >= other.length) this to other else other to this
    val longerLength = longer.length
    if (longerLength == 0) return 1.0
    return (longerLength - LevenshteinDistance.getDefaultInstance().apply(longer, shorter)) / longerLength.toDouble()
}
