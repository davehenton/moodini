package ch.fihlon.moodini;

import ch.fihlon.moodini.business.question.boundary.QuestionResource;
import ch.fihlon.moodini.business.question.boundary.QuestionsResource;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.validation.constraints.NotNull;

public class MoodiniApplication extends Application<MoodiniConfiguration> {

    public static void main(@NotNull final String... args) {
        try {
            new MoodiniApplication().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(@NotNull final Bootstrap<MoodiniConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/webapp", "/", "index.html", "webapp"));
        bootstrap.addBundle(new AssetsBundle("/apidocs", "/apidocs", "index.html", "apidocs"));
    }

    @Override
    public void run(@NotNull final MoodiniConfiguration configuration,
                    @NotNull final Environment environment) {
        registerModules(environment.getObjectMapper());
        final Injector injector = createInjector(configuration, environment);
        final QuestionsResource questionsResource = injector.getInstance(QuestionsResource.class);
        environment.jersey().register(questionsResource);
        final QuestionResource questionResource = injector.getInstance(QuestionResource.class);
        environment.jersey().register(questionResource);
    }

    private static void registerModules(@NotNull final ObjectMapper objectMapper) {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(Include.NON_ABSENT);
    }

    private Injector createInjector(@NotNull final MoodiniConfiguration configuration, @NotNull final Environment environment) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MoodiniConfiguration.class).toInstance(configuration);
                bind(HealthCheckRegistry.class).toInstance(environment.healthChecks());
                bind(LifecycleEnvironment.class).toInstance(environment.lifecycle());
                bind(MetricRegistry.class).toInstance(environment.metrics());
            }
        });
    }

}
