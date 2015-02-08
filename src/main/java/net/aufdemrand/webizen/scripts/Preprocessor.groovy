package net.aufdemrand.webizen.scripts

/**
 * Created by denizen_ on 1/16/15.
 */
class Preprocessor {

    public static String process(String content) {

        if (content == null) return new String()

        else return content
            .replace('<<<', 'context.response.getWriter().println("""')
            .replace('>>>', '""");')

    }

}
