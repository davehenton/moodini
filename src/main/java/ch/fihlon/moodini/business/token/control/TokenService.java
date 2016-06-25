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
package ch.fihlon.moodini.business.token.control;

import ch.fihlon.moodini.MoodiniConfiguration;
import ch.fihlon.moodini.MoodiniConfiguration.SmtpConfiguration;
import ch.fihlon.moodini.business.token.entity.Challenge;
import ch.fihlon.moodini.business.user.control.UserService;
import ch.fihlon.moodini.business.user.entity.User;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.github.toastshaman.dropwizard.auth.jwt.hmac.HmacSHA512Signer;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebToken;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenClaim;
import com.github.toastshaman.dropwizard.auth.jwt.model.JsonWebTokenHeader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.apache.commons.io.Charsets;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Singleton
@Timed(name = "Timed: TokenService")
@Metered(name = "Metered: TokenService")
public class TokenService {

    private static final String CHALLENGE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MINIMAL_CHALLENGE_LENGTH = 5;
    private static final int MAXIMAL_CHALLENGE_LENGTH = 10;
    private static final int TRESHOLD_FOR_COMPLEXITY_INCREASE = 20;
    private static final int MAXIMAL_WRONG_CHALENGE_TRIES = 10;

    private final MoodiniConfiguration configuration;
    private final byte[] tokenSecret;
    private final UserService userService;
    private final Cache<String, Challenge> challengeCache;


    @Inject
    public TokenService(@NotNull final MoodiniConfiguration configuration,
                        @NotNull final UserService userService) {
        this.configuration = configuration;
        this.tokenSecret = configuration.getJwtTokenSecret().getBytes(Charsets.UTF_8);
        this.userService = userService;
        challengeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public void requestChallenge(@NotNull final String email) {
        userService.readByEmail(email).orElseThrow(NotFoundException::new);
        final Challenge challenge = generateChallenge();
        challengeCache.put(email, challenge);
        sendChallenge(email, challenge);
    }

    private Challenge generateChallenge() {
        final int requiredLength = currentlyRequiredChallengeLength();
        final StringBuilder challengeBuilder = new StringBuilder(requiredLength);
        final Random random = new SecureRandom();
        while (challengeBuilder.length() < requiredLength) {
            final char randomChar = CHALLENGE_CHARACTERS.charAt(random.nextInt(CHALLENGE_CHARACTERS.length()));
            challengeBuilder.append(randomChar);
        }
        return new Challenge(challengeBuilder.toString());
    }

    private int currentlyRequiredChallengeLength() {
        @SuppressWarnings("NumericCastThatLosesPrecision")
        final int complexityIncrease = (int) (challengeCache.size() / TRESHOLD_FOR_COMPLEXITY_INCREASE);
        final int calculatedComplexity = MINIMAL_CHALLENGE_LENGTH + complexityIncrease;
        return Math.min(MAXIMAL_CHALLENGE_LENGTH, calculatedComplexity);
    }

    @SneakyThrows
    private void sendChallenge(@NotNull final String email,
                               @NotNull final Challenge challenge) {
        final SmtpConfiguration smtp = configuration.getSmtp();
        final Email mail = new SimpleEmail();
        mail.setHostName(smtp.getHostname());
        mail.setSmtpPort(smtp.getPort());
        mail.setAuthenticator(new DefaultAuthenticator(smtp.getUser(), smtp.getPassword()));
        mail.setSSLOnConnect(smtp.getSsl());
        mail.setFrom(smtp.getFrom());
        mail.setSubject("Your challenge to login to Moodini");
        mail.setMsg(String.format("Your one time challenge, valid for 10 minutes: %s", challenge.getChallenge()));
        mail.addTo(email);
        mail.send();
    }

    public Optional<String> authorize(@NotNull final String email,
                                      @NotNull final String challengeValue) {
        Optional<String> token = Optional.empty();

        final Optional<User> user = userService.readByEmail(email);
        if (user.isPresent()) {
            final Challenge cachedChallenge = challengeCache.getIfPresent(email);
            if (cachedChallenge != null) {
                if (challengeValue.equals(cachedChallenge.getChallenge()) &&
                        cachedChallenge.getTries() < MAXIMAL_WRONG_CHALENGE_TRIES) {
                    token = Optional.of(generateToken(user.get()));
                    challengeCache.invalidate(email);
                } else {
                    cachedChallenge.increaseTries();
                }
            }
        }

        return token;
    }

    private String generateToken(@NotNull final User user) {
        final HmacSHA512Signer signer = new HmacSHA512Signer(tokenSecret);
        final JsonWebToken token = JsonWebToken.builder()
                .header(JsonWebTokenHeader.HS512())
                .claim(JsonWebTokenClaim.builder()
                        .subject(user.getUserId().toString())
                        .issuedAt(DateTime.now())
                        .expiration(DateTime.now().plusHours(12))
                        .build())
                .build();

        return signer.sign(token);
    }

}
