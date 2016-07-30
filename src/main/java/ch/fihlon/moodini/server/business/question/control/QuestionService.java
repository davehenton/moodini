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
package ch.fihlon.moodini.server.business.question.control;

import ch.fihlon.moodini.server.PersistenceManager;
import ch.fihlon.moodini.server.business.question.entity.Answer;
import ch.fihlon.moodini.server.business.question.entity.Question;
import ch.fihlon.moodini.server.exception.NotFoundException;
import pl.setblack.airomem.core.SimpleController;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * This singleton is a service for working with {@link Question}s.
 */
@Singleton
public class QuestionService {

    private SimpleController<QuestionRepository> controller;

    public QuestionService() {
        controller = PersistenceManager.createSimpleController(Question.class, QuestionRepository::new);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> controller.close()));
    }

    public Question create(@NotNull final Question question) {
        return controller.executeAndQuery((ctrl) -> ctrl.create(question));
    }

    public Question update(@NotNull final Question question) {
        final Long questionId = question.getQuestionId();
        read(questionId).orElseThrow(NotFoundException::new);
        return controller.executeAndQuery((ctrl) -> ctrl.update(question));
    }

    public Optional<Question> read(@NotNull final Long questionId) {
        return controller.readOnly().read(questionId);
    }

    public List<Question> readAll() {
        return controller.readOnly().readAll();
    }

    public Question readLatest() {
        final Optional<Question> optional = controller.readOnly().readLatest();
        return optional.orElseThrow(NotFoundException::new);
    }

    public void delete(@NotNull final Long questionId) {
        final Question oldQuestion = read(questionId).orElseThrow(NotFoundException::new);
        controller.execute((ctrl) -> ctrl.delete(questionId));
    }

    public Long vote(@NotNull final Long questionId,
                     @NotNull final Answer answer) {
        read(questionId).orElseThrow(NotFoundException::new);
        return controller.executeAndQuery((ctrl) -> ctrl.vote(questionId, answer));
    }
}
