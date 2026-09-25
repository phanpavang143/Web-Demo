import os
import time

import boto3


def handler(event, context):
    boto3.client("cloudwatch").put_metric_data(
        Namespace=os.environ["METRIC_NAMESPACE"],
        MetricData=[
            {
                "MetricName": "LambdaHeartbeat",
                "Dimensions": [{"Name": "Function", "Value": context.function_name}],
                "Timestamp": time.time(),
                "Value": 1,
                "Unit": "Count",
            }
        ],
    )
    return {"statusCode": 200, "body": "heartbeat published"}