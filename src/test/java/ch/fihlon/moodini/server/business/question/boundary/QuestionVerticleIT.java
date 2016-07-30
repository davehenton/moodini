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

import java.time.LocalDateTime;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class QuestionVerticleIT {

    @BeforeClass
    public static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("http.port", 8080);
    }

    @AfterClass
    public static void unconfigureRestAssured() {
        RestAssured.reset();
    }

    @Test
    public void crudTest() {
        String questionText = "Do you like testing? " + LocalDateTime.now();

        // create
        Question question = given()
                .body("{\"question\":\"" + questionText + "\"}")
                .request()
                .post("/api/questions")
                .thenReturn()
                .as(Question.class);
        assertThat(question.getQuestion(), is(questionText));
        assertThat(question.getQuestionId(), is(not(0)));
        assertThat(question.getVersion(), is(not(0)));

        // read all
        get("/api/questions").then()
                .assertThat()
                .statusCode(200)
                .body(containsString(questionText));

        // read one
        get("/api/questions/" + question.getQuestionId()).then()
                .assertThat()
                .statusCode(200)
                .body("question", equalTo(questionText))
                .body("questionId", not(is(0L)))
                .body("version", not(is(0L)));

        // update
        questionText += " (Updated)";
        question = given()
                .body("{\"question\":\"" + questionText + "\",\"version\":" + question.getVersion() + "}")
                .request()
                .put("/api/questions/" + question.getQuestionId())
                .thenReturn()
                .as(Question.class);
        assertThat(question.getQuestion(), is(questionText));
        assertThat(question.getQuestionId(), is(not(0)));
        assertThat(question.getVersion(), is(not(0)));

        // read one
        get("/api/questions/" + question.getQuestionId()).then()
                .assertThat()
                .statusCode(200)
                .body("question", equalTo(questionText))
                .body("questionId", not(is(0L)))
                .body("version", not(is(0L)));

        // delete
        delete("/api/questions/" + question.getQuestionId()).then()
                .assertThat()
                .statusCode(204);
        get("/api/questions/" + question.getQuestionId()).then()
                .assertThat()
                .statusCode(404);
    }

}
