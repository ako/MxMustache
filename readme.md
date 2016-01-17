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

The [microflow to generate a string based on the template with MxMustache][4] above looks like this:

<iframe width='100%' height='491px' frameborder='0' src='https://modelshare.mendix.com/models/152dad95-7e3e-4ad5-ac68-7a1a7a1b4360/simple-mxmustache-example?embed=true' allowfullscreen></iframe>


  
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

### Generate JSON document with one to many association

You can use templating to generate JSON strings:

    [
    {{#Car_CarList}}
    { "Brand" : "{{Brand}}",
      "Model" : "{{Model}}",
      "HasTurbo" : {{HasTurbo}},
      "CarId" : {{CarId}},
      "Price" : {{Price}},
      "Doors" : {{Doors}},
      "Color" : "{{Color}}",
      "DateIntroduction" : "{{DateIntroduction | yyyy-MM-dd''T''HH:mm:ss.SSSZ}}"
    },
    {{/Car_CarList}}
    ]

### Generate XML document with one to many association

You can also generate XML in a similar fashion:

    <Cars>
    {{#Car_CarList}}
      <Car>
       <Brand>{{Brand}}</Brand>
       <Model>{{Model}}</Model>
       <HasTurbo>{{HasTurbo}}</HasTurbo>
       <CarId>{{CarId}}</CarId>
       <Price>{{Price}}</Price>
       <Doors>{{Doors}}</Doors>
       <Color>{{Color}}</Color>
       <DateIntroduction>{{DateIntroduction | yyyy-MM-dd''T''HH:mm:ss.SSSZ}}</DateIntroduction>
      </Car>
    {{/Car_CarList}}
    </Cars>

## History

 * 0.0.1 - Initial release
 
 [1]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
 [2]: https://github.com/samskivert/jmustache
 [3]: https://github.com/sirthias/pegdown/
 [4]: https://modelshare.mendix.com/models/152dad95-7e3e-4ad5-ac68-7a1a7a1b4360/simple-mxmustache-example

