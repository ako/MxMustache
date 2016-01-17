# MxMustache, templating for mendix

Use Mustache templates in Mendix Microflows. Includes support for [Markdown][1] to Html conversion. Based on [JMustache][2] with
extensions for formatting. For formatting it uses [PegDown][3].

## Examples

### Basic single object

Uses a single object of instance Car. Template refers to the attributes in the entity. Example can be found in the tests module:

    Brand: {{Brand}}
    Model: {{Model}}
    HasTurbo: {{HasTurbo}}
    CarId: {{CarId}}
    Price: {{Price}}
    Doors: {{Doors}}
    Color: {{Color}}
    DateIntroduction: {{DateIntroduction}}

### One to many association

Uses a CarList object with associations to 10 Cars. Result is a CSV string. 

    Brand,Model,HasTurbo,CarId,Price,Doors,Color,DateIntroduction
    {{#Car_CarList}}
    {{Brand}},{{Model}},{{HasTurbo}},{{CarId}},{{Price}},{{Doors}},{{Color}},{{DateIntroduction}}
    {{/Car_CarList}}

### One to many association using markdown to create an Html table

This template uses markdown to create an html table. Includes JMustache extensions to specify formatting: money, dd-MM-yyyy:

    | Brand | Model | HasTurbo | CarId | Price | Doors | Color | DateIntroduction |
    |:----- |:----- |:-------- | -----:| -----:| -----:|:----- |:---------------- |
    {{#Car_CarList}}
    | {{Brand}} | {{Model}} | {{HasTurbo}} | {{CarId}} | {{Price | money}} | {{Doors}} | {{Color}} | {{DateIntroduction | dd-MM-yyyy}} |
    {{/Car_CarList}}

The resulting html snippet can be used with the document generator to generate a pdf.

## History

 * 0.0.1 - Initial release
 
 [1]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
 [2]: https://github.com/samskivert/jmustache
 [3]: https://github.com/sirthias/pegdown/


