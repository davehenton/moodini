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
import ch.fihlon.moodini.business.question.entity.Answer;
import ch.fihlon.moodini.business.question.entity.Question;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

public class QuestionsVerticle extends AbstractVerticle {

    @Inject
    private QuestionService questionService;

    @Override
    public void start(Future<Void> fut) {
        Injector.injectMembers(this);

        // Create a router object.
        Router router = Router.router(vertx);

        // Serve static resources from the assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.route("/api/questions*").handler(BodyHandler.create());

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
                        config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        }
                );
    }

    private void vote(RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam("id"));
        final String body = routingContext.getBodyAsString();
        final Answer answer = Answer.valueOf(body);
        questionService.vote(id, answer);
        routingContext.response()
                .end();
    }

    private void list(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(questionService.readAll()));
    }

    private void read(RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam("id"));
        final Optional<Question> question = questionService.read(id);
        if (question.isPresent()) {
            routingContext.response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(question.get()));
        } else {
            routingContext.response()
                    .setStatusCode(404)
                    .end();
        }
    }

    private void latest(RoutingContext routingContext) {
        final Question question = questionService.readLatest();
        routingContext.response()
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(question));
    }

    private void create(RoutingContext routingContext) {
        final String body = routingContext.getBodyAsString();
        final Question question = Json.decodeValue(body,
                Question.class);
        final Question createdQuestion = questionService.create(question);
        final String location = routingContext.normalisedPath() +
                File.separator + createdQuestion.getQuestionId().toString();
        routingContext.response()
                .setStatusCode(201)
                .putHeader("Location", location)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(createdQuestion));
    }

    private void update(RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam("id"));
        final Question question = Json.decodeValue(routingContext.getBodyAsString(),
                Question.class).toBuilder().questionId(id).build();
        final Question updatedQuestion = questionService.update(question);
        routingContext.response()
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(updatedQuestion));
    }

    private void delete(RoutingContext routingContext) {
        final Long id = Long.valueOf(routingContext.request().getParam("id"));
        questionService.delete(id);
        routingContext.response()
                .setStatusCode(204)
                .end();
    }

}
