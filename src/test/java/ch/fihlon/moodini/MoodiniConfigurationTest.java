package ch.fihlon.moodini;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class MoodiniConfigurationTest {

    @Test
    public void testInstantiation() {
        // arrange

        // act
        final MoodiniConfiguration moodiniConfiguration = new MoodiniConfiguration();

        // assert
        assertThat(moodiniConfiguration, notNullValue());
    }

}