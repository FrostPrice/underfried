package underfried.behaviors;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

public abstract class ConditionalTickerBehavior extends WakerBehaviour {
    private final long interval;

    public ConditionalTickerBehavior(Agent a, long timeout) {
        super(a, timeout);
        this.interval = timeout;
    }

    protected void onWake() {
        if (testCondition()) {
            execute();
            reset(interval);
        } else {
            reset(200);
        }
    }

    abstract protected boolean testCondition();

    abstract protected void execute();
}
