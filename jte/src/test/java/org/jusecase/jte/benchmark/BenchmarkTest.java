package org.jusecase.jte.benchmark;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensure that the benchmark page can be rendered without error
 */
public class BenchmarkTest {
    @Test
    public void name() {
        StringOutput output = new Benchmark().render(new WelcomePage(42));

        assertThat(output.toString()).contains("This page has 42 visits already.");
    }
}
