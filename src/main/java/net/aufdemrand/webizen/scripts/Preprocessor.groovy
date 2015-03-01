package net.aufdemrand.webizen.scripts

import net.aufdemrand.webizen.includes.Include
import org.apache.commons.lang.StringUtils

/**
 * Created by denizen_ on 1/16/15.
 */
class Preprocessor {

    public static String process(String content) {

        if (content == null) return new String()

        // TODO: Loosen form (ie. allow spaces/trim interior/etc.)
        for(Include i in Include.getAll())
            content = StringUtils.replace(content, '!{' + i.name + '}', i.contents)

        return content
            // Writer 'shorthand'
            .replace('<<<', 'context.response.getWriter().println("""')
            .replace('>>>', '""");')

    }

}
