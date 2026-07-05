package com.taskflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainSmokeTest {
    @Test
    void mainStartsWithoutThrowing() {
        assertDoesNotThrow(() -> Main.main(new String[0]));
    }
}
