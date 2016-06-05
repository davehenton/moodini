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

import ch.fihlon.moodini.AbstractLifecycleListener;
import ch.fihlon.moodini.PersistenceManager;
import ch.fihlon.moodini.business.question.entity.Question;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.eclipse.jetty.util.component.LifeCycle;
import pl.setblack.airomem.core.SimpleController;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;

@Singleton
@Timed(name = "Timed: QuestionService")
@Metered(name = "Metered: QuestionService")
public class QuestionService {

    private SimpleController<QuestionRepository> controller;

    @Inject
    public QuestionService(@NotNull final LifecycleEnvironment lifecycleEnvironment) {
        controller = PersistenceManager.createSimpleController(Question.class, QuestionRepository::new);
        lifecycleEnvironment.addLifeCycleListener(new AbstractLifecycleListener() {
            @Override
            public void lifeCycleStopping(@NotNull final LifeCycle event) {
                controller.close();
            }
        });
    }

//    @Inject
//    private HealthCheckRegistry healthCheckRegistry;

//    @PostConstruct
//    private void registerHealthCheck() {
//        final QuestionServiceHealthCheck questionServiceHealthCheck = new QuestionServiceHealthCheck(this);
//        healthCheckRegistry.register(QuestionService.class.getName(), questionServiceHealthCheck);
//    }

    public Question create(@NotNull final Question question) {
        return controller.executeAndQuery((mgr) -> mgr.create(question));
    }

    public Question update(@NotNull final Question question) {
        return controller.executeAndQuery((mgr) -> mgr.update(question));
    }

    public Optional<Question> findByQuestionId(@NotNull final Long questionId) {
        return controller.readOnly().findByQuestionId(questionId);
    }

    public List<Question> findAll() {
        return controller.readOnly().findAll();
    }

    public Question findLatest() {
        final Optional<Question> optional = controller.readOnly().findLatest();
        return optional.orElseThrow(NotFoundException::new);
    }

    public void delete(final Long userId) {
        controller.execute((mgr) -> mgr.delete(userId));
    }
}
