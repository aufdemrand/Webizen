package net.aufdemrand.webizen;


import javax.servlet.http.HttpServletRequest;

public class Util {


    public String  getPostContents(HttpServletRequest request) {

        StringBuilder content = new StringBuilder();
        try {
            String line = request.getReader().readLine();
            while (line != null) {
                content.append(line);
                line = request.getReader().readLine();
            }
        } catch (Exception e) {  return null;  }

        return content.toString();

    }


}

