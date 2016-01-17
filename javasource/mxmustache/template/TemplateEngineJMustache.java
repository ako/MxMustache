package mxmustache.template;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ako on 1/2/2016.
 */
public class TemplateEngineJMustache {
    private static ILogNode logger = Core.getLogger(TemplateEngineJMustache.class.getName());
    public static final String UTF8 = "UTF-8";

    public String execute(String template, Object data, Boolean runMarkdown) throws IOException {
        logger.info("template: " + template);
        logger.info("templateData: " + data);
        Template tmpl = Mustache.compiler().
                withFormatter(new Mustache.Formatter() {
                    public String format(Object value) {
                        logger.info("formatting object typed: " + value.getClass().getName());
                        if (value instanceof Date) {
                            return _fmt.format((Date) value);
                        } else if (value instanceof BigDecimal) {
                            return NumberFormat.getInstance().format(value);
                        } else {
                            return String.valueOf(value);
                        }
                    }

                    public String format(Object value, String specifier) {
                        logger.info("formatting object typed: " + value.getClass().getName() + ", specifier: " + specifier);
                        if (value instanceof Date) {
                            DateFormat _fmt = new SimpleDateFormat(specifier);
                            return _fmt.format((Date) value);
                        } else if (value instanceof BigDecimal && specifier.equals("money")) {
                            return NumberFormat.getCurrencyInstance().format((BigDecimal) value);
                        } else if (value instanceof BigDecimal) {
                            return NumberFormat.getInstance().format(value);
                        } else {
                            return String.valueOf(value);
                        }
                    }

                    protected DateFormat _fmt = new SimpleDateFormat("yyyy/MM/dd");

                }).
                nullValue("<null>").
                compile(template);
        String resultMd = tmpl.execute(data);
        String result = null;
        // run through pegdown for markdown to html conversion
        if (runMarkdown) {
            PegDownProcessor pegdown = new PegDownProcessor(Extensions.ALL);
            String resultHtml = pegdown.markdownToHtml(resultMd);
            result = resultHtml;
        } else {
            result = resultMd;
        }
        return result;
    }
}
