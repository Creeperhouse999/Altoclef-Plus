package altoclefplus.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SettingsSearchTest {
    private static final List<SettingsModel.Row> ROWS = List.of(
            new SettingsModel.Header("Basics"),
            new SettingsModel.Toggle("Take items from chests", () -> true, v -> { }),
            new SettingsModel.Toggle("Sleep in beds", () -> true, v -> { }),
            new SettingsModel.Header("Survival"),
            new SettingsModel.Toggle("Swim like a player in deep water", () -> true, v -> { }),
            new SettingsModel.Value("Run when this many mobs", () -> 3, v -> { }, 0, 9, 1));

    @Test
    void keepsMatchingRowsUnderTheirHeading() {
        List<SettingsModel.Row> r = SettingsModel.filter(ROWS, "swim");
        assertEquals(2, r.size());
        assertEquals("Survival", ((SettingsModel.Header) r.get(0)).label());
    }

    @Test
    void ignoresCaseAndShowsEverythingWhenEmpty() {
        assertEquals(ROWS, SettingsModel.filter(ROWS, ""));
        assertEquals(3, SettingsModel.filter(ROWS, "BEDS").size() + 1, "heading + 1 match = 2");
        assertTrue(SettingsModel.filter(ROWS, "nothing like this").isEmpty());
    }
}
