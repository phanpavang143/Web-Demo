# AWS deployment

This project uses three AWS services:

## AWS deployment

Terraform provisions this Spring Boot application with the following flow:

```text
Internet -> public ALB -> private ECS Fargate tasks -> NAT Gateway -> AWS APIs
							  |
							  +-> CloudWatch Logs
EventBridge -> Lambda -> CloudWatch custom metric -> alarm
AWS API activity -> CloudTrail -> private S3 audit bucket
```

The stack includes a VPC, two public and two private subnets, Internet Gateway,
NAT Gateway, ECR, ECS Fargate, an Application Load Balancer, ECS CPU
autoscaling, a scheduled Lambda heartbeat, CloudWatch logs/metrics/alarm and
multi-region CloudTrail. S3 product images and SQS order events remain private
and are accessible to the ECS task role.

## Prerequisites

- AWS credentials configured for Terraform.
- Terraform 1.5 or newer.
- JDK 25 and Docker.

## Build and publish the application image

From the repository root:

```powershell
docker build -t jt-spring-commerce:latest .
```

Create the base infrastructure first. With an empty `container_image`, ECS
temporarily uses nginx so the load balancer can be created:

```powershell
cd infrastructure/terraform
terraform init
terraform apply -var-file=variables.tfvars.example
$ECR = terraform output -raw ecr_repository_url
aws ecr get-login-password --region ap-southeast-1 | docker login --username AWS --password-stdin $ECR
docker tag jt-spring-commerce:latest "$ECR:latest"
docker push "$ECR:latest"
```

Deploy the application image and replace the temporary task:

```powershell
terraform apply -var-file=variables.tfvars.example -var="container_image=$ECR:latest"
terraform output load_balancer_url
```

The ALB health check uses `/actuator/health`. The application exposes only
`health` and `info` through Actuator.

## Application configuration

The ECS task receives these values automatically:

```text
AWS_ENABLED=true
AWS_REGION=ap-southeast-1
AWS_S3_PRODUCT_BUCKET=<Terraform output>
AWS_SQS_ORDER_QUEUE_URL=<Terraform output>
```

The task uses its IAM role instead of static AWS keys. The S3 image bucket and
CloudTrail bucket block public access. The single NAT Gateway keeps the example
small; production deployments should consider one NAT Gateway per AZ or VPC
endpoints to reduce failure and data-transfer risk.

## Cleanup

```powershell
terraform destroy -var-file=variables.tfvars.example
```

Review CloudTrail and S3 retention requirements before destroying a production
environment.

## Provision infrastructure

```bash
cd infrastructure/terraform
terraform init
terraform plan -var-file=variables.tfvars.example
terraform apply -var-file=variables.tfvars.example
```

Configure the application with an IAM role or the AWS default credential chain:

```text
AWS_ENABLED=true
AWS_REGION=ap-southeast-1
AWS_S3_PRODUCT_BUCKET=<terraform product_images_bucket output>
AWS_SQS_ORDER_QUEUE_URL=<terraform order_events_queue_url output>
```

The S3 bucket is private. Use presigned URLs or a CloudFront distribution before exposing images publicly.