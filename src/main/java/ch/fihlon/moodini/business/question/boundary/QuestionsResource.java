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
import ch.fihlon.moodini.business.question.entity.Question;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.util.List;

@Immutable
@Path("questions")
@Produces(MediaType.APPLICATION_JSON)
@Timed(name = "Timed: QuestionsResource")
@Metered(name = "Metered: QuestionsResource")
public class QuestionsResource {

    private QuestionService questionService;

    private QuestionResource questionResource;

    @Inject
    public QuestionsResource(@NotNull final QuestionService questionService,
                             @NotNull final QuestionResource questionResource) {
        this.questionService = questionService;
        this.questionResource = questionResource;
    }

    /**
     * Get a list of all questions
     *
     * @return a list of all questions
     * @successResponse 200 Successful request
     */
    @GET
    public List<Question> findAll() {
        return questionService.findAll();
    }

    @Path("{questionId}")
    public QuestionResource getQuestionResource() {
        return questionResource;
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
    public Question findLatest() {
        return questionService.findLatest();
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
    public Response createQuestion(@Valid final Question newQuestion,
                                   @Context UriInfo uriInfo) {
        final Question savedQuestion = questionService.create(newQuestion);
        final Long questionId = savedQuestion.getQuestionId();
        final URI uri = uriInfo.getAbsolutePathBuilder().path(File.separator + questionId).build();
        return Response.created(uri).build();
    }

}
