package altoclefplus.core.logic.events;

/** The recovery watchdog decided the bot is stuck. kind is a StuckKind name. */
public record StuckDetected(String kind, String details) {}
