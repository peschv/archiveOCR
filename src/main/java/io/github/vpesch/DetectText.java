/*
 * Code adapted from Amazon Textract Documentation, code sample for AWS SDK Java V2
 * https://docs.aws.amazon.com/textract/latest/dg/example_textract_DetectDocumentText_section.html
 *
 * Obtains image file from an AWS S3 bucket, extracts text using Amazon Textract,
 * and sends results as JSON file to a second S3 Bucket.
 */

package io.github.vpesch;

import org.json.JSONException;
import org.json.JSONObject;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.model.S3Object;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.TextractException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;


public class DetectText {

    public static void main(String[] args) {

        String docName = args[0]; //Image file, ex. 1842/1842-IMG_6245.JPEG
        String inputBucket = "microfilms"; //Input S3 bucket containing JPEGs
        String outputBucket = "textract-microfilms"; //Output S3 bucket containing JSONs
        Region region = Region.US_EAST_1;

        TextractClient textractClient = TextractClient.builder()
                .region(region)
                .build();

        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        //Extract raw text from image
        StringBuilder rawText = detectDocTextS3(textractClient, inputBucket, docName);
        textractClient.close();

        //Create JSON object
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("filename", docName);
            jsonObject.put("article", rawText);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        //Upload JSON object to S3 bucket
        PutObjectRequest request = PutObjectRequest.builder().
                bucket(outputBucket).
                key(modifyExtension(docName)).
                contentType("application/json").
                build();
        s3.putObject(request, RequestBody.fromString(String.valueOf(jsonObject)));

    }
    public static StringBuilder detectDocTextS3(TextractClient textractClient, String inputBucket, String docName) {
        StringBuilder rawText = new StringBuilder();
        try {
            S3Object s3Object = S3Object.builder()
                    .bucket(inputBucket)
                    .name(docName)
                    .build();

            // Create a Document object and reference the s3Object instance.
            Document myDoc = Document.builder()
                    .s3Object(s3Object)
                    .build();

            DetectDocumentTextRequest detectDocumentTextRequest = DetectDocumentTextRequest.builder()
                    .document(myDoc)
                    .build();

            DetectDocumentTextResponse textResponse = textractClient.detectDocumentText(detectDocumentTextRequest);
            for (Block block : textResponse.blocks()) {
                rawText.append(block.text()).append(" ");
            }
            System.out.println("Content of file:" + '\n' + rawText);

        } catch (TextractException e) {

            System.err.println(e.getMessage());
            System.exit(1);
        }
        return rawText;

    }

    /*
     * Replaces "JPEG" extension with "json". Necessary for passing the files
     * to OpenSearch for indexing.
     */
    public static String modifyExtension(String docName) {

        return docName.substring(0, docName.length() - 4) + "json";
    }

}
