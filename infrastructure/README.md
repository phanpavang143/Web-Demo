# Triển khai E-commerce Spring Boot lên AWS

Tài liệu này hướng dẫn triển khai đúng theo Terraform và mã nguồn hiện tại của
dự án. Các lệnh bên dưới dùng PowerShell trên Windows.

## 1. Kiến trúc AWS của dự án

Terraform trong thư mục `infrastructure/terraform` tạo luồng sau:

```text
Internet
   |
Public Application Load Balancer
   |
Private ECS Fargate tasks (Spring Boot, port 8080)
   |                         |
   |                         +--> CloudWatch Logs
   +--> NAT Gateway --> AWS APIs

S3: ảnh sản phẩm riêng tư
SQS: sự kiện order
EventBridge --> Lambda heartbeat --> CloudWatch metric/alarm
CloudTrail --> S3 bucket audit riêng tư
ECR: lưu Docker image
```

Các tài nguyên chính gồm VPC, Internet Gateway, hai public subnet, hai
private subnet, NAT Gateway, ECR, ECS Fargate, ALB, autoscaling ECS,
CloudWatch Logs/Metrics/Alarm, Lambda, EventBridge, CloudTrail, S3 và SQS.

Ứng dụng Java sử dụng AWS SDK trong
`src/main/java/.../configuration/AwsConfiguration.java`. Khi
`AWS_ENABLED=true`, SDK dùng AWS Default Credentials Provider Chain. Trên ECS,
quyền được cấp qua IAM task role; không cần lưu access key trong source code.

## 2. Điều kiện cần trước khi bắt đầu

Cài các công cụ sau:

- AWS CLI
- Terraform từ phiên bản 1.5 trở lên
- Docker Desktop
- JDK 25
- Git

Kiểm tra cài đặt:

```powershell
aws --version
terraform version
docker --version
java -version
```

Bạn cần một AWS account có quyền tạo VPC, IAM, ECS, ECR, ALB, S3, SQS,
CloudWatch, Lambda, EventBridge và CloudTrail. Những tài nguyên này có thể
phát sinh chi phí, đặc biệt là NAT Gateway, ALB, ECS và CloudTrail.

## 3. Đăng nhập AWS an toàn

Khuyến nghị dùng IAM Identity Center/SSO thay vì tạo access key lâu dài:

```powershell
aws configure sso
```

Khi được hỏi, chọn account, region và profile. Sau đó đăng nhập:

```powershell
aws sso login --profile my-aws
$env:AWS_PROFILE = "my-aws"
aws sts get-caller-identity
```

Lệnh cuối phải trả về đúng `Account`, `Arn` và `UserId` của tài khoản bạn muốn
dùng. Nếu dùng access key cho môi trường tạm thời, cấu hình bằng:

```powershell
aws configure --profile my-aws
$env:AWS_PROFILE = "my-aws"
```

Không commit các file chứa access key, secret key, mật khẩu hoặc token vào
repository.

## 4. Cấu hình Terraform

Đi tới thư mục Terraform:

```powershell
cd "D:\Web AWS\E-commerce-project-springBoot\infrastructure\terraform"
```

File mẫu hiện tại là `variables.tfvars.example`:

```hcl
aws_region      = "ap-southeast-1"
project_name    = "jt-spring-commerce"
vpc_cidr        = "10.20.0.0/16"
container_image = ""
database_secret_arn = ""
```

Có thể tạo file riêng để không sửa file mẫu:

```powershell
Copy-Item variables.tfvars.example variables.tfvars
```

Sửa `variables.tfvars` nếu cần. `project_name` phải phù hợp với tên tài nguyên
AWS của account/region. `container_image` để trống ở lần đầu để Terraform dùng
nginx tạm thời và tạo được ALB/ECS service.

Khởi tạo và kiểm tra cấu hình:

```powershell
terraform init
terraform fmt -check
terraform validate
terraform plan -var-file=variables.tfvars
```

Đọc kỹ plan trước khi xác nhận. Không chạy `apply` nếu region, CIDR hoặc tên
project không đúng mong muốn.

## 5. Kiểm tra database trước khi triển khai ECS

Terraform hiện tại **không tạo MySQL/RDS**. Ứng dụng mặc định trong
`src/main/resources/application.properties` dùng:

```text
jdbc:mysql://localhost:3306/ecommjava
```

ECS không thể kết nối database trên `localhost` của máy phát triển. Trước khi
đưa Spring Boot lên ECS, cần chọn một trong hai cách:

