package ch.fihlon.moodini.business.question.boundary;

import ch.fihlon.moodini.business.question.control.QuestionService;
import ch.fihlon.moodini.business.question.entity.Question;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Immutable
@Path("question/{questionId}")
@Produces(MediaType.APPLICATION_JSON)
@Timed(name = "Timed: QuestionResource")
@Metered(name = "Metered: QuestionResource")
public class QuestionResource {

    private QuestionService questionService;

    @Inject
    public QuestionResource(@NotNull final QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Get the question with the specified id
     *
     * @return the question with the specified id or a <code>404 NOT FOUND</code> if there is no question available
     */
    @GET
    public Question findByQuestionId(@PathParam("questionId") final Long questionId) {
        return questionService.findByQuestionId(questionId).orElseThrow(NotFoundException::new);
    }

    /**
     * Delete the question with the specified id
     *
     * @return a <code>204 NO CONTENT</code> on success or a <code>404 NOT FOUND</code> if there is no question available
     */
    @DELETE
    public Response delete(@PathParam("questionId") final Long questionId) {
        final Question question = findByQuestionId(questionId); // only delete existing questions
        questionService.delete(question.getQuestionId());
        return Response.noContent().build();
    }

}
