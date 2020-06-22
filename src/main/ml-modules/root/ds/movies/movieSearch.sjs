'use strict';
const jsearch = require('/MarkLogic/jsearch');

/** Params ************/
var searchString;
/**********************/

const query = cts.andQuery([
  cts.collectionQuery('movies-json'),
  cts.wordQuery(searchString)
]);

const output =
  jsearch.documents()
    .where(query)
  .result();

output;
