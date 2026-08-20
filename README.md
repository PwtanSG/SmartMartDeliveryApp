# SmartCart Delivery Mobile App

An Android application for SmartMart delivery personnel to view assigned deliveries, update delivery status, capture proof of delivery, and receive delivery notifications.

## Main Features
- Delivery-person account registration
- Secure login and logout using JWT authentication
- Dashboard showing delivery-job information
- View in-progress deliveries
- View completed deliveries
- Search for a delivery by barcode scanning its tracking barcode
- Update delivery status through the delivery workflow
- Update delivery status through ease of barcode scan
- Capture delivery photo proof and upload proof of delivery
- Receive real-time job-assignment notifications using Firebase Cloud Messaging
- Launch map directions to the recipient’s address
- Call the recipient directly from the delivery details screen
- Access an LLM and RAG-based delivery-support chatbot

## Technology Stack

- Kotlin
- Android SDK
- Spring Boot REST API
- JWT authentication
- Firebase Cloud Messaging
- AWS S3 for delivery photo storage presign url
- AWS bedrock for support chat using RAG
- Angular frontend (Merchant - packed, assign tracking no. and delivery man)

## Required Android Permissions
The application requires:
Internet access
Camera access
Location access
Notification permission

## Application Architecture

The mobile application communicates with the SmartMart Spring Boot backend using REST APIs.

1. The delivery person logs in.
2. The backend returns a JWT and user ID.
3. An OkHttp interceptor adds the JWT to protected API requests.
4. The application retrieves orders assigned to the logged-in delivery person.
5. Delivery status changes and proof-of-delivery information are sent to the backend.

## Delivery Workflow

`PACKED → PICKED_UP → DELIVERED`

- **PACKED:** The order is assigned and ready for collection.
- **PICKED_UP:** The delivery person has collected the order.
- **DELIVERED:** The order has been delivered with photo proof.

## Dashboard

The dashboard provides the delivery person with an overview of their delivery workload display in-project and completed jobs.

It displays:
- The delivery person's name.
- The number of assigned delivery jobs.
- The number of in-progress deliveries.
- The number of completed deliveries.
- Quick access to the in-progress and completed delivery lists.
- Notifications for newly assigned delivery jobs.

## In-Progress Delivery List
The in-progress delivery list displays active jobs assigned to the logged-in delivery person.

It includes orders with the following statuses:
- `PACKED` — The order has been packed and assigned to the delivery person.
- `PICKED_UP` — The order has been collected and is being delivered.

After an order is marked as `DELIVERED`, it is removed from the in-progress list and displayed in the completed delivery list.

## Completed Delivery List

The completed delivery list displays the logged-in delivery person's delivery history.

It includes orders with the `DELIVERED` status and displays information such as:

- Order ID.
- Tracking number.
- Recipient's name.
- Delivery address.
- Delivery completion date and time.
- Delivery status.
- Proof-of-delivery information.

## Map and Recipient Contact
The delivery details screen allows the delivery person to:

- View the delivery destination on a map.
- if needed,  Open the phone dialler with the recipient's contact number.

## Delivery Support Chatbot

The application includes an LLM-powered support chatbot using Retrieval-Augmented Generation (RAG).

The chatbot:

- Retrieves information from the delivery-support knowledge base.
- Uses the retrieved information as context for the LLM.
- Answers operational questions about e.g unsuccessful deliveries, unavailable recipients and damaged parcels.

## Firebase Push Notifications

Firebase Cloud Messaging (FCM) sends a real-time push notification when a merchant assigns a delivery job.
After login, the application registers its latest FCM device token with the Spring Boot backend. The backend uses this token to send job-assignment notifications to the correct delivery person.

## Prerequisites

- Android Studio
- Android SDK
- Android emulator or physical Android device
- Java 17 or the version required by the project
- Running SmartMart Spring Boot backend
- Firebase configuration file: `google-services.json`

