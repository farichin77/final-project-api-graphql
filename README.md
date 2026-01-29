# GraphQL API Automation Testing

[![API Automation Tests](https://github.com/farichin77/final-project-api-graphql/actions/workflows/test.yml/badge.svg)](https://github.com/farichin77/final-project-api-graphql/actions/workflows/test.yml)

Automated testing framework untuk GraphQL API menggunakan Rest Assured, TestNG, dan Extent Reports dengan integrasi Slack untuk CI/CD notifications.

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Test Reports](#test-reports)
- [CI/CD Integration](#cicd-integration)
- [Slack Notifications](#slack-notifications)
- [Test Modules](#test-modules)
- [Contributing](#contributing)

---

## 🎯 Overview

Framework automation testing untuk GraphQL API yang mencakup testing untuk berbagai modul:
- **Employee Module**: Employee & Division Management
- **Training Module**: Training, Chapter & Content Management
- **Authentication**: Login & User Management

Framework ini dilengkapi dengan:
- ✅ Extent Reports untuk visualisasi hasil test
- ✅ Data-driven testing menggunakan CSV
- ✅ Slack integration untuk CI/CD notifications
- ✅ GitHub Actions untuk automated testing
- ✅ Modular test structure untuk maintainability

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17+ | Programming Language |
| **Gradle** | 8.x | Build Tool |
| **Rest Assured** | 6.0.0 | API Testing Framework |
| **TestNG** | 7.11.0 | Testing Framework |
| **Extent Reports** | 5.1.2 | Test Reporting |
| **OkHttp** | 4.12.0 | HTTP Client (Slack) |
| **Gson** | 2.13.2 | JSON Processing |
| **Apache POI** | 5.2.5 | Excel/CSV Handling |
| **dotenv-java** | 3.2.0 | Environment Variables |

---

## 📁 Project Structure

```
final-project-api-graphql/
├── .github/
│   └── workflows/
│       └── test.yml                 # GitHub Actions CI/CD workflow
├── src/
│   ├── main/
│   │   └── resources/
│   │       └── graphql/
│   │           ├── mutations/       # GraphQL mutation queries
│   │           └── queries/         # GraphQL query queries
│   └── test/
│       ├── java/
│       │   ├── core/
│       │   │   └── BaseTest.java    # Base test class
│       │   ├── listeners/
│       │   │   └── ExtentReportListener.java  # Test listener
│       │   ├── tests/
│       │   │   ├── auth/            # Authentication tests
│       │   │   ├── employeeModule/
│       │   │   │   ├── division/    # Division tests
│       │   │   │   └── employee/    # Employee tests
│       │   │   └── trainingModule/
│       │   │       ├── chapter/     # Chapter tests
│       │   │       ├── content/     # Content tests
│       │   │       └── training/    # Training tests
│       │   └── utils/
│       │       ├── ExtentManager.java      # Report manager
│       │       ├── SlackNotifier.java      # Slack integration
│       │       ├── ReportUtils.java        # Report utilities
│       │       └── JsonHelper.java         # JSON utilities
│       └── resources/
│           ├── testng.xml           # TestNG configuration
│           └── data/                # Test data (CSV files)
├── test-output/                     # Test reports output
├── .env                             # Environment variables (gitignored)
├── .gitignore
├── build.gradle                     # Gradle dependencies
└── README.md
```

---

## ✨ Features

### 🎨 Rich Test Reporting
- **Extent Reports** dengan dark theme
- Detailed test execution logs
- Test categorization by module
- Execution time tracking
- Failed test analysis

### 📊 Data-Driven Testing
- CSV-based test data management
- Parameterized test execution
- Multiple test scenarios per test case

### 🔔 Slack Integration
- Automated notifications ke Slack channel
- Beautiful message formatting dengan Slack Block Kit
- Test results summary dengan statistics
- Failed tests details
- Execution time dan environment info

### 🚀 CI/CD Ready
- GitHub Actions workflow
- Automated test execution on push/PR
- Test artifacts upload
- Secure secrets management

### 📦 Modular Architecture
- Organized by business modules
- Reusable base classes
- Centralized utilities
- Easy to extend and maintain

---

## 📋 Prerequisites

Sebelum menjalankan project, pastikan sudah terinstall:

- **Java JDK 17** atau lebih tinggi
- **Gradle 8.x** (atau gunakan Gradle Wrapper yang sudah included)
- **Git** untuk version control
- **IDE** (IntelliJ IDEA / Eclipse / VS Code)

---

## 🚀 Installation

### 1. Clone Repository

```bash
git clone https://github.com/farichin77/final-project-api-graphql.git
cd final-project-api-graphql
```

### 2. Install Dependencies

```bash
./gradlew build
```

Atau di Windows:
```powershell
.\gradlew.bat build
```

---

## ⚙️ Configuration

### 1. Environment Variables

Buat file `.env` di root directory:

```properties
# API Configuration
BASE_URL=https://lmsb2b.do.dibimbing.id
DEBUG=true

# Authentication Credentials
EMAIL=your-email@example.com
PASSWORD=your-password
COMPANY_ID=your-company-id
```

> ⚠️ **IMPORTANT**: File `.env` sudah ada di `.gitignore`. Jangan commit credentials ke repository!

### 2. GitHub Secrets (untuk CI/CD)

Setup secrets di GitHub repository:

**Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Required secrets:
- `BASE_URL` - API base URL
- `EMAIL` - Login email
- `PASSWORD` - Login password
- `COMPANY_ID` - Company ID
- `SLACK_WEBHOOK_URL` - Slack webhook URL (optional)

---

## 🧪 Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Suite

```bash
./gradlew test --tests "tests.employeeModule.*"
```

### Run with Gradle Clean

```bash
./gradlew clean test
```

### Run Specific Test Class

```bash
./gradlew test --tests "tests.auth.LoginTest"
```

---

## 📊 Test Reports

### Extent Report

Setelah test selesai, buka report di:
```
test-output/Automation-API-Report.html
```

Report mencakup:
- ✅ Test execution summary
- ✅ Pass/Fail statistics
- ✅ Test categorization by module
- ✅ Detailed logs per test
- ✅ Execution time
- ✅ Error analysis

### Console Output

Test results juga ditampilkan di console dengan format:
```
✓ PASS: testCreateEmployee [Employee Module - Employee]
✗ FAIL: testDeleteContent [Training Module - Content] - Content not found
```

---

## 🔄 CI/CD Integration

### GitHub Actions Workflow

Workflow otomatis berjalan saat:
- Push ke branch `main`, `master`, atau `develop`
- Pull request ke branch tersebut
- Manual trigger via workflow dispatch

**Workflow Steps:**
1. ✅ Checkout code
2. ✅ Setup Java 17
3. ✅ Create `.env` from GitHub Secrets
4. ✅ Run tests
5. ✅ Upload Extent Report
6. ✅ Send Slack notification
7. ✅ Publish test results

### View Workflow Results

1. Go to repository **Actions** tab
2. Click on latest workflow run
3. View test results and logs
4. Download extent report artifact

---

## 🔔 Slack Notifications

### Setup Slack Integration

1. **Create Slack Webhook:**
   - Go to Slack App settings
   - Create Incoming Webhook
   - Copy webhook URL

2. **Add to GitHub Secrets:**
   - Repository **Settings** → **Secrets**
   - Add `SLACK_WEBHOOK_URL` secret

3. **Enable Notifications:**
   - Notifications otomatis terkirim setelah test selesai
   - Format: Rich message dengan Slack Block Kit

### Notification Format

**Success:**
```
🎉 Test Automation - PASSED
━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 Test Results Summary
   ✓ Total Tests: 22
   ✓ Passed: 22 (100%)
   ✗ Failed: 0
   ⊘ Skipped: 0

⏱️ Execution Time: 2m 34s
🌍 Environment: CI/CD
👤 Triggered by: farichin77
```

**Failure:**
```
⚠️ Test Automation - FAILED
━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 Test Results Summary
   ✓ Total Tests: 22
   ✓ Passed: 17 (77.3%)
   ✗ Failed: 5
   ⊘ Skipped: 0

❌ Failed Tests:
   • CreateChapterTest (Training Module)
   • DeleteContentTest (Training Module)
```

### Local Testing with Slack

```powershell
# Windows PowerShell
$env:SLACK_WEBHOOK_URL="your-webhook-url"
$env:SLACK_ENABLE_NOTIFICATIONS="true"
./gradlew test
```

```bash
# Linux/Mac
export SLACK_WEBHOOK_URL="your-webhook-url"
export SLACK_ENABLE_NOTIFICATIONS="true"
./gradlew test
```

---

## 📚 Test Modules

### 🔐 Authentication Module
- Login Test
- User Authentication
- Session Management

### 👥 Employee Module

#### Employee Management
- Create Employee
- Get Employee
- Update Employee
- Delete Employee
- Transfer Employee to Division

#### Division Management
- Create Division
- Get Division
- Update Division
- Delete Division

### 📖 Training Module

#### Training Management
- Create Training
- Get Training
- Update Training
- Delete Training

#### Chapter Management
- Create Chapter
- Get Chapter
- Update Chapter
- Delete Chapter

#### Content Management
- Create Content
- Get Content
- Update Content
- Delete Content

#### Assigned Employee
- Assign Employee to Training Program
- Validate Date Range

---

## 🤝 Contributing

### Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Write tests
   - Update documentation
   - Follow coding standards

3. **Run Tests Locally**
   ```bash
   ./gradlew test
   ```

4. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: your feature description"
   ```

5. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

### Coding Standards

- ✅ Follow Java naming conventions
- ✅ Add JavaDoc comments for public methods
- ✅ Write descriptive test names
- ✅ Keep test methods focused and atomic
- ✅ Use meaningful variable names
- ✅ Handle exceptions properly

---

## 📝 License

This project is created for educational purposes as part of Dibimbing.id Bootcamp Final Project.

---

## 👤 Author

**Ahmad Farichin**
- GitHub: [@farichin77](https://github.com/farichin77)
- Email: ahmadfarichin98@gamial.com

---

## 🙏 Acknowledgments

- **Dibimbing.id** - Bootcamp Platform
- **Rest Assured** - API Testing Framework
- **Extent Reports** - Test Reporting Library
- **Slack** - Notification Platform

---

## 📞 Support

Jika ada pertanyaan atau issue:
1. Check existing [Issues](https://github.com/farichin77/final-project-api-graphql/issues)
2. Create new issue dengan detail yang jelas
3. Contact via email

---

**Happy Testing! 🚀**
