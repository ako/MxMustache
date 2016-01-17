package mxmustache;

import mxmustache.template.TemplateEngineJMustache;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.fail;

/**
 * Created by ako on 1/16/2016.
 */
public class TemplateEngineJMustacheTest {
    @Before

    @Test
    public void basicTemplate() {
        try {

            String template = "{{a}}";
            TemplateEngineJMustache engine = new TemplateEngineJMustache();

            HashMap map = new HashMap();
            map.put("a", "xyz");

            String result = engine.execute(template, map, false);
            if (!result.equals("xyz")) fail();

        } catch (Exception e) {
            fail();
        }
    }
}