1. Tạo Amazon RDS for MySQL trong private subnet và cho phép ECS security
   group truy cập cổng `3306`.
2. Dùng MySQL/MariaDB đang chạy ở một hệ thống bên ngoài AWS, có endpoint mà
   private ECS task truy cập được.

Database cần có schema/dữ liệu từ `basedata.sql`. Với production, lưu thông tin
kết nối trong AWS Secrets Manager dưới dạng JSON. Secret phải có đúng bốn key:

```text
{
  "db_driver": "com.mysql.cj.jdbc.Driver",
  "db_url": "jdbc:mysql://<rds-endpoint>:3306/ecommjava",
  "db_username": "<database-user>",
  "db_password": "<database-password>"
}
```

Tạo secret bằng AWS CLI, thay các giá trị mẫu bằng thông tin thật:

```powershell
$secret = '{"db_driver":"com.mysql.cj.jdbc.Driver","db_url":"jdbc:mysql://<rds-endpoint>:3306/ecommjava","db_username":"<database-user>","db_password":"<database-password>"}'
aws secretsmanager create-secret `
  --name jt-spring-commerce/database `
  --secret-string $secret `
  --region ap-southeast-1
$DB_SECRET_ARN = aws secretsmanager describe-secret `
  --secret-id jt-spring-commerce/database `
  --query ARN --output text `
  --region ap-southeast-1
```

Đặt ARN trả về vào `variables.tfvars`:

```hcl
database_secret_arn = "<secret-arn>"
```

Khi `container_image` còn rỗng, secret là tùy chọn để tạo hạ tầng nginx ban
đầu. Khi deploy image Spring Boot, Terraform bắt buộc
`database_secret_arn` khác rỗng. ECS execution role chỉ được cấp quyền đọc
đúng secret ARN này.

## 6. Cho phép ALB health check

Terraform cấu hình ALB kiểm tra:

```text
/actuator/health
```

Route này đã được mở public trong `SecurityConfiguration` và phải trả HTTP 200
mà không cần đăng nhập. Nếu endpoint trả 302 về `/login`, ALB sẽ đánh dấu ECS
task là unhealthy.

## 7. Tạo hạ tầng cơ sở lần đầu

Sau khi AWS credentials, Terraform và database đã sẵn sàng:

```powershell
terraform apply -var-file=variables.tfvars
```

Nhập `yes` khi Terraform hiển thị yêu cầu xác nhận. Lần đầu này sử dụng nginx
tạm thời vì `container_image` đang rỗng.

Kiểm tra các output quan trọng:

```powershell
terraform output
terraform output -raw ecr_repository_url
terraform output -raw product_images_bucket
terraform output -raw order_events_queue_url
terraform output -raw load_balancer_url
```

## 8. Build và push Docker image lên ECR

Từ thư mục gốc project:

```powershell
cd "D:\Web AWS\E-commerce-project-springBoot"
docker build -t jt-spring-commerce:latest .
```

Đăng nhập Docker vào ECR:

```powershell
cd infrastructure\terraform
$ECR = terraform output -raw ecr_repository_url
$REGION = "ap-southeast-1"

aws ecr get-login-password --region $REGION |
  docker login --username AWS --password-stdin $ECR
```

Gắn tag và push image:

```powershell
docker tag jt-spring-commerce:latest "$ECR:latest"
docker push "$ECR:latest"
```

## 9. Cập nhật ECS chạy image Spring Boot

Chạy Terraform với image vừa push:

```powershell
terraform apply `
  -var-file=variables.tfvars `
  -var="container_image=$ECR:latest"
```

Terraform sẽ tạo task definition mới và ECS service sẽ thay thế task nginx
bằng image của ứng dụng.

Lấy địa chỉ website:

```powershell
$URL = terraform output -raw load_balancer_url
$URL
Invoke-WebRequest "$URL/actuator/health" -UseBasicParsing
```

Nếu health check trả `200`, mở `$URL` trên trình duyệt. Nếu trả `302`, kiểm tra
SecurityConfiguration. Nếu task dừng, xem log ECS/CloudWatch để kiểm tra
database, biến môi trường và quyền IAM.

## 10. Cấu hình AWS mà ứng dụng nhận được

Terraform đã truyền tự động các biến sau vào ECS:

```text
AWS_ENABLED=true
AWS_REGION=ap-southeast-1
AWS_METRICS_NAMESPACE=jt-spring-commerce
AWS_S3_PRODUCT_BUCKET=<product_images_bucket output>
AWS_SQS_ORDER_QUEUE_URL=<order_events_queue_url output>
```

