# archiveOCR: Microfilm Search
Converts image files from an archive of microfilms into searchable text, with a front-end web application for the search function.

## How it works:


### AWS Lambdas
- s3-to-opensearch
- opensearch-query

### AWS S3 Buckets
- microfilms: holds raw jpeg files.
- textract-microfilms: holds json files of the extracted text.

### AWS OpenSearch
- Create domain
-- In dashboard, add S3 bucket (in this case textract-microfilms) as data source.
