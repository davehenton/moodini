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
package ch.fihlon.moodini.server.business.question.entity;

import ch.fihlon.moodini.server.business.question.entity.Question.QuestionBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * This entity class is representing a question.
 */
@Value
@SuppressWarnings("PMD.UnusedPrivateField")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@JsonDeserialize(builder = QuestionBuilder.class)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_TEXT_LENGTH = 100;

    private Long questionId;

    private Long version;

    @NotEmpty
    @Length(max = MAX_TEXT_LENGTH)
    private String text;

    /**
     * This is the builder for the {@link Question}.
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static final class QuestionBuilder {
    }

}
