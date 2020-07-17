package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;

import static org.assertj.core.api.Assertions.assertThat;

public class StringOutputTest extends AbstractTemplateOutputTest {
    @Override
    TemplateOutput createTemplateOutput() {
        return output = new StringOutput();
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}