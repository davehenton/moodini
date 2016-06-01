package ch.fihlon.moodini.business.question.entity;

import ch.fihlon.moodini.business.question.entity.Question.QuestionBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@JsonDeserialize(builder = QuestionBuilder.class)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long questionId;

    private Long version;

    @Length(max=100)
    private String question;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class QuestionBuilder {
    }

}
