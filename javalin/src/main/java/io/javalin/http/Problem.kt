/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.http

import java.net.URI

private val ABOUT_BLANK = URI.create("about:blank")

/**
 * A Problem Details object, as defined by RFC 9457:
 * https://www.rfc-editor.org/rfc/rfc9457.html
 * 
 * @param type A URI reference identifying the problem type. The RFC default is 
 * `about:blank`, which represents an error with no specific type.
 * @param title A short, human-readable summary of the problem type.
 * @param status The HTTP status code. This is redundant with the actual response status.
 * @param detail A human-readable explanation specific to this occurrence of the problem.
 * @param instance A URI reference identifying this specific occurrence of the problem.
 * @param additionalMembers Problem-specific extension members.
 * These are included in the top level of the serialized object, not nested under a key
 */


data class Problem @JvmOverloads constructor(
    val type: URI = ABOUT_BLANK,
    val title: String = "",
    val status: Int? = null,
    val detail: String? = null,
    val instance: URI? = null,
    val additionalMembers: Map<String, Any?> = emptyMap(),
)

/**
 * Converts this problem into the map that gets serialized to JSON. 
 * Members which are absent are omitted rather than serialized as `null`, as required by the RFC
 */

internal fun Problem.toMap(): Map<String, Any?> = buildMap {
    putAll(additionalMembers)
    if (type != ABOUT_BLANK) put("type", type.toString())
    if (title.isNotEmpty()) put("title", title)
    status?.let { put("status", it) }
    detail?.let { put("detail", it) }
    instance?.let { put("instance", it.toString()) }
}