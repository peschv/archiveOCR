# archiveOCR: Microfilm Search
Converts image files from an archive of microfilms (of primarily newspaper articles) into searchable text, with a front-end web application for the search function.

The aim is to find articles among 10,000+ images, prioritizing relevant filenames over accurate full article transcriptions due to Amazon Textract's limitations.

## How it works:
User uploads image to an S3 bucket. User runs Java program to run Amazon Textract on the image; this requires specifying the image filename at runtime. The image is converted into text using Textract, with the text being sent as a JSON file to a second S3 bucket. An AWS Lambda (s3-to-opensearch) automatically sends this file for indexing to Amazon OpenSearch. 

In the front-end web application, user enters a search query of >2 characters. An API Gateway connects to a second Lambda (opensearch-query) which queries the OpenSearch index for any files containing text that matches the query term. The web application displays the results, i.e. either no matching search terms found, or the filename and text contents that were saved in the JSON file. 

### AWS Lambdas
- s3-to-opensearch
- opensearch-query

### AWS S3 Buckets
- microfilms: holds raw jpeg files.
- textract-microfilms: holds json files of the extracted text.

### AWS OpenSearch
- Create domain
-- In dashboard, add S3 bucket (in this case textract-microfilms) as data source.


Ideas for future:
Create an open source version of this project using Tesseract instead of Amazon Textract; Tesseract would allow you to train on your own data, so the results may be more accurate.
