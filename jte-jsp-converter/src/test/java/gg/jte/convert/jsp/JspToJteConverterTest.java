package gg.jte.convert.jsp;

import gg.jte.convert.IoUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


class JspToJteConverterTest {
    @TempDir
    Path tempDir;

    String usecase;
    Path jspRoot;
    Path jteRoot;

    String[] notConvertedTags;

    List<Consumer<Converter>> setups = new ArrayList<>();

    @Test
    void simpleTag() {
        givenUsecase("simpleTag");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTag_nullPrefix() {
        givenSetup(c -> c.setPrefix(null));
        givenUsecase("simpleTag");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        // No npe!
    }

    @Test
    void simpleTagWithArrayParameter() {
        givenUsecase("simpleTagWithArrayParameter");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithGenericParameter() {
        givenUsecase("simpleTagWithGenericParameter");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithCommentBetweenParams() {
        givenUsecase("simpleTagWithCommentBetweenParams");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTag_kebabCase() {
        givenUsecase("simpleTag");
        Throwable throwable = catchThrowable(() -> whenJspTagIsConverted("simple.tag", "tag/not-so-simple.jte"));
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class).hasMessage("Illegal jte tag name 'tag/not-so-simple.jte'. Tag names should be camel case.");
    }

    @Test
    void simpleTagWithTwoOutputsAfterEachOther() {
        givenUsecase("simpleTagWithTwoOutputsAfterEachOther");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithIfStatement() {
        givenUsecase("simpleTagWithIfStatement");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithChooseStatement() {
        givenUsecase("simpleTagWithChooseStatement");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithVariable() {
        givenUsecase("simpleTagWithVariable");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithForEach() {
        givenUsecase("simpleTagWithForEach");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithForEachAndVarStatus() {
        givenUsecase("simpleTagWithForEachAndVarStatus");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithFormatMessage() {
        givenUsecase("simpleTagWithFormatMessage");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithUsages() {
        givenUsecase("simpleTagWithUsages");
        whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleJspWithAlreadyConvertedTag() {
        givenUsecase("simpleJspWithAlreadyConvertedTag");
        whenJspTagIsConverted("usage.jsp", "usage.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void jspWithAlreadyConvertedTagParametersOnSeparateLines() {
        givenUsecase("jspWithAlreadyConvertedTagParametersOnSeparateLines");
        whenJspTagIsConverted("usage.jsp", "usage.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithSetProperty() {
        givenUsecase("simpleTagWithSetProperty");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithNotYetConvertedTag() {
        givenUsecase("simpleTagWithNotYetConvertedTag");
        Throwable throwable = catchThrowable(() -> whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte"));
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class).hasMessage("The tag <my:simple-dependency/> is used by this tag and not converted to jte yet. You should convert <my:simple-dependency/> first. If this is a tag that should be always converted by hand, implement getNotConvertedTags() and add it there.");
    }

    @Test
    void simpleTagWithNotYetConvertedTags() {
        givenUsecase("simpleTagWithNotYetConvertedTags");
        Throwable throwable = catchThrowable(() -> whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte"));
        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class).hasMessage(
              "The tag <my:simple-dependency1/> is used by this tag and not converted to jte yet. You should convert <my:simple-dependency1/> first. If this is a tag that should be always converted by hand, implement getNotConvertedTags() and add it there.\n" +
              "The tag <my:simple-dependency2/> is used by this tag and not converted to jte yet. You should convert <my:simple-dependency2/> first. If this is a tag that should be always converted by hand, implement getNotConvertedTags() and add it there."
        );
    }

    @Test
    void simpleTagWithNotYetConvertedTag_allowed() {
        notConvertedTags = new String[]{"my:simple-dependency"};

        givenUsecase("simpleTagWithNotYetConvertedTag");
        whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithInclude_fails() {
        givenUsecase("simpleTagWithInclude");

        Throwable throwable = catchThrowable(() -> whenJspTagIsConverted("simple.tag", "tag/simple.jte"));

        assertThat(throwable).isInstanceOf(UnsupportedOperationException.class).hasMessage("Includes are not supported. You should convert it to a tag first, or suppress with addInlinedInclude(\"/WEB-INF/import.jsp.inc\")");
    }

    @Test
    void simpleTagWithInclude_inlined() {
        givenSetup(c -> c.addInlinedInclude("/WEB-INF/import.jsp.inc"));
        givenUsecase("simpleTagWithInclude");

        whenJspTagIsConverted("simple.tag", "tag/simple.jte");

        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithManualConversion() {
        notConvertedTags = new String[]{"fmt:formatDate"};
        givenUsecase("simpleTagWithManualConversion");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithBody1() {
        givenUsecase("simpleTagWithBody1");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithBody2() {
        givenUsecase("simpleTagWithBody2");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithBody3() {
        givenUsecase("simpleTagWithBody3");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithBodyUsages() {
        givenUsecase("simpleTagWithBodyUsages");
        whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithComment() {
        givenUsecase("simpleTagWithComment");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    void givenUsecase(String usecase) {
        jspRoot = tempDir.resolve("jsp");
        jteRoot = tempDir.resolve("jte");

        IoUtils.copyDirectory(Paths.get("testdata", usecase, "before"), tempDir);

        this.usecase = usecase;
    }

    void givenSetup(Consumer<Converter> setup) {
        setups.add(setup);
    }

    void whenJspTagIsConverted(String jspTag, String jteTag) {
        JspToJteConverter converter = new MyConverter();
        converter.convertTag(jspTag, jteTag, c -> {
            c.setPrefix("@import static example.JteContext.*\n");
            setups.forEach(s -> s.accept(c));
        });
    }

    private void thenConversionIsAsExpected() {
        Path expected = Paths.get("testdata", usecase, "after");
        Path actual = tempDir;

        try (Stream<Path> stream = Files.walk(expected)) {
            stream.filter(p -> !Files.isDirectory(p) && !p.getFileName().toString().startsWith(".")).forEach(expectedFile -> {
                Path actualFile = actual.resolve(expected.relativize(expectedFile));
                assertThat(actualFile).exists();
                assertThat(actualFile).hasSameTextualContentAs(expectedFile);
            });
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    private class MyConverter extends JspToJteConverter {

        public MyConverter() {
            super(jspRoot, jteRoot, "my:jte");
        }

        @Override
        protected String[] getNotConvertedTags() {
            return notConvertedTags;
        }
    }
}
