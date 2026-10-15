package altoclefplus.zones.logic;

/** What the bot may not do inside a zone. Defaults suit a house or a petting zoo. */
public final class ZoneRules {
    public boolean noBreak = true;
    public boolean noPlace = true;
    public boolean noLoot = true;
    public boolean noEnter = false;
    public boolean noAttackHostile = false;
    public boolean noAttackNeutral = true;
    public boolean noAttackFriendly = true;
}
