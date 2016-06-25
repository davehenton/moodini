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
package ch.fihlon.moodini;

import ch.fihlon.moodini.business.question.boundary.QuestionsResource;
import ch.fihlon.moodini.business.token.boundary.TokenResource;
import ch.fihlon.moodini.business.user.boundary.UsersResource;
import ch.fihlon.moodini.business.user.entity.User;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.toastshaman.dropwizard.auth.jwt.JWTAuthFilter;
import com.github.toastshaman.dropwizard.auth.jwt.JsonWebTokenParser;
import com.github.toastshaman.dropwizard.auth.jwt.hmac.HmacSHA512Verifier;
import com.github.toastshaman.dropwizard.auth.jwt.parser.DefaultJsonWebTokenParser;
import com.google.common.base.Charsets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import javax.validation.constraints.NotNull;
import java.security.Principal;

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
        registerAuthorization(configuration, environment, injector);
        registerResources(environment, injector);
    }

    private static void registerModules(@NotNull final ObjectMapper objectMapper) {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new GuavaModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(Include.NON_ABSENT);
    }

    private Injector createInjector(@NotNull final MoodiniConfiguration configuration,
                                    @NotNull final Environment environment) {
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

    private void registerAuthorization(@NotNull final MoodiniConfiguration configuration,
                                       @NotNull final Environment environment,
                                       @NotNull final Injector injector) {
        final byte[] key = configuration.getTokenSecret().getBytes(Charsets.UTF_8);

        final JwtConsumer consumer = new JwtConsumerBuilder()
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setRequireSubject() // the JWT must have a subject claim
                .setVerificationKey(new HmacKey(key)) // verify the signature with the public key
                .setRelaxVerificationKeyValidation() // relaxes key length requirement
                .build(); // create the JwtConsumer instance

        final JsonWebTokenParser tokenParser = new DefaultJsonWebTokenParser();
        final HmacSHA512Verifier tokenVerifier = new HmacSHA512Verifier(key);

        environment.jersey().register(new AuthDynamicFeature(
                new JWTAuthFilter.Builder<User>()
                        .setTokenParser(tokenParser)
                        .setTokenVerifier(tokenVerifier)
                        .setRealm("realm")
                        .setPrefix("Bearer")
                        .setAuthenticator(injector.getInstance(MoodiniAuthenticator.class))
                        .buildAuthFilter()));

        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));
        environment.jersey().register(injector.getInstance(RolesAllowedDynamicFeature.class));
        environment.jersey().register(injector.getInstance(TokenResource.class));
    }

    private void registerResources(@NotNull final Environment environment,
                                   @NotNull final Injector injector) {
        environment.jersey().register(injector.getInstance(QuestionsResource.class));
        environment.jersey().register(injector.getInstance(UsersResource.class));
    }

}
