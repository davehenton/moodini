/*
 * Moodini
 * Copyright (C) 2016 Marcus Fihlon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fihlon.moodini.business.question.boundary;

import ch.fihlon.moodini.business.question.entity.Question;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.ServerSocket;

import static com.jayway.restassured.RestAssured.port;

@RunWith(VertxUnitRunner.class)
public class QuestionsVerticleTest {

    private Vertx vertx;

    @Before
    public void setUp(@NotNull final TestContext context) throws IOException {
        vertx = Vertx.vertx();
        final ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("http.port", port));
        vertx.deployVerticle(QuestionsVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(@NotNull final TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testCreate(@NotNull final TestContext context) {
        final Async async = context.async();
        final String json = Json.encodePrettily(Question.builder().question("Do you like testing?").build());
        final String length = Integer.toString(json.length());
        vertx.createHttpClient().post(port, "localhost", "/api/questions")
                .putHeader("Content-Type", "application/json")
                .putHeader("Content-Length", length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("location").startsWith("/api/questions/"));
                    final MultiMap h = response.headers();
                    final String ct = h.get("Content-Type");
                    context.assertTrue(ct.contains("application/json"));
                    response.bodyHandler(body -> {
                        final Question question = Json.decodeValue(body.toString(), Question.class);
                        context.assertEquals(question.getQuestion(), "Do you like testing?");
                        context.assertNotEquals(question.getQuestionId(), 0L);
                        context.assertNotEquals(question.getVersion(), 0L);
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void testReadAll(@NotNull final TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/api/questions",
                response -> {
                    context.assertEquals(response.statusCode(), 200);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        context.assertTrue(body.toString().contains("Hallo Welt!"));
                        async.complete();
                    });
                });
    }

}