/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */


package io.javalin

import io.javalin.http.ContentType
import io.javalin.http.Header
import io.javalin.http.HttpStatus.IM_A_TEAPOT
import io.javalin.http.Problem
import io.javalin.json.JavalinGson
import io.javalin.testing.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI


class TestProblem {

    @Test
    fun `problem with all fields is serialized with problem json content type`() = TestUtil.test { app, http ->
        app.unsafe.routes.get("/problem") {
            it.problem(
                Problem(
                    type = URI.create("https://example.com/probs/out-of-credit"),
                    title = "You do not have enough credit.",
                    status = 403,
                    detail = "Your current balance is 30, but that costs 50.",
                    instance = URI.create("/account/12345/msgs/abc"),
                )
            )
        }
        val response = http.get("/problem")
        assertThat(response.status).isEqualTo(403)
        assertThat(response.headers.getFirst(Header.CONTENT_TYPE)).startsWith(ContentType.APPLICATION_PROBLEM_JSON.mimeType)
        assertThat(response.body).isEqualTo(
            """{"type":"https://example.com/probs/out-of-credit","title":"You do not have enough credit.","status":403,"detail":"Your current balance is 30, but that costs 50.","instance":"/account/12345/msgs/abc"}"""
        ) 
    }

    @Test
    fun `defaults are omitted from serialized problem`() = TestUtil.test { app, http ->
        app.unsafe.routes.get("/problem") { it.problem(Problem()) }
        assertThat(http.getBody("/problem")).isEqualTo("{}")
    }

    @Test
    fun `explicit about blank type is omitted`() = TestUtil.test { app, http -> 
        app.unsafe.routes.get("/problem") { it.problem(Problem(type = URI.create("about:blank"), title = "Boom")) }
        assertThat(http.getBody("/problem")).isEqualTo("""{"title":"Boom"}""")
    }

    @Test
    fun `additional members are included in top level`() = TestUtil.test { app, http ->
        app.unsafe.routes.get("/problem") {
            it.problem(Problem(title = "Boom", additionalMembers = mapOf("balance" to 30, "accountId" to "12345")))
        }
        assertThat(http.getBody("/problem")).isEqualTo(
            """{"balance":30,"accountId":"12345","title":"Boom"}"""
        )
    }

    @Test
    fun `additional members cannot override standard problem members`() = TestUtil.test { app, http ->
        app.unsafe.routes.get("/problem") {
            it.problem(Problem(status = 403, additionalMembers = mapOf("status" to 200, "title" to "Nope")))
        }
        assertThat(http.getBody("/problem")).isEqualTo("""{"status":403,"title":"Nope"}""")
    }

    @Test
    fun `null status does not overwrite existing response status`() = TestUtil.test { app, http ->
        app.unsafe.routes.get("/problem") { it.status(IM_A_TEAPOT.code).problem(Problem(title = "Boom")) }
        assertThat(http.get("/problem").status).isEqualTo(IM_A_TEAPOT.code)
    }

    @Test
    fun `problem is serialized with gson as well`() = TestUtil.test(Javalin.create { it.jsonMapper(JavalinGson()) }) { app, http -> 
        app.unsafe.routes.get("/problem") { it.problem(Problem(title = "Boom", status = 418)) }
        val response = http.get("/problem")
        assertThat(response.headers.getFirst(Header.CONTENT_TYPE)).startsWith(ContentType.APPLICATION_PROBLEM_JSON.mimeType)
        assertThat(response.body).isEqualTo("""{"title":"Boom","status":418}""")
    }
}