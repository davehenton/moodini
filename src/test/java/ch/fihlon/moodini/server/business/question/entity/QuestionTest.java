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
package ch.fihlon.moodini.server.business.question.entity;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This is the unit test for the class {@link Question}.
 */
public class QuestionTest {

    private static final Long QUESTION_ID = 1L;
    private static final Long VERSION = 1L;
    private static final String TEXT = "Test";

    private Question question;

    @Before
    public void setUp() {
        question = Question.builder()
                .questionId(QUESTION_ID)
                .version(VERSION)
                .text(TEXT)
                .build();
    }

    @Test
    public void getQuestionId() {
        assertThat(question.getQuestionId(), is(QUESTION_ID));
    }

    @Test
    public void getVersion() {
        assertThat(question.getVersion(), is(VERSION));
    }

    @Test
    public void getQuestion() {
        assertThat(question.getText(), is(TEXT));
    }

}
