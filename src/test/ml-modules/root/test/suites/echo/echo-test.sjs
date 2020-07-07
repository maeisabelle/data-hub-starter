'use strict';

const test = require('/test/test-helper.xqy');
const echo = require('/lib/echo.sjs');

const message = "Echo test 123";

const assertions = [
  test.assertEqual(message, echo.echo(message))
];

// return
assertions;
