package ch.fihlon.moodini.business.question.boundary;

import ch.fihlon.moodini.business.question.control.QuestionService;
import ch.fihlon.moodini.business.question.entity.Question;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

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

    @Inject
    public QuestionsResource(@NotNull final QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Get a list of all questions
     *
     * @return a list of all questions
     */
    @GET
    public List<Question> findAll() {
        return questionService.findAll();
    }

    /**
     * Get the latest question available
     *
     * @return the latest question or a <code>404 NOT FOUND</code> if there is no question available
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
     */
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Question created successfully", responseHeaders = @ResponseHeader(name = "Location", description = "The location of the created question", response = URI.class)),
            @ApiResponse(code = 400, message = "The data of the question is not valid")
    })
    @POST
    public Response createQuestion(@Valid final Question newQuestion,
                                   @Context UriInfo uriInfo) {
        final Question savedQuestion = questionService.create(newQuestion);
        final Long questionId = savedQuestion.getQuestionId();
        final URI uri = uriInfo.getAbsolutePathBuilder().path(File.separator + questionId).build();
        return Response.created(uri).build();
    }

}
