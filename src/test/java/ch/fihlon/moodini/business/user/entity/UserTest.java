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
package ch.fihlon.moodini.business.user.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = User.builder()
                .userId(0L)
                .version(1L)
                .firstname("Marcus")
                .lastname("Fihlon")
                .email("marcus@fihlon.ch")
                .build();
    }

    @Test
    public void getUserId() {
        // arrange

        // act
        final Long userId = user.getUserId();

        // assert
        assertThat(userId, is(0L));
    }

    @Test
    public void getVersion() {
        // arrange

        // act
        final Long version = user.getVersion();

        // assert
        assertThat(version, is(1L));
    }

    @Test
    public void getFirstname() {
        // arrange

        // act
        final String firstname = user.getFirstname();

        // assert
        assertThat(firstname, is("Marcus"));
    }

    @Test
    public void getLastname() {
        // arrange

        // act
        final String lastname = user.getLastname();

        // assert
        assertThat(lastname, is("Fihlon"));
    }

    @Test
    public void getName() {
        // arrange

        // act
        final String name = user.getName();

        // assert
        assertThat(name, is("Marcus Fihlon"));
    }

    @Test
    public void getEmail() {
        // arrange

        // act
        final String email = user.getEmail();

        // assert
        assertThat(email, is("marcus@fihlon.ch"));
    }

    @Test
    @SneakyThrows
    public void serializesToJSON() {
        // arrange
        final ObjectMapper mapper = Jackson.newObjectMapper();
        final String expected = fixture("fixtures/user.json");

        // act
        final String actual = mapper.writeValueAsString(user);

        // assert
        assertThat(actual, is(expected));
    }

    @Test
    @SneakyThrows
    public void deserializesFromJSON() {
        // arrange
        final ObjectMapper mapper = Jackson.newObjectMapper();

        // act
        final User actual = mapper.readValue(fixture("fixtures/user.json"), User.class);

        // assert
        assertThat(actual, is(user));
    }

}
