package ch.fihlon.moodini;

import org.eclipse.jetty.util.component.LifeCycle;

import javax.validation.constraints.NotNull;

public class AbstractLifecycleListener implements LifeCycle.Listener {

    @Override
    public void lifeCycleStarting(@NotNull final LifeCycle event) {
    }

    @Override
    public void lifeCycleStarted(@NotNull final LifeCycle event) {
    }

    @Override
    public void lifeCycleFailure(@NotNull final LifeCycle event, @NotNull final Throwable cause) {
    }

    @Override
    public void lifeCycleStopping(@NotNull final LifeCycle event) {
    }

    @Override
    public void lifeCycleStopped(@NotNull final LifeCycle event) {
    }

}
