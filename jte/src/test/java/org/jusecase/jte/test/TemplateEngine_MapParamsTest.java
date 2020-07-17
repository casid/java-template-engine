package org.jusecase.jte.test;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.TemplateEngine;
import org.jusecase.jte.internal.Constants;
import org.jusecase.jte.output.StringOutput;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_MapParamsTest {
    StringOutput output = new StringOutput();
    Map<String, Object> params = new HashMap<>();
    Map<String, String> layoutDefinitions = new HashMap<>();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver);

    @Test
    public void template() {
        givenTemplate("page.jte", "@param String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTemplateIsRendered("page.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    public void tag() {
        givenTag("card", "@param String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    public void tag_defaultParamBoolean() {
        givenTag("card", "@param boolean firstParam = false\n" +
                "@param Boolean secondParam = true\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: false, two: true");
    }

    @Test
    public void tag_defaultParamInt() {
        givenTag("card", "@param int firstParam = 1\n" +
                "@param Integer secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    public void tag_defaultParamLong() {
        givenTag("card", "@param long firstParam = 1L\n" +
                "@param Long secondParam = 3L\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    public void tag_defaultParamFloat() {
        givenTag("card", "@param float firstParam = 1.0f\n" +
                "@param Float secondParam = 3.0f\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    public void tag_defaultParamDouble() {
        givenTag("card", "@param double firstParam = 1.0\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    public void tag_defaultParamTypeNeedsCast() {
        givenTag("card", "@param byte firstParam = (byte)1\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3.0");
    }

    @Test
    public void tag_defaultParamIsExpression() {
        givenTag("card", "@param int firstParam = Integer.MIN_VALUE\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: -2147483648, two: 3.0");
    }

    @Test
    public void tag_defaultParamString() {
        givenTag("card", "@param java.lang.String firstParam = \"test\"\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: test, two: 3");
    }

    @Test
    public void tag_defaultParamNull() {
        givenTag("card", "@param String firstParam = null\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: , two: 3");
    }

    @Test
    public void layout_noParamsAndDefinitions() {
        givenLayout("page", "Hello @render(content)!!");
        whenLayoutIsRendered("layout/page.jte");
        thenOutputIs("Hello !!");
    }

    @Test
    public void layout_noParamsAndOneDefinition() {
        givenLayout("page", "Hello @render(content)!!");
        layoutDefinitions.put("content", "<p>world</p>");

        whenLayoutIsRendered("layout/page.jte");

        thenOutputIs("Hello <p>world</p>!!");
    }

    @Test
    public void layout_oneParamAndTwoDefinitions() {
        givenLayout("page", "@param String name\n" +
                "Hello ${name} @render(content), @render(footer)");
        params.put("name", "jte");
        layoutDefinitions.put("content", "<p>content</p>");
        layoutDefinitions.put("footer", "<p>footer</p>");

        whenLayoutIsRendered("layout/page.jte");

        thenOutputIs("Hello jte <p>content</p>, <p>footer</p>");
    }

    @SuppressWarnings("SameParameterValue")
    private void givenTemplate(String name, String code) {
        dummyCodeResolver.givenCode(name, code);
    }

    @SuppressWarnings("SameParameterValue")
    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + Constants.TAG_EXTENSION, code);
    }

    @SuppressWarnings("SameParameterValue")
    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + Constants.LAYOUT_EXTENSION, code);
    }

    @SuppressWarnings("SameParameterValue")
    private void whenTemplateIsRendered(String name) {
        templateEngine.render(name, params, output);
    }

    @SuppressWarnings("SameParameterValue")
    private void whenTagIsRendered(String name) {
        templateEngine.renderTag(name, params, output);
    }

    @SuppressWarnings("SameParameterValue")
    private void whenLayoutIsRendered(String name) {
        templateEngine.renderLayout(name, params, layoutDefinitions, output);
    }

    private void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}
