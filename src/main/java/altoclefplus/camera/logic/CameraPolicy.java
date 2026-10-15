package altoclefplus.camera.logic;

/** Decides per frame whether the view is smoothed or follows the real rotation exactly. */
public final class CameraPolicy {
    public enum Mode { SMOOTH, SNAP }

    /**
     * @param moduleEnabled      camera module switched on
     * @param botActive          a user task is running or Baritone is pathing (bot controls the view)
     * @param screenOpen         any screen is open
     * @param movedSinceLastFrame blocks moved since the previous frame (large = teleport)
     * @param dimensionChanged   dimension differs from the previous frame
     * @param combat             mob defense is active
     * @param perspectiveChanged first person / third person back / front was switched this frame
     * @param cameraOnPlayer     the camera follows the player (not spectating another entity)
     */
    public record Input(boolean moduleEnabled, boolean botActive, boolean screenOpen, double movedSinceLastFrame,
                        boolean dimensionChanged, boolean combat, boolean perspectiveChanged, boolean cameraOnPlayer) {}

    public record Decision(Mode mode, boolean combat) {}

    private CameraPolicy() {}

    public static Decision decide(Input in, float snapDistance) {
        boolean snap = !in.moduleEnabled() || !in.botActive() || in.screenOpen()
                || in.movedSinceLastFrame() > snapDistance || in.dimensionChanged()
                || in.perspectiveChanged() || !in.cameraOnPlayer();
        return new Decision(snap ? Mode.SNAP : Mode.SMOOTH, in.combat());
    }
}
