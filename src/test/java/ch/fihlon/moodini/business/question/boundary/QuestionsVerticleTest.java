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

import ch.fihlon.moodini.Injector;
import ch.fihlon.moodini.business.question.control.QuestionService;
import ch.fihlon.moodini.business.question.entity.Question;
import com.google.inject.AbstractModule;
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
import org.mockito.Mock;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.ServerSocket;

import static com.jayway.restassured.RestAssured.port;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class QuestionsVerticleTest {

    private static final String QUESTION_TEXT = "Do you like testing?";
    private static final Long QUESTION_ID = 1L;
    private static final Long QUESTION_VERSION = 42L;
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_LOCATION = "Location";

    private Vertx vertx;

    @Mock
    private QuestionService questionServiceMock;

    @Before
    public void setUp(@NotNull final TestContext context) throws IOException {
        final Question answerQuestion = Question.builder()
                .question(QUESTION_TEXT)
                .questionId(QUESTION_ID)
                .version(QUESTION_VERSION)
                .build();
        questionServiceMock = mock(QuestionService.class);
        when(questionServiceMock.create(any(Question.class))).thenReturn(answerQuestion);
        when(questionServiceMock.readAll()).thenReturn(singletonList(answerQuestion));
        Injector.setModule(new AbstractModule() {
            @Override
            protected void configure() {
                bind(QuestionService.class).toInstance(questionServiceMock);
            }
        });

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
        Injector.resetModule();
    }

    @Test
    public void testCreate(@NotNull final TestContext context) {
        final Async async = context.async();
        final String json = Json.encodePrettily(Question.builder().question(QUESTION_TEXT).build());
        final String length = Integer.toString(json.length());
        vertx.createHttpClient().post(port, "localhost", "/api/questions")
                .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .putHeader(HEADER_CONTENT_LENGTH, length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get(HEADER_LOCATION)
                            .startsWith("/api/questions/"));
                    final MultiMap h = response.headers();
                    final String ct = h.get(HEADER_CONTENT_TYPE);
                    context.assertTrue(ct.contains(CONTENT_TYPE_APPLICATION_JSON));
                    response.bodyHandler(body -> {
                        final Question question = Json.decodeValue(body.toString(), Question.class);
                        context.assertEquals(question.getQuestion(), QUESTION_TEXT);
                        context.assertEquals(question.getQuestionId(), QUESTION_ID);
                        context.assertEquals(question.getVersion(), QUESTION_VERSION);
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
                    context.assertTrue(response.headers().get(HEADER_CONTENT_TYPE)
                            .contains(CONTENT_TYPE_APPLICATION_JSON));
                    response.bodyHandler(body -> {
                        context.assertTrue(body.toString().contains(QUESTION_TEXT));
                        async.complete();
                    });
                });
    }

}