# archiveOCR: Microfilm Search

|   |   |
|---|---|
<img src="archiveocr-web-app/images/screenshot-homepage.png" height="260">   |   ![Gif of web application in action](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNGN4MmppMnAwZ2oxdjQ0NGg3bGNhZnRoeWRmamRhOHMxajBqN2VoYiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/yfXNObKnh7HPBWkonH/giphy.gif) 


Converts image files from an archive of microfilms (of primarily newspaper articles) into searchable text, with a front-end web application for the search function.

The aim is to find articles among 10,000+ images, prioritizing relevant filenames over accurate full article transcriptions due to Amazon Textract's limitations.


## How It Works:

User uploads image to an S3 bucket. User runs Java program to run Amazon Textract on the image; this requires specifying the image filename at runtime. The image is converted into text using Textract, with the text being sent as a JSON file to a second S3 bucket. An AWS Lambda (s3-to-opensearch) automatically sends this file for indexing to Amazon OpenSearch. 
![General System Architecture](archiveOCR-gen-sys-arch.png)


In the front-end web application, user enters a search query of >2 characters. An API Gateway connects to a second Lambda (opensearch-query) which queries the OpenSearch index for any files containing text that matches the query term. The web application displays the results, i.e. either no matching search terms found, or the filename and text contents that were saved in the JSON file. 

![Web Application Architecture](archiveOCR-app-arch.png)

The images below show screenshots of the web application in action.

| Search in progress  | No result  | Successful search  |
|---|---|---|
![archiveOCR screenshot](archiveocr-web-app/images/screenshot-getting-result.png) | ![archiveOCR screenshot](archiveocr-web-app/images/screenshot-no-result.png) | ![archiveOCR screenshot](archiveocr-web-app/images/screenshot-search-result.png)

## AWS Services

- Lambda
    - "s3-to-opensearch": set 2 triggers, 1) API Gateway, specifically the "opensearch-indexing" REST API, 2) S3 bucket "textract-microfilms" for Object Created event.
    - "opensearch-query": set 1 trigger for API Gateway "opensearch-query" REST API.  
- AWS S3
    - "microfilms" bucket: holds raw jpeg files.
    - "textract-microfilms" bucket: holds json files of the extracted text.
- Amazon OpenSearch Service
    - Create own domain with following configuration:
       - Cluster config: 1 AZ (sufficient for purposes of testing), t3.small.search instance, public network access (IPv4, dual stack).
       - Security config: no fine-grained access control, create access policy restricting access to this OpenSearch domain by IP address (i.e. for resource "awn:aws:es:us-east-1:{account_no}:domain/my-domain/*", add Condition -> IpAddress -> aws:SourceIp for my own IP address to access this resource).
    - In dashboard for this OpenSearch domain, add S3 bucket ("textract-microfilms") as data source.
- IAM
    - User Groups: "programmers" with PowerUserAccess policy to allow access to AWS Services via the AWS Toolkit plugin in IntelliJ IDE.
    - Users: "power-user", belongs to "programmers" group.
    - Roles: "s3-to-opensearch-role" (attached AWSLambdaS3ExecutionRole policy to allow s3:GetObject, and AWSLambdaElasticsearchExecutionRole policy to allow es:ESHttpPost and es:ESHttpGet), "opensearch-query-role" (attached the AmazonOpenSearchServiceReadOnlyAccess policy, and AWSLambdaElasticsearchExecutionRole policy)
- API Gateway
    - "opensearch-indexing": deploy API with "ANY" method, return response body as content type application/json.
    - "opensearch-query": deploy API with a "GET" method type, require URL query string parameter ("q"), return response body as content type application/json.
- Other tools used: CloudWatch (for troubleshooting lambdas), Billing and Cost Management (to set monthly budget amount and alerts for three cost thresholds)


### Limitations

Two key limitations are accuracy and cost. Textract has some difficulty accurately extracting text from these images, which are all images of old newspapers on microfilm and often have blurred letters, bleedthrough, and darkened areas that may be challenging for OCR tech. OpenSearch Service is also a costly tool, making it less viable for daily use and/or for uploading all 10,000+ images and periodically conducting searches on this large collection. 

Future Plans:
Create an open source version of this project using Tesseract instead of Amazon Textract, and self-host the project to reduce costs. Tesseract would allow you to train on your own data, so the results may be more accurate given that all 10,000+ images are very similar in quality and appearance.

