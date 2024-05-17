# Code adapted from AWS Documentation 
# Source: https://docs.aws.amazon.com/opensearch-service/latest/developerguide/integrations.html
import boto3
import json
import re
import requests
from requests_aws4auth import AWS4Auth

region = 'us-east-1'
service = 'es'
credentials = boto3.Session().get_credentials()
awsauth = AWS4Auth(credentials.access_key, credentials.secret_key, region, service, session_token=credentials.token)

host = 'https://search-my-microfilm-domain-hlwe3hmo26dwnnquuu2ocxd2eq.aos.us-east-1.on.aws' # the OpenSearch Service domain
index = 'lambda-s3-index'
datatype = '_doc'
url = host + '/' + index + '/' + datatype

headers = { "Content-Type": "application/json" }

s3 = boto3.client('s3')

# Lambda execution starts here
def lambda_handler(event, context):

    for record in event['Records']:
        #Get bucket name and key for new file
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']
        
        #Get and read the file
        obj = s3.get_object(Bucket=bucket, Key=key)
        body = obj['Body'].read().decode('utf-8')
       
        #Convert to json
        dict_body = json.dumps(body)

        jsonbody = json.loads(json.loads(dict_body)) 

        #Index the json
        try:
            response = requests.post(url, auth=awsauth, json=jsonbody, headers=headers)\
            return response.status_code
        except Exception as e:
            print(e)
      
