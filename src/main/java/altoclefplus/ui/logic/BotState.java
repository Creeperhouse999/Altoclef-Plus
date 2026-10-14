package altoclefplus.ui.logic;

public enum BotState {
    IDLE("Idle"),
    RUNNING("Running"),
    PAUSED("Paused"),
    RECOVERING("Recovering");

    public final String label;

    BotState(String label) {
        this.label = label;
    }
}
