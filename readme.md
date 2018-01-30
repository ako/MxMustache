# MxMustache, templating for mendix

Use Mustache templates in Mendix Microflows. Includes support for [Markdown][1] to Html conversion. Based on [JMustache][2] with
extensions for formatting. For formatting it uses [PegDown][3].

## Usage

This module contains 2 java actions:

 * FillTemplate - fills a JMustache template using data from Mendix Object.
 * SendEmail - to send emails, e.g., based on text generated with mustache template.
 
### FillTemplate

FillTemplate has the following parameters:
 
 * Template - string containing a mustache template
 * Data - Mendix object containing the data you want to use in your template. You can use associations to include related objects. See examples below.
 * RunMarkdown - run the result of the template through markdown processor to generate html. Assumes the template contains markdown. See examples below.
 * NoObjectLevels - how many levels of associations need to be included from the root object.

### SendEmail

SendEmail has the following parameters:

 * To - To address
 * From - From address
 * ReplyTo - Reply to address
 * Subject - Subject of email
 * Contents - contents of the email
 * Attachment - String to be sent as attachment with the email
 * AttachmentMimeType - Mime type of the attachment, e.g. 'text/html'.
 * AttachmentFilename - Filename that you want to be used for the attachment.
 * AttachmentDocument - Mendix FileDocument object to be attached to the email.
 * SmtpHost - Host name of the smtp server to be used
 * SmtpPort - Port of the smtp server to be used
 * SmtpUsername - Username for connecting with the smtp server
 * SmtpPassword - Password for connecting with the smtp server
 * UseSsl - Use ssl for connecting to the smtp server?
 
If you want to use gmail smtp server, specify host smtp.gmail.com, port 465, ssl is true. You also need to turn on 
[less secure apps for the gmail account][11], otherwise you'll run into AuthenticationFailException.
 
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

You can find an example microflow on [Mendix model share][9]: [Test csv template multiple objects][5].

### One to many association using markdown to create an Html table

This template uses markdown to create an html table. Includes JMustache extensions to specify formatting: money, dd-MM-yyyy:

    | Brand | Model | HasTurbo | CarId | Price | Doors | Color | DateIntroduction |
    |:----- |:----- |:-------- | -----:| -----:| -----:|:----- |:---------------- |
    {{#Car_CarList}}
    | {{Brand}} | {{Model}} | {{HasTurbo}} | {{CarId}} | {{Price | money}} | {{Doors}} | {{Color}} | {{DateIntroduction | dd-MM-yyyy}} |
    {{/Car_CarList}}

The resulting html snippet can be used with the document generator to generate a pdf.

You can find an example microflow on [Mendix model share][9]: [Test markdown template multiple objects][6]. 
You can find an example with pdf generation here: [Test markdown pdf template multiple objects][10].

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

You can find an example microflow on [Mendix model share][9]: [Test json template multiple objects][7]

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

You can find an example microflow on [Mendix model share][9]: [Test xml template multiple objects][8]

## History

 * 0.1
    * Initial release
 * 0.2
    * New action to fill template using json string
    * Migrated project to Mendix 7.6
 
 [1]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
 [2]: https://github.com/samskivert/jmustache
 [3]: https://github.com/sirthias/pegdown/
 [4]: https://modelshare.mendix.com/models/152dad95-7e3e-4ad5-ac68-7a1a7a1b4360/simple-mxmustache-example
 [5]: https://modelshare.mendix.com/models/ce716c9a-7beb-42ca-9cff-bab16920a8ff/test-csv-template-multiple-objects
 [6]: https://modelshare.mendix.com/models/745ecbeb-7270-4a57-8676-6394fcfb6cf4/test-markdown-template-multiple-objects
 [7]: https://modelshare.mendix.com/models/bcd40b79-05c2-4e61-857b-f94c3eedcc6b/test-json-template-multiple-objects
 [8]: https://modelshare.mendix.com/models/6b841874-4f28-4509-ae21-123c7587263b/test-xml-template-multiple-objects
 [9]: https://modelshare.mendix.com/
 [10]: https://modelshare.mendix.com/models/5f010314-0f71-47c9-a8a7-9baef004ef3f/test-markdown-pdf-template-multiple-objects
 [11]: https://www.google.com/settings/security/lesssecureapps