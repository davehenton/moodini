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

import ch.fihlon.moodini.business.question.control.QuestionService;
import ch.fihlon.moodini.business.question.entity.Answer;
import ch.fihlon.moodini.business.question.entity.Question;
import ch.fihlon.moodini.business.user.entity.User;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.security.Principal;
import java.util.List;

@Immutable
@Path("questions")
@Produces(MediaType.APPLICATION_JSON)
@Timed(name = "Timed: QuestionsResource")
@Metered(name = "Metered: QuestionsResource")
public class QuestionsResource {

    private QuestionService questionService;

    @Inject
    public QuestionsResource(@NotNull final QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Create a new question
     *
     * @param newQuestion the question to create
     * @param uriInfo information about the URI of the request
     * @return a <code>201 CREATED</code> and the location of the created question
     * @successResponse 201 The question was successfully created
     * @errorResponse 400 There was no question in the request
     */
    @POST
    public Response create(@Auth Principal principal,
                           @Valid final Question newQuestion,
                           @Context UriInfo uriInfo) {
        final User user = (User) principal;
        final Question savedQuestion = questionService.create(user, newQuestion);
        final Long questionId = savedQuestion.getQuestionId();
        final URI uri = uriInfo.getAbsolutePathBuilder().path(File.separator + questionId).build();
        return Response.created(uri).build();
    }

    /**
     * Get the question with the specified id
     *
     * @return the question with the specified id or a <code>404 NOT FOUND</code> if there is no question available
     * @successResponse 200 Successful request
     * @errorResponse 404 The question does not exist
     */
    @Path("{questionId}")
    @GET
    public Question read(@PathParam("questionId") final Long questionId) {
        return questionService.read(questionId).orElseThrow(NotFoundException::new);
    }

    /**
     * Get a list of all questions
     *
     * @return a list of all questions
     * @successResponse 200 Successful request
     */
    @GET
    public List<Question> readAll() {
        return questionService.readAll();
    }

    /**
     * Get the latest question available
     *
     * @return the latest question or a <code>404 NOT FOUND</code> if there is no question available
     * @successResponse 200 The latest question `ch.fihlon.moodini.business.question.entity.Question
     * @errorResponse 404 There is no question available
     */
    @Path("latest")
    @GET
    public Question readLatest() {
        return questionService.readLatest();
    }

    /**
     * Update an existing question
     *
     * @param question the question to update
     * @return a <code>200 OK</code> and the updated question
     * @successResponse 200 The question was successfully updated
     * @errorResponse 400 The question data in the request was invalid
     */
    @Path("{questionId}")
    @PUT
    public Response update(@Auth Principal principal,
                           @PathParam("questionId") final Long questionId,
                           @Valid final Question question) {
        final User user = (User) principal;
        final Question newQuestion = question.toBuilder()
                .questionId(questionId)
                .build();
        final Question savedQuestion = questionService.update(user, newQuestion);
        return Response.ok(savedQuestion).build();
    }

    /**
     * Delete the question with the specified id
     *
     * @return a <code>204 NO CONTENT</code> on success or a <code>404 NOT FOUND</code> if there is no question available
     * @successResponse 204 The question was successfully deleted
     * @errorResponse 404 The question does not exist
     */
    @Path("{questionId}")
    @DELETE
    public Response delete(@Auth Principal principal,
                           @PathParam("questionId") final Long questionId) {
        final User user = (User) principal;
        questionService.delete(user, questionId);
        return Response.noContent().build();
    }

    /**
     * Vote for the specific answer of a specific question
     *
     * @return a <code>200 OK</code> for a successful vote
     * @successResponse 204 The vote was successful
     * @errorResponse 404 The question does not exist
     */
    @Path("{questionId}/vote")
    @POST
    public Response vote(@PathParam("questionId") final Long questionId,
                         @NotNull final Answer answer) {
        questionService.vote(questionId, answer);
        return Response.ok().build();
    }
}
