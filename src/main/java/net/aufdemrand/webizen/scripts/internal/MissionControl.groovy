package net.aufdemrand.webizen.scripts.internal

import net.aufdemrand.webizen.database.CouchHandler
import net.aufdemrand.webizen.scripts.Script

/**
 * Created by denizen_ on 1/16/15.
 */
class MissionControl {

    public static main(String[] args) {

        // Get a Couch
        new CouchHandler('http://68.70.176.226:5984/');

        Script missionControl = Script.get('Control');
        missionControl.hook = 'on /control/ hit'
        missionControl.contents =

            '''
import net.aufdemrand.webizen.scripts.Script

<<< <html>
    <head>
    <title> Mission Control </title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/codemirror.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/mode/groovy/groovy.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/codemirror.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/theme/monokai.css">
    <link href='http://fonts.googleapis.com/css?family=Roboto:400italic,700italic,300,700,300italic,400' rel='stylesheet' type='text/css'>

    <style>
    body {
        margin: 25px;
        font-family: Roboto;
    }
    span[entry] {
        position: relative;
        display: block;
        border: 1px solid lightgray;
        border-bottom: 3px solid lightgray;
        padding: 15px;
        margin-bottom: 3px;
    }
    span[entry]>a {
        padding-left: 15px;
        padding-right: 15px;
        float: right;
    }
    h1 {
        font-size: 160%;
        font-weight: 700;
    }
    span[process-time] {
        bottom: 25px;
        position: absolute;
        display: block;
        opacity: 0.25;
    }
    </style>
    </head>

    <body>

    <h1> Scripts </h1> <div entries>
>>>

def all_scripts = Script.getAll()

all_scripts.each {
    Script s ->
    <<< <span entry>
        ${s._id}
        <a href='script-edit?${s._id}'>   edit   </a>
        <a href='script-reload?${s._id}'> reload </a>
        <a href='script-unload?${s._id}'> unload </a>
    </span> >>>
}

<<< </div> <span process-time> ${ System.currentTimeMillis() - context.hit_time } milliseconds were spent processing this page </span> </body> </html> >>>

            '''

        missionControl.save();



        //
        // /control/script-edit
        //
        //


        Script scriptEdit = Script.get('Script Edit');
        scriptEdit.hook = 'on /control/script-edit hit'
        scriptEdit.contents =

                '''
import net.aufdemrand.webizen.scripts.Script
import org.apache.commons.lang.StringEscapeUtils

<<< <html>
    <head>
    <title> Control </title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/codemirror.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/mode/groovy/groovy.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/codemirror.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/codemirror/4.11.0/theme/monokai.css">
    <link href='http://fonts.googleapis.com/css?family=Roboto:400italic,700italic,300,700,300italic,400' rel='stylesheet' type='text/css'>

    <style>
    body {
        margin: 25px;
        font-family: Roboto;
    }
    h1 {
        font-size: 160%;
        font-weight: 700;
    }
    span[process-time] {
        bottom: 25px;
        position: absolute;
        display: block;
        opacity: 0.25;
    }
    .CodeMirror {
        height: auto;
        margin-bottom: 25px;
    }
    button {
        padding: 13px;
        font-family: Roboto;
        font-size: 100%;
        padding-left: 30px;
        padding-right: 30px;
        font-weight: 800;
    }
    </style>
    </head>

    <body>
>>>

def s = Script.get( URLDecoder.decode(context.query) )

<<< <h1> Edit ${s._id} </h1>
    <form action='script-save?${ StringEscapeUtils.escapeHtml(s._id) }' method='POST'>
    <span>Hook: <input type=text name=hook value='${ StringEscapeUtils.escapeHtml(s.hook) }'> </span>
    <span>Description: <input type=text name=description value='${ StringEscapeUtils.escapeHtml(s.description) }'> </span>
    <span> <input type=submit value='Save & Load Script'> </span>
    <textarea type=text name=contents id=editor> ${ StringEscapeUtils.escapeHtml(s.contents) } </textarea>
    <script>
    var textArea = document.getElementById('editor');
    var editor = CodeMirror.fromTextArea(textArea, {
        lineNumbers: true,
        mode: "groovy",
        theme: "monokai",
        scrollbarStyle: null
    });
    </script>
    </form>
</span> >>>

<<< </body> </html> >>>

            '''

        scriptEdit.save();


        //
        // /control/script-save
        //
        //


        Script scriptSave = Script.get('Script Save');
        scriptSave.hook = 'on /control/script-save hit'
        scriptSave.contents =

                '''
import net.aufdemrand.webizen.scripts.Script
import org.apache.commons.lang.StringEscapeUtils

<<< <html>
    <head>
    <title> Control </title>
    <link href='http://fonts.googleapis.com/css?family=Roboto:400italic,700italic,300,700,300italic,400' rel='stylesheet' type='text/css'>

    <style>
    body {
        margin: 25px;
        font-family: Roboto;
    }
    h1 {
        font-size: 160%;
        font-weight: 700;
    }
    span[process-time] {
        bottom: 25px;
        position: absolute;
        display: block;
        opacity: 0.25;
    }
    </style>
    </head>

    <body>
>>>

// Get script
def s = Script.get( URLDecoder.decode(context.query) )

<<< <h1> Processed ${s._id} </h1> >>>

// Update the script
s.description = context.request.getParameter('description')
s.contents = context.request.getParameter('contents')
s.hook = context.request.getParameter('hook')

// Save the script in the database
<<< <span process> Save output: ${ s.save() } </span> >>>

// Reload the script
<<< <span process> Load output: ${ s.load() } </span> >>>

<<< </body> </html> >>>

            '''

        scriptSave.save();

    }

}
