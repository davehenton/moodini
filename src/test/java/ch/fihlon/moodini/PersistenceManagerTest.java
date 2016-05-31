package ch.fihlon.moodini;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import pl.setblack.airomem.core.SimpleController;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SimpleController.class)
public class PersistenceManagerTest {

    @Test
    public void createSimpleController() {
        // arrange
        mockStatic(SimpleController.class);
        final SimpleController<Serializable> simpleControllerMock = mock(SimpleController.class);
        when(SimpleController.loadOptional(anyObject(), anyObject())).thenReturn(simpleControllerMock);

        // act
        final SimpleController<PersistenceManagerTestClass> simpleController =
                PersistenceManager.createSimpleController(
                        PersistenceManagerTestClass.class, PersistenceManagerTestClass::new);

        // assert
        assertThat(simpleController, is(simpleControllerMock));
        verifyStatic(times(1));
        SimpleController.loadOptional(anyObject(), anyObject());
    }

}