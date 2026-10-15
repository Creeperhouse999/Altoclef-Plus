package altoclefplus.ui.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetCommandTest {
    @Test
    void parsesSimpleGetCommands() {
        assertEquals(List.of(new GetCommand.Want("iron_boots", 1)), GetCommand.parse("get iron_boots 1"));
        assertEquals(List.of(new GetCommand.Want("dirt", 1)), GetCommand.parse("get dirt"));
        assertEquals(List.of(new GetCommand.Want("iron_sword", 1), new GetCommand.Want("shield", 2)),
                GetCommand.parse("get iron_sword 1 shield 2"));
    }

    @Test
    void anythingElseIsNotJudged() {
        assertNull(GetCommand.parse("get [diamond 3, stick 2]"), "AltoClef list syntax: never crash, just don't judge");
        assertNull(GetCommand.parse("get Iron Boots!"));
        assertNull(GetCommand.parse("gamer"));
        assertNull(GetCommand.parse(""));
    }
}
