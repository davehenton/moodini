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
