package altoclefplus.core.logic;

/** A toggleable feature ticked once per client tick. */
public interface Module {
    String id();

    void tick(Snapshot snapshot);

    /** Short human-readable status for the UI, e.g. "ok" or "hook missing: camera". */
    String status();

    /** Called when the module is turned off (by the user or after an error). */
    default void onDisable() {}
}
