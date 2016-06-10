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
package ch.fihlon.moodini.business.question.control;

import ch.fihlon.moodini.business.question.entity.Question;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

class QuestionRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Long, Question> questions = new ConcurrentHashMap<>();

    private final AtomicLong questionSeq = new AtomicLong(0);

    Question create(@NotNull final Question question) {
        final Long questionId = questionSeq.incrementAndGet();
        final Long version = (long) question.hashCode();
        final Question questionToCreate = question.toBuilder()
                .questionId(questionId)
                .version(version)
                .build();
        questions.put(questionId, questionToCreate);
        return questionToCreate;
    }

    Question update(@NotNull final Question question) {
        final Question previousQuestion = questions.getOrDefault(question.getQuestionId(), question);
        if (!previousQuestion.getVersion().equals(question.getVersion())) {
            throw new ConcurrentModificationException("You tried to update a question that was modified concurrently!");
        }
        final Long version = (long) question.hashCode();
        final Question questionToUpdate = question.toBuilder()
                .version(version)
                .build();
        questions.put(questionToUpdate.getQuestionId(), questionToUpdate);
        return questionToUpdate;
    }

    Optional<Question> findByQuestionId(@NotNull final Long questionId) {
        return Optional.ofNullable(this.questions.get(questionId));
    }

    List<Question> findAll() {
        return this.questions.values().stream()
                .sorted(comparingLong(Question::getQuestionId))
                .collect(toList());
    }

    Optional<Question> findLatest() {
        return this.questions.values().stream()
                .max(comparingLong(Question::getQuestionId));
    }

    void delete(@NotNull final Long questionId) {
        if (!questions.containsKey(questionId)) {
            throw new NotFoundException();
        }
        // TODO delete only questions without answers/votes
        this.questions.remove(questionId);
    }
}
