#!/bin/sh
set -eu
awslocal sqs create-queue --queue-name order-requests >/dev/null
awslocal sqs create-queue --queue-name order-results >/dev/null
