package org.jusecase.jte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.output.StringOutput;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngineTest {
    String templateName = "test/template.jte";
    Path root = Path.of("kte");

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = new TemplateEngine(dummyCodeResolver, root);
    Model model = new Model();

    @BeforeEach
    void setUp() throws Exception {
        if (Files.exists(root)) {
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        model.hello = "Hello";
        model.x = 42;
    }

    @Test
    void helloWorld() {
        givenTemplate("${model.hello} World");
        thenOutputIs("Hello World");
    }

    @Test
    void helloWorld_lineBreak() {
        givenTemplate("${model.hello}\nWorld");
        thenOutputIs("Hello\nWorld");
    }

    @Test
    void helloWorldMethod() {
        givenTemplate("${model.hello} ${model.getAnotherWorld()}");
        thenOutputIs("Hello Another World");
    }

    @Test
    void helloWorldMethod42() {
        givenTemplate("${model.hello} ${model.getAnotherWorld()} ${model.x}");
        thenOutputIs("Hello Another World 42");
    }

    @Test
    void helloLength() {
        givenTemplate("'${model.hello}' has length: ${model.hello.length}");
        thenOutputIs("'Hello' has length: 5");
    }

    @Test
    void simpleTemplateName() {
        templateName = "Simple";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void simpleTemplateNameWithExtension() {
        templateName = "Simple.txt";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void addition() {
        givenTemplate("${model.x + 1}");
        thenOutputIs("43");
    }

    @Test
    void addition2() {
        givenTemplate("${model.x + model.x}");
        thenOutputIs("84");
    }

    @Test
    void condition() {
        givenTemplate("@if (model.x == 42)Bingo@endif${model.x}");
        thenOutputIs("Bingo42");
        model.x = 12;
        thenOutputIs("12");
    }

    @Test
    void condition_else() {
        givenTemplate("@if (model.x == 42)" +
                            "Bingo" +
                       "@else" +
                            "Bongo" +
                       "@endif");

        model.x = 42;
        thenOutputIs("Bingo");
        model.x = 12;
        thenOutputIs("Bongo");
    }

    @Test
    void condition_elseif() {
        givenTemplate("@if (model.x == 42)" +
                            "Bingo" +
                       "@elseif (model.x == 43)" +
                            "Bongo" +
                       "@endif!");

        model.x = 42;
        thenOutputIs("Bingo!");
        model.x = 43;
        thenOutputIs("Bongo!");
        model.x = 44;
        thenOutputIs("!");
    }

    @Test
    void conditionBraces() {
        givenTemplate("@if ((model.x == 42) && (model.x > 0))Bingo@endif${model.x}");
        thenOutputIs("Bingo42");
        model.x = 12;
        thenOutputIs("12");
    }

    @Test
    void conditionNested() {
        givenTemplate("@if (model.x < 100)Bingo@if (model.x == 42)Bongo@endif@endif${model.x}");
        thenOutputIs("BingoBongo42");
        model.x = 12;
        thenOutputIs("Bingo12");
    }

    @Test
    void loop() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (i in model.array)" +
                        "${i}" +
                      "@endfor");
        thenOutputIs("123");
    }

    @Test
    void loopWithCondition() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (i in model.array)" +
                        "@if (i > 1)" +
                            "${i}" +
                        "@endif" +
                      "@endfor");
        thenOutputIs("23");
    }

    @Test
    void classicLoop() {
        model.array = new int[]{10, 20, 30};
        givenTemplate("@for (i in model.array.indices)" +
                        "Index ${i} is ${model.array[i]}" +
                        "@if (i < model.array.size - 1)" +
                            "<br>" +
                        "@endif" +
                      "@endfor");
        thenOutputIs("Index 0 is 10<br>Index 1 is 20<br>Index 2 is 30");
    }

    @Test
    void statement() {
        givenTemplate("!{model.x = 12}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void variable() {
        givenTemplate("!{val y:Int = 50}${y}");
        thenOutputIs("50");
    }

    @Test
    void braceInJavaString() {
        model.hello = ":-)";
        givenTemplate("@if(\":-)\".equals(model.hello))this is a smiley@endif");
        thenOutputIs("this is a smiley");
    }

    @Test
    void braceInJavaStringWithEscapedQuote() {
        model.hello = "\":-)";
        givenTemplate("@if(\"\\\":-)\".equals(model.hello))this is a smiley@endif");
        thenOutputIs("this is a smiley");
    }

    @Test
    void tag() {
        givenTag("card", "@param firstParam:String\n" +
                         "@param secondParam:Int\n" +
                         "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@tag.card(model.hello, model.x), That was a tag!");
        thenOutputIs("One: Hello, two: 42, That was a tag!");
    }

    @Test
    void tagWithMethodCallForParam() {
        givenTag("card", "@param firstParam:String\n" +
                "@param secondParam:Int\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@tag.card(model.anotherWorld, model.x), That was a tag!");
        thenOutputIs("One: Another World, two: 42, That was a tag!");
    }

    @Test
    void tagInTag() {
        givenTag("divTwo", "@param amount:Int\n" +
                         "Divided by two is ${amount / 2}!");
        givenTag("card", "@param firstParam:String\n" +
                "@param secondParam:Int\n" +
                "${firstParam}, @tag.divTwo(secondParam)");
        givenTemplate("@tag.card (model.hello, model.x) That was a tag in a tag!");
        thenOutputIs("Hello, Divided by two is 21! That was a tag in a tag!");
    }

    @Test
    void sameTagReused() {
        givenTag("divTwo", "@param amount:Int\n" +
                "${amount / 2}!");
        givenTemplate("@tag.divTwo(model.x),@tag.divTwo(2 * model.x)");
        thenOutputIs("21!,42!");
    }

    @Test
    void tagRecursion() {
        givenTag("recursion", "@param amount:Int\n" +
                "${amount}" +
                "@if (amount > 0)" +
                    "@tag.recursion(amount - 1)" +
                "@endif"
        );
        givenTemplate("@tag.recursion(5)");
        thenOutputIs("543210");
    }

    @Test
    void tagWithoutParams() {
        givenTag("basic", "I do nothing!");
        givenTemplate("@tag.basic()");
        thenOutputIs("I do nothing!");
    }

    @Test
    void tagWithPackage() {
        givenTag("my/basic", "I have a custom package");
        givenTemplate("@tag.my.basic()");
        thenOutputIs("I have a custom package");
    }

    @Test
    void tagWithNamedParam() {
        givenTag("named", "@param one:Int\n" +
                "@param two:Int\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(two = 2, one = 1)");
        thenOutputIs("1, 2");
    }

    @Test
    void tagWithNamedParamString() {
        givenTag("named", "@param one:Int\n" +
                "@param two:Int\n" +
                "@param three:String\n" +
                "${one}, ${two}, ${three}");
        givenTemplate("@tag.named(\n" +
                "two = 2,\n" +
                "three = \"Hello, there ;-)\",\n" +
                "one = 1)");
        thenOutputIs("1, 2, Hello, there ;-)");
    }

    @Test
    void tagWithDefaultParam() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named()");

        thenOutputIs("1, 2");
    }

    @Test
    void tagWithDefaultParam_firstSet() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(one = 6)");

        thenOutputIs("6, 2");
    }

    @Test
    void tagWithDefaultParam_secondSet() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(two = 5)");

        thenOutputIs("1, 5");
    }

    @Test
    void hotReload() throws Exception {
        givenTemplate("${model.hello} World");
        thenOutputIs("Hello World");

        templateEngine.invalidate(templateName);
        setUp(); // Rather hacky :-D

        givenTemplate("${model.hello}");
        thenOutputIs("Hello");
    }

    @Test
    void comment() {
        givenTemplate("<%--This is a comment" +
                " ${model.hello} everything in here is omitted--%>" +
                "This is visible...");
        thenOutputIs("This is visible...");
    }

    @Test
    void layout() {
        givenLayout("main", "@param model:org.jusecase.jte.TemplateEngineTest.Model\n" +
                "\n" +
                "<body>\n" +
                "<b>Welcome to my site - you are on page ${model.x}</b>\n" +
                "\n" +
                "<div class=\"content\">\n" +
                "    @render(content)\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    @render(footer)\n" +
                "</div>\n" +
                "</body>");

        givenTemplate("@layout.main(model)\n" +
                "    @define(content)\n" +
                "        ${model.hello}, enjoy this great content\n" +
                "    @enddefine\n" +
                "    @define(footer)\n" +
                "        Come again!\n" +
                "    @enddefine\n" +
                "@endlayout");

        thenOutputIs("\n" +
                "<body>\n" +
                "<b>Welcome to my site - you are on page 42</b>\n" +
                "\n" +
                "<div class=\"content\">\n" +
                "    \n" +
                "        Hello, enjoy this great content\n" +
                "    \n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    \n" +
                "        Come again!\n" +
                "    \n" +
                "</div>\n" +
                "</body>");
    }

    @Test
    void nestedLayouts() {
        givenLayout("main",
                "<header>@render(header)</header>" +
                "<content>@render(content)</content>" +
                "<footer>@render(footer)</footer>");
        givenLayout("mainExtended", "@layout.main()" +
                "@define(content)" +
                "@render(contentPrefix)" +
                "<b>@render(content)</b>" +
                "@render(contentSuffix)" +
                "@enddefine" +
                "@endlayout");
        givenTemplate("@layout.mainExtended()" +
                "@define(header)" +
                "this is the header" +
                "@enddefine" +
                "@define(contentPrefix)" +
                "<content-prefix>" +
                "@enddefine" +
                "@define(content)" +
                "this is the content" +
                "@enddefine" +
                "@define(contentSuffix)" +
                "<content-suffix>" +
                "@enddefine" +
                "@endlayout");

        thenOutputIs("<header>this is the header</header>" +
                "<content><content-prefix><b>this is the content</b><content-suffix></content>" +
                "<footer></footer>");
    }

    @Test
    void layoutWithNamedParams() {
        givenLayout("main",
                "@param status:Int = 5\n" +
                "@param duration:Int = -1\n" +
                "Hello, @render(content) your status is ${status}, the duration is ${duration}");

        givenTemplate("@layout.main()" +
                "@define(content)Sir@enddefine" +
                "@endlayout");

        thenOutputIs("Hello, Sir your status is 5, the duration is -1");
    }

    @Test
    void enumCheck() {
        givenRawTemplate(
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                "@import org.jusecase.jte.TemplateEngineTest.ModelType\n" +
                "@param model:Model\n" +
                "@if (model.type == ModelType.One)" +
                "one" +
                "@else" +
                "not one" +
                "@endif");

        model.type = ModelType.One;
        thenOutputIs("one");

        model.type = ModelType.Two;
        thenOutputIs("not one");
    }

    @Test
    void nestedJavascript() {
        givenTemplate("@if (model.isCaseA() && model.isCaseB())\n" +
                        "        <meta name=\"robots\" content=\"a, b\">\n" +
                        "        @elseif (model.isCaseB())\n" +
                        "        <meta name=\"robots\" content=\"b\">\n" +
                        "        @elseif (model.isCaseA())\n" +
                        "        <meta name=\"robots\" content=\"a\">\n" +
                        "        @endif" +
                "@if (model.x > 0)\n" +
                "        <meta name=\"description\" content=\"${model.x}\">\n" +
                "        @endif\n" +
                "\n" +
                "        <script>\n" +
                "            function readCookie(name) {");
        thenOutputIs("\n" +
                "        <meta name=\"robots\" content=\"a\">\n" +
                "        \n" +
                "        <meta name=\"description\" content=\"42\">\n" +
                "        \n" +
                "\n" +
                "        <script>\n" +
                "            function readCookie(name) {");
    }

    @Test
    void snakeCaseCanBeCompiled() {
        templateName = "snake-case.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void classPrefix() {
        templateName = "test/404.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void escaping() {
        givenTemplate("\\");
        thenOutputIs("\\");
    }

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + TemplateCompiler.TAG_EXTENSION, code);
    }

    private void givenTemplate(String template) {
        template = "@param model:org.jusecase.jte.TemplateEngineTest.Model\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + TemplateCompiler.LAYOUT_EXTENSION, code);
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);

        assertThat(output.toString()).isEqualTo(expected);
    }

    public static class Model {
        public String hello;
        public int x;
        public int[] array;
        public ModelType type;

        @SuppressWarnings("unused")
        public String getAnotherWorld() {
            return "Another World";
        }

        @SuppressWarnings("unused")
        public void setX(int amount) {
            x = amount;
        }

        @SuppressWarnings("unused")
        public boolean isCaseA() {
            return true;
        }

        @SuppressWarnings("unused")
        public boolean isCaseB() {
            return false;
        }
    }

    @SuppressWarnings("unused")
    public enum ModelType {
        One, Two, Three
    }
}