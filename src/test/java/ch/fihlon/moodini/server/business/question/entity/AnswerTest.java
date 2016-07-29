package ch.fihlon.moodini.server.business.question.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnswerTest {

    @Test
    public void getAnswer() {
        assertThat(Answer.AMPED.getAnswer(), is("Amped"));
        assertThat(Answer.GOOD.getAnswer(), is("Good"));
        assertThat(Answer.FINE.getAnswer(), is("Fine"));
        assertThat(Answer.MEH.getAnswer(), is("Meh"));
        assertThat(Answer.PISSED.getAnswer(), is("Pissed"));
    }

}