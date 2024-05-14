// Update this variable to point to your domain.
var apigatewayendpoint = 'https://g2szfekug2.execute-api.us-east-1.amazonaws.com/beta/';
var loadingdiv = $('#loading');
var noresults = $('#noresults');
var resultdiv = $('#results');
var searchbox = $('input#search');
var timer = 0;

// Executes the search function 250 milliseconds after user stops typing
searchbox.keyup(function () {
  clearTimeout(timer);
  timer = setTimeout(search, 250);
});

async function search() {
  // Clear results before searching
  noresults.hide();
  resultdiv.empty();
  loadingdiv.show();
  // Get the query from the user
  let userquery = searchbox.val();
  // Only run a query if the string contains at least three characters
  if (userquery.length > 2) {
    // Make the HTTP request with the query as a parameter and wait for the JSON results
    let response = await $.get(apigatewayendpoint, { q: userquery, size: 25 }, 'json');
    // Get the part of the JSON response that we care about
    console.log('response' + JSON.stringify(response));
    let results = response['hits']['hits'];
    console.log('results:' + JSON.stringify(results));
    console.log('results length:' + results.length);

    if (results.length > 0) {
      loadingdiv.hide();
      let results_num = 1;
      // Iterate through the results and write them to HTML
      resultdiv.append('<p>Found ' + results.length + ' results.</p>');
      //for (var item in results) {
      for (let item = 0; item < results.length; item++) {
        let content_name = results[item]._source.filename;
        let content = results[item]._source.article;
        // Construct the full HTML string that we want to append to the div
        resultdiv.append('<div class="result">' +
        '<div><h2>Result ' + results_num + '</h2>' + '<p>' + content_name + '</p>' +
        '<p>' + content + '</p></div></div>');
        results_num++;
      }
    } else {
      noresults.show();
    }
  }
  loadingdiv.hide();
}
