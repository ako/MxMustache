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

Please check ModelShare for an example how to use this in a microflow: [microflow to generate a string based on the template with MxMustache][4].

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

Example microflow:

<iframe width='100%' height='491px' frameborder='0' src='https://modelshare.mendix.com/models/6b841874-4f28-4509-ae21-123c7587263b/test-xml-template-multiple-objects?embed=true' allowfullscreen>Xml MxMustache example</iframe>

## History

 * 0.0.1 - Initial release
 
 [1]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
 [2]: https://github.com/samskivert/jmustache
 [3]: https://github.com/sirthias/pegdown/
 [4]: https://modelshare.mendix.com/models/152dad95-7e3e-4ad5-ac68-7a1a7a1b4360/simple-mxmustache-example
 [5]: https://modelshare.mendix.com/models/ce716c9a-7beb-42ca-9cff-bab16920a8ff/test-csv-template-multiple-objects
 [6]: https://modelshare.mendix.com/models/745ecbeb-7270-4a57-8676-6394fcfb6cf4/test-markdown-template-multiple-objects
 [7]: https://modelshare.mendix.com/models/bcd40b79-05c2-4e61-857b-f94c3eedcc6b/test-json-template-multiple-objects
 [8]: https://modelshare.mendix.com/models/6b841874-4f28-4509-ae21-123c7587263b/test-xml-template-multiple-objects
