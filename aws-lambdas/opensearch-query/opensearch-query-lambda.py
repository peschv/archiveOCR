# Code adapted from AWS Documentation for Amazon OpenSearch Service
# Source: https://docs.aws.amazon.com/opensearch-service/latest/developerguide/search-example.html
import boto3
import json
import requests
from requests_aws4auth import AWS4Auth

region = 'us-east-1'
service = 'es'
credentials = boto3.Session().get_credentials()
awsauth = AWS4Auth(credentials.access_key, credentials.secret_key, region, service, session_token=credentials.token)

# OpenSearch domain endpoint
host = 'https://search-my-microfilm-domain-hlwe3hmo26dwnnquuu2ocxd2eq.aos.us-east-1.on.aws' 
# OpenSearch index
index = 'lambda-s3-index'
url = host + '/' + index + '/_search'


def lambda_handler(event, context):
    print('Event:', event)
    # User query
    query = {
        "size": 25,
        "query": {
            "match": {
                "article": event['queryStringParameters']['q']
            }
        }
    }

    headers = { "Content-Type": "application/json" }

    # Make the signed HTTP request
    r = requests.get(url, auth=awsauth, headers=headers, data=json.dumps(query))

    # Create the response
    response = {
        "statusCode": 200,
        "headers": {
          "Access-Control-Allow-Origin": "*", # Enable CORS
          "Access-Control-Allow-Methods": "GET, OPTIONS",
          "Content-Type": "application/json"
        },
        "isBase64Encoded": False
    }

    # Add the search results to the response
    response['body'] = r.text
    return response
