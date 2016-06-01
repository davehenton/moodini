package ch.fihlon.moodini.business.question.control;

import ch.fihlon.moodini.OptimisticLockException;
import ch.fihlon.moodini.business.question.entity.Question;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
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
        final Long version = Long.valueOf(question.hashCode());
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
            throw new OptimisticLockException("You tried to update an user that was modified concurrently!");
        }
        final Long version = Long.valueOf(question.hashCode());
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

    void delete(final Long questionId) {
        // TODO delete only questions without answers/votes
        this.questions.remove(questionId);
    }
}
