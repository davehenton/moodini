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
package ch.fihlon.moodini.server.business.question.boundary;

import ch.fihlon.moodini.server.Injector;
import ch.fihlon.moodini.server.business.question.control.QuestionService;
import ch.fihlon.moodini.server.business.question.entity.Answer;
import ch.fihlon.moodini.server.business.question.entity.Question;
import ch.fihlon.moodini.server.exception.StatusCodeException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Optional;

/**
 * This verticle is the entry point fo the HTTP requests to the RESTful JSON
 * interface of the {@link Question}s resources.
 */
public class QuestionsVerticle extends AbstractVerticle {

    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int SC_CREATED = 201;
    private static final int SC_NO_CONTENT = 204;
    private static final int SC_NOT_FOUND = 404;
    private static final String PARAM_NAME_ID = "id";
    private static final String HEADER_LOCATION = "Location";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json; charset=utf-8";

    @Inject
    private QuestionService questionService;

    /**
     * Start this verticle.
     *
     * @param future a future which is called when this verticle start-up is complete
     */
    @Override
    public void start(@NotNull final Future<Void> future) {
        Injector.injectMembers(this);

        // Create a router object.
        final Router router = Router.router(vertx);

        // Add the body handler
        router.route("/api/questions*").handler(BodyHandler.create())
                .failureHandler(this::failueHandler);

        // Add the routing
        // CHECKSTYLE DISABLE MultipleStringLiterals FOR 7 LINES
        router.post("/api/questions").handler(this::create);
        router.get("/api/questions").handler(this::list);
        router.get("/api/questions/latest").handler(this::latest);
        router.get("/api/questions/:id").handler(this::read);
        router.put("/api/questions/:id").handler(this::update);
        router.delete("/api/questions/:id").handler(this::delete);
        router.post("/api/questions/:id/vote").handler(this::vote);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .listen(
                // Retrieve the port from the configuration,
                // default to 8080.
                config().getInteger("http.port", DEFAULT_HTTP_PORT),
                result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                }
            );
    }

    private void failueHandler(@NotNull final RoutingContext routingContext) {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        final Throwable failure = routingContext.failure();
        if (failure instanceof StatusCodeException) {
            final StatusCodeException exception = (StatusCodeException) failure;
            routingContext.response().setStatusCode(exception.getStatusCode());
        }
        routingContext.response().end();
    }

    private void vote(@NotNull final RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam(PARAM_NAME_ID));
        final String body = routingContext.getBodyAsString();
        final Answer answer = Answer.valueOf(body);
        questionService.vote(id, answer);
        routingContext.response()
                .end();
    }

    private void list(@NotNull final RoutingContext routingContext) {
        routingContext.response()
                .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .end(Json.encodePrettily(questionService.readAll()));
    }

    private void read(@NotNull final RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam(PARAM_NAME_ID));
        final Optional<Question> question = questionService.read(id);
        if (question.isPresent()) {
            routingContext.response()
                    .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                    .end(Json.encodePrettily(question.get()));
        } else {
            routingContext.response()
                    .setStatusCode(SC_NOT_FOUND)
                    .end();
        }
    }

    private void latest(@NotNull final RoutingContext routingContext) {
        final Question question = questionService.readLatest();
        routingContext.response()
                .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .end(Json.encodePrettily(question));
    }

    private void create(@NotNull final RoutingContext routingContext) {
        final String body = routingContext.getBodyAsString();
        final Question question = Json.decodeValue(body,
                Question.class);
        final Question createdQuestion = questionService.create(question);
        final String location = routingContext.normalisedPath() +
                File.separator + createdQuestion.getQuestionId().toString();
        routingContext.response()
                .setStatusCode(SC_CREATED)
                .putHeader(HEADER_LOCATION, location)
                .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .end(Json.encodePrettily(createdQuestion));
    }

    private void update(@NotNull final RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam(PARAM_NAME_ID));
        final Question question = Json.decodeValue(routingContext.getBodyAsString(),
                Question.class).toBuilder().questionId(id).build();
        final Question updatedQuestion = questionService.update(question);
        routingContext.response()
                .putHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .end(Json.encodePrettily(updatedQuestion));
    }

    private void delete(@NotNull final RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam(PARAM_NAME_ID));
        questionService.delete(id);
        routingContext.response()
                .setStatusCode(SC_NO_CONTENT)
                .end();
    }

}