Ý nghĩa:

- **S3**: lưu ảnh sản phẩm. Bucket bị khóa public; ứng dụng dùng presigned URL.
- **SQS**: gửi sự kiện order vào queue riêng tư.
- **CloudWatch**: lưu log ECS và metric ứng dụng.
- **ECR**: lưu image Docker.
- **Lambda/EventBridge**: gửi heartbeat định kỳ.
- **CloudTrail**: ghi hoạt động AWS vào bucket audit riêng tư.

IAM task role hiện cấp quyền S3 cho object ảnh và quyền gửi/nhận/xóa message
trên SQS. Không cấp quyền public cho bucket.

## 11. Kiểm tra sau triển khai

Kiểm tra ECS service:

```powershell
aws ecs list-clusters --region ap-southeast-1
aws ecs list-services `
  --cluster jt-spring-commerce `
  --region ap-southeast-1
```

Kiểm tra task đang chạy:

```powershell
aws ecs list-tasks `
  --cluster jt-spring-commerce `
  --service-name jt-spring-commerce `
  --region ap-southeast-1
```

Kiểm tra log bằng AWS Console tại:

```text
CloudWatch -> Logs -> Log groups -> /aws/jt-spring-commerce/application
```

Kiểm tra các trang ứng dụng:

```powershell
Invoke-WebRequest "$URL/actuator/health" -UseBasicParsing
Invoke-WebRequest "$URL/login" -UseBasicParsing
Invoke-WebRequest "$URL/register" -UseBasicParsing
```

Sau khi đăng nhập, kiểm tra sản phẩm, profile, giỏ hàng và upload ảnh. S3
bucket là private nên không nên kiểm tra bằng URL object public; hãy dùng URL
presigned do ứng dụng tạo.

## 12. Cập nhật phiên bản mới

Mỗi lần thay đổi mã nguồn:

```powershell
cd "D:\Web AWS\E-commerce-project-springBoot"
docker build -t jt-spring-commerce:latest .

cd infrastructure\terraform
$ECR = terraform output -raw ecr_repository_url
docker tag jt-spring-commerce:latest "$ECR:latest"
docker push "$ECR:latest"

terraform apply `
  -var-file=variables.tfvars `
  -var="container_image=$ECR:latest"
```

Nên dùng tag bất biến như `:2026-09-25-01` thay cho `:latest` trong production
để rollback dễ dàng.

## 13. Xóa tài nguyên và tránh phát sinh chi phí

Trước khi xóa, kiểm tra dữ liệu trong S3, CloudTrail và database. Sau đó:

```powershell
terraform destroy -var-file=variables.tfvars
```

Xác nhận trên AWS Console rằng ECS, ALB, NAT Gateway, ECR, S3, SQS, Lambda,
CloudTrail và CloudWatch resources đã được xử lý. Các bucket có dữ liệu hoặc
CloudTrail retention policy có thể cần dọn riêng.

## 14. Lỗi thường gặp

| Triệu chứng | Nguyên nhân và cách xử lý |
| --- | --- |
| `Unable to locate credentials` | Chạy lại `aws sso login`, kiểm tra `$env:AWS_PROFILE` và `aws sts get-caller-identity`. |
| ECS task dừng ngay sau khi chạy | Kiểm tra CloudWatch Logs; thường do thiếu database hoặc sai biến `DB_URL`. |
| ALB báo unhealthy | Kiểm tra `/actuator/health` có trả 200 không và security rule có `permitAll()` không. |
| Upload ảnh lỗi AccessDenied | Kiểm tra ECS task role và tên `AWS_S3_PRODUCT_BUCKET`. |
| Gửi order lỗi AccessDenied | Kiểm tra `AWS_SQS_ORDER_QUEUE_URL` và quyền `sqs:SendMessage`. |
| Terraform báo resource đã tồn tại | Đổi `project_name`, kiểm tra state hoặc import resource theo hướng dẫn Terraform. |
| Không truy cập được database | Kiểm tra route private subnet, NAT/RDS security group và cổng 3306. |

## 15. Tài liệu liên quan

- Cấu hình AWS mẫu: `config/aws.env.example`
- Biến Terraform mẫu: `infrastructure/terraform/variables.tfvars.example`
- Docker image: `Dockerfile`
- Cấu hình local/dev: `src/main/resources/application-dev.properties`
- Dữ liệu mẫu: `basedata.sql`
