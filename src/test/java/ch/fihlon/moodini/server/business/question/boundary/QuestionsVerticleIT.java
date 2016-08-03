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
package ch.fihlon.moodini.server.business.question.boundary;

import ch.fihlon.moodini.server.business.question.entity.Question;
import com.jayway.restassured.RestAssured;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * This is the integration test for the class {@link QuestionsVerticle}.
 */
public class QuestionsVerticleIT {

    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final int SC_OK = 200;
    private static final int SC_NO_CONTENT = 204;
    private static final int SC_NOT_FOUND = 404;
    private static final String QUESTION_VERSION = "version";
    private static final String QUESTION_ID = "questionId";
    private static final String QUESTION_TEXT = "text";
    private static final String API_ENDPOINT_ALL = "/api/questions";
    private static final String API_PREFIX_ONE = "/api/questions/";

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", DEFAULT_HTTP_PORT);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestContainsTooManyAsserts", "PMD.UseStringBufferForStringAppends"})
    public void crudTest() {
        String questionText = "Do you like testing? " + LocalDateTime.now();

        // create
        Question question = given()
                .body(createJSON(questionText, null))
                .request()
                .post(API_ENDPOINT_ALL)
                .thenReturn()
                .as(Question.class);
        assertQuestion(question, questionText);

        // read all
        get(API_ENDPOINT_ALL).then()
                .assertThat()
                .statusCode(SC_OK)
                .body(containsString(questionText));

        // read one
        get(API_PREFIX_ONE + question.getQuestionId()).then()
                .assertThat()
                .statusCode(SC_OK)
                .body(QUESTION_TEXT, equalTo(questionText))
                .body(QUESTION_ID, not(is(0L)))
                .body(QUESTION_VERSION, not(is(0L)));

        // update
        questionText += " (Updated)";
        question = given()
                .body(createJSON(questionText, question.getVersion()))
                .request()
                .put(API_PREFIX_ONE + question.getQuestionId())
                .thenReturn()
                .as(Question.class);
        assertQuestion(question, questionText);

        // read one
        get(API_PREFIX_ONE + question.getQuestionId()).then()
                .assertThat()
                .statusCode(SC_OK)
                .body(QUESTION_TEXT, equalTo(questionText))
                .body(QUESTION_ID, not(is(0L)))
                .body(QUESTION_VERSION, not(is(0L)));

        // delete
        delete(API_PREFIX_ONE + question.getQuestionId()).then()
                .assertThat()
                .statusCode(SC_NO_CONTENT);
        get(API_PREFIX_ONE + question.getQuestionId()).then()
                .assertThat()
                .statusCode(SC_NOT_FOUND);
    }

    private static String createJSON(@NotNull final String text,
                                     final Long version) {
        final StringBuilder json = new StringBuilder("{\"" + QUESTION_TEXT + "\":\"" + text + "\"");
        if (version != null) {
            json.append(",\"").append(QUESTION_VERSION).append("\":").append(version);
        }
        json.append('}');
        return json.toString();
    }

    private static void assertQuestion(@NotNull final Question question,
                                       @NotNull final String questionText) {
        assertThat("The text is not equal", question.getText(), is(questionText));
        assertThat("The id should not be 0", question.getQuestionId(), is(not(0)));
        assertThat("The version should not be 0", question.getVersion(), is(not(0)));
    }

}