## Application screenshoot
![SmartCart Delivery App Screenshots](https://pwt-bucket-s3.s3.us-east-1.amazonaws.com/delivery-app-screen-shoot.png)

## Application demo video
[![SmartCart Delivery App Demo](https://pwt-bucket-s3.s3.us-east-1.amazonaws.com/SmartCart-delivery-app.png)](https://youtu.be/anqRexRswLU)

## Project Structure
app/src/main/
├── java/.../
│   ├── api/
│   ├── model/
│   ├── adapter/
│   ├── activity/
│   └── util/
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
└── AndroidManifest.xml

## Related Repositories
Web frontend: <WEB_REPOSITORY_URL>
Spring Boot backend: <BACKEND_REPOSITORY_URL>

## Main Use Cases
| Actor | Use Case | Description |
|---|---|---|
| Delivery Personnel | Register account | Creates a delivery personnel account |
| Delivery Personnel | Log in | Authenticates and receives a JWT |
| Delivery Personnel | Log out | Clears the stored JWT and returns to the login screen |
| Delivery Personnel | View dashboard | Views assigned, in-progress and completed delivery statistics |
| Delivery Personnel | View in-progress jobs | Views deliveries with `PACKED` or `PICKED_UP` status |
| Delivery Personnel | View completed jobs | Views deliveries with `DELIVERED` status |
| Delivery Personnel | Scan barcode | Scans a parcel barcode to find the corresponding delivery |
| Delivery Personnel | View map and directions | Views the recipient’s location and opens navigation directions |
| Delivery Personnel | Call recipient | Opens the phone dialler with the recipient’s contact number |
| Delivery Personnel | Pick up order | Updates the delivery status from `PACKED` to `PICKED_UP` |
| Delivery Personnel | Complete delivery | Captures photo proof and updates the status to `DELIVERED` |
| Delivery Personnel | Use support chatbot | Asks delivery-related questions using an LLM and RAG chatbot |
| Merchant | Assign delivery job | Sets the order to `PACKED`, enters a tracking number and assigns a registered delivery person |
| System | Send job notification | Sends an FCM push notification when a delivery job is assigned |

## Related Merchant Use Case (Smartcart web application - merchant - order / delivery)

### Assign Delivery Job

**Actor:** Merchant

## Merchant Order Delivery Management — Web Application
![Merchant Order Delivery Management](https://pwt-bucket-s3.s3.us-east-1.amazonaws.com/Merchant-delivery-web.png)

**Precondition:** The customer has placed and paid for an order. The order status is `PAID`.

### Main Flow

1. The merchant receives the customer's paid order through the SmartCart web application.
2. The merchant prepares and packs the order.
3. The merchant opens the delivery management page.
4. The merchant changes the order status from `PAID` to `PACKED`.
5. The merchant enters a unique tracking number.
6. The merchant selects a registered delivery person.
7. The merchant saves the delivery details.
8. The Spring Boot backend updates the order record.
9. Firebase Cloud Messaging sends a job-assignment notification.
10. The assigned delivery person receives the notification.
11. The job appears in the delivery mobile application's in-progress list.

### Postconditions

- The order status is `PACKED`.
- A unique tracking number is assigned.
- A registered delivery person is assigned.
- The delivery person receives a push notification.
- The assigned job is available in the delivery mobile application.

### Validation Rules

- Only registered delivery personnel can be selected.
- A tracking number is required when assigning a delivery person.
- The tracking number must be unique.
- The order must have `PACKED` status before it can be assigned.

### Workflow

`PAID → PACKED → Assigned to Delivery Person → FCM Notification`

## User Stories and Technology Stack

| ID | Actor | User Story | Acceptance Criteria | Technology Stack Used |
|---|---|---|---|---|
| US01 | Delivery Personnel | As a delivery person, I want to register an account so that I can access the delivery application. | The user can submit valid registration details and a delivery personnel account is created. | Kotlin, Android SDK, Retrofit, Spring Boot, Spring Security, MySQL |
| US02 | Delivery Personnel | As a delivery person, I want to log in securely so that I can access my assigned delivery jobs. | Valid credentials return a JWT and allow access to protected features. Invalid credentials are rejected. | Kotlin, Retrofit, OkHttp, JWT, Spring Security |
| US03 | Delivery Personnel | As a delivery person, I want to log out so that other people cannot access my account from the device. | The stored JWT and user information are cleared and the user returns to the login screen. | Kotlin, Android SharedPreferences, JWT |
| US04 | Delivery Personnel | As a delivery person, I want to view a dashboard so that I can quickly understand my current workload. | The dashboard displays assigned, in-progress and completed delivery information. | Kotlin, Android XML, View Binding, Retrofit, Spring Boot |
| US05 | Delivery Personnel | As a delivery person, I want to view my in-progress deliveries so that I can manage active jobs. | The list displays assigned orders with `PACKED` or `PICKED_UP` status. | Kotlin, Android ListView, Custom Adapter, Retrofit, Gson |
| US06 | Delivery Personnel | As a delivery person, I want to refresh my delivery list so that I can see the latest job information. | Pulling down refreshes the list using the latest data from the backend. | Kotlin, SwipeRefreshLayout, Retrofit, Spring Boot |
| US07 | Delivery Personnel | As a delivery person, I want to view my completed deliveries so that I can review my delivery history. | Orders with `DELIVERED` status appear in a read-only completed list. | Kotlin, Android ListView, Custom Adapter, Retrofit, MySQL |
| US08 | Delivery Personnel | As a delivery person, I want to scan a parcel barcode so that I can quickly find the corresponding delivery record. | A valid barcode opens or identifies the matching delivery. An invalid barcode displays an error. | Kotlin, CameraX or ZXing/ML Kit, Android Camera, Spring Boot |
| US09 | Delivery Personnel | As a delivery person, I want to update an order as picked up so that its delivery progress is recorded. | The status changes from `PACKED` to `PICKED_UP` and is saved in the backend. | Kotlin, Retrofit, REST API, Spring Boot, MySQL |
| US10 | Delivery Personnel | As a delivery person, I want to complete a delivery so that the order is recorded as delivered. | A `PICKED_UP` order can be changed to `DELIVERED`, and it moves to the completed list. | Kotlin, Retrofit, Spring Boot, MySQL |
| US11 | Delivery Personnel | As a delivery person, I want to capture and upload photo proof so that there is evidence of successful delivery. | The application captures a photograph, uploads it and associates its file key with the correct order. | Kotlin, Android Camera, AWS API Gateway, AWS Lambda, Amazon S3 |
| US12 | Delivery Personnel | As a delivery person, I want to view the delivery location and directions so that I can navigate to the recipient. | The destination is displayed on a map and turn-by-turn navigation can be launched. | Kotlin, Google Maps SDK, Google Maps Intent, Android Location |
| US13 | Delivery Personnel | As a delivery person, I want to call the recipient so that I can contact them about the delivery. | The phone dialler opens with the recipient's number and requires confirmation before calling. | Kotlin, Android Intent, Phone Dialler |
| US14 | Delivery Personnel | As a delivery person, I want to receive a notification when a job is assigned so that I can respond promptly. | The device receives an FCM push notification containing the new job information. | Firebase Cloud Messaging, Kotlin, Spring Boot, Firebase Admin SDK |
| US15 | Delivery Personnel | As a delivery person, I want to ask a support chatbot delivery-related questions so that I can follow the correct procedures. | The chatbot retrieves relevant approved information and returns a grounded response. | Kotlin, Retrofit, AWS API Gateway, AWS Lambda, Amazon Bedrock, RAG, S3 Vectors |
| US16 | Merchant | As a merchant, I want to assign a packed order to a registered delivery person so that the order can be delivered. | The merchant sets the status to `PACKED`, enters a unique tracking number and selects a registered delivery person. | Angular, TypeScript, Spring Boot, REST API, MySQL |
| US17 | System | As the system, I want to notify the assigned delivery person so that they know a new job is available. | After assignment, the backend sends an FCM notification to the assigned delivery person's registered device. | Spring Boot, Firebase Admin SDK, Firebase Cloud Messaging |
