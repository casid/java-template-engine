package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;
import org.xml.sax.Attributes;

import java.util.concurrent.atomic.AtomicBoolean;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspJteConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        String jteTagPath = tag.getAttribute("jte");
        Attributes attributes = tag.getAttributes();

        String pathWithoutExtension = jteTagPath.substring(0, jteTagPath.length() - 4);
        String tagCall = pathWithoutExtension.replace('/', '.');
        int tagStartPos = output.getCurrentLineCharCount();
        StringBuilder sb = new StringBuilder(tagStartPos);
        for (int i = 0; i < tagStartPos; ++i) {
            sb.append(output.getIndentationChar());
        }
        final String tagStartIndent = sb.toString(); // Used in lambda, must be final
        output.append("@").append(tagCall).append("(");

        final AtomicBoolean first = new AtomicBoolean(true); // Used in lambda, must be final
        Runnable handleParameterStart = () -> {
            if (!first.get()) {
                output.append(",");
            }
            output.newLine(tagStartIndent);
            output.indent(1);
            first.set(false);
        };

        for (int i = 0; i < attributes.getLength(); i++) {
            String localName = attributes.getLocalName(i);
            if ("jte".equals(localName)) {
                continue;
            }
            handleParameterStart.run();
            output.append(localName).append(" = ").append(convertAttributeValue(attributes.getValue(i)));
        }

        if (tag.hasBody()) {
            handleParameterStart.run();
            output.append("bodyContent = @`");
            bodyConverter.convert();
            output.append("`");
        }

        if (!first.get()) {
            output.newLine(tagStartIndent);
        }
        output.append(")");
    }
}
