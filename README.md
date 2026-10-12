# Hidden Store - E-commerce website (Spring Boot + Thymeleaf + MySQL/H2)

A Spring Boot version of the PHP & MySQL e-commerce demo ("Hidden Store").
Everything shown in the demo video is included.

## Features

### Customer side
- Home page with **products in random order on every refresh**
- **Delivery brands** filter (`/?brand_id=..`) - shows *"Sorry, this brand is not available for service right now"* when a brand has no products
- **Categories** filter (`/?category_id=..`) - shows *"No stock for this category"* when empty
- **All products** page, **keyword search** (title, keywords, description, brand, category) - shows *"No results match"* when nothing is found
- **Product details** page ("View more") with 3 images, description and **related products**
- **Add to cart** (cart works for guests too), cart count + total price in the navbar
- **Cart page**: change quantity, update cart, remove item, continue shopping, checkout
- **Checkout requires login**: guest -> login page; after login (or after registering with items in cart) the user goes straight to the payment page
- **Pay Offline** -> order is saved as *Incomplete / pending*
- **User registration** (name, email, image, password, confirm password, address, contact) - passwords are **BCrypt hashed**
- **User login / logout**, "Welcome guest" / "Welcome <name>"
- **Profile**: pending orders count, pending orders, my orders, edit account, delete account
- **Confirm payment**: invoice number, amount, choose payment mode (UPI, Netbanking, Paypal, Cash on Delivery, Pay Offline) -> status becomes *Complete (Paid)*

### Admin side (`/admin`)
- Admin **register / login / logout**
- **Insert product** (title, description, keywords, category, brand, 3 images, price)
- **View products** with **total sold**, **edit** and **delete**
- **Categories**: insert, list, edit, delete (deleting a category deletes its products)
- **Brands**: insert, list, edit, delete (deleting a brand deletes its products)
- **All orders** (with delete), **all payments**, **list users** (with delete)

Demo data (8 products, brands, categories) is inserted automatically on first start.

---

## HOW TO RUN - step by step

### Step 1 - Install the requirements (one time)
1. **JDK 17 or newer** (JDK 21 is fine). Check: open a terminal and run `java -version`
2. **Apache Maven 3.6+** (skip this if you use IntelliJ IDEA / Eclipse / STS - they include Maven). Check: `mvn -v`
3. Internet connection the first time (Maven downloads libraries; the pages load Bootstrap from a CDN)

### Step 2 - Unzip the project
Unzip `hidden-store-springboot.zip`. You will get the folder `hidden-store-springboot` (it contains `pom.xml`).

### Step 3 - Run it (easiest way: built-in H2 database, nothing to install)
Open a terminal **inside the project folder** (the one that has `pom.xml`) and run:

```
mvn spring-boot:run
```

Wait until you see a line like `Started HiddenStoreApplication` .

### Step 4 - Open the website
- Store: **http://localhost:8080**
- Admin area: **http://localhost:8080/admin**

### Step 5 - Try the same flow as the video
1. Open the store, refresh a few times - products change order.
2. Click brands (Adidas, Amazon, **Flipkart = not available**) and categories (Juices, **Milk Products = no stock**).
3. Search `apple`, `mango`, `amazon`.
4. Click **View more** on a product - see images and related products.
5. Click **Add to cart** on 2 products -> open **Cart** -> change quantity -> **Update cart**.
6. Click **Checkout** -> you are sent to login -> **Register** (any name, email, password) -> you see "items in cart" message -> **Login** -> you land on the payment page.
7. Click **Pay Offline** -> profile shows "1 pending order".
8. **Pending orders / My orders** -> status *Incomplete* -> click **Confirm** -> choose **UPI** -> status becomes *Complete (Paid)* and pending orders become 0.
9. Go to **/admin** -> **Register** an admin -> **Login**.
10. Admin: **Insert Product** (add images), **View Products** (see *Total sold*), **Edit**, **Delete**, add **Categories** and **Brands**, view **All Orders**, **All Payments**, **List Users**.

### Step 3 (alternative) - Use MySQL like XAMPP in the video
1. Install XAMPP, start **Apache not needed - start only MySQL** from the XAMPP control panel (port 3306).
2. Default XAMPP login is user `root` with an **empty password** (already set in `src/main/resources/application-mysql.properties`; change it there if yours is different).
3. You do **not** need to create the database - `hidden_store` is created automatically.
4. Run with the `mysql` profile:

```
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```
5. Open http://localhost/phpmyadmin to see the tables: `users`, `admins`, `products`, `categories`, `brands`, `user_orders`, `order_items`, `payments`.

### Run from an IDE instead
- **IntelliJ IDEA**: File > Open > select the folder (or `pom.xml`) > wait for Maven import > open `HiddenStoreApplication.java` > click the green Run button. (Lombok plugin is built in to recent versions.)
- **Eclipse / STS**: File > Import > Existing Maven Projects > select the folder > Run As > Spring Boot App / Java Application. Eclipse needs Lombok installed once (run the lombok jar installer) - or just use `mvn spring-boot:run`.
- For MySQL in an IDE add the program argument/VM option: `-Dspring.profiles.active=mysql`

### Build a jar (optional)
```
mvn clean package
java -jar target/hidden-store-1.0.0.jar
```

---

## Troubleshooting
| Problem | Fix |
|---|---|
| `mvn` is not recognized | Install Maven and add it to PATH, or use an IDE |
| `release version 17 not supported` | Your JDK is older than 17. Install JDK 17+ |
| Port 8080 already in use | Change `server.port=8081` in `application.properties` |
| MySQL: `Communications link failure` | MySQL is not running - start it in XAMPP |
| MySQL: `Access denied for user root` | Put your real password in `application-mysql.properties` |
| Lombok errors in Eclipse (`getX() undefined`) | Install Lombok in Eclipse, or run with Maven from the terminal |
| Start fresh | Stop the app and delete the `data/` folder (H2) and `uploads/` folder; for MySQL drop the `hidden_store` database |

H2 database viewer (default mode only): http://localhost:8080/h2-console  - JDBC URL `jdbc:h2:file:./data/hiddenstore`, user `sa`, empty password.

## Project structure
```
hidden-store-springboot
|- pom.xml
|- src/main/java/com/hiddenstore
|  |- HiddenStoreApplication.java
|  |- config      (WebConfig, GlobalModelAdvice, DataInitializer)
|  |- model       (User, Admin, Category, Brand, Product, UserOrder, OrderItem, Payment)
|  |- repository  (Spring Data JPA repositories)
|  |- service     (CartService, FileStorageService, CleanupService)
|  |- controller  (StoreController, UserController, AdminController)
|  \- dto         (CartLine)
\- src/main/resources
   |- application.properties          (H2 - default)
   |- application-mysql.properties    (MySQL profile)
   |- static/css, static/images
   \- templates  (Thymeleaf pages: shop, user/, admin/)
```

## Notes
- Uploaded images are stored in the `uploads/` folder next to where you start the app.
- The cart is kept in the HTTP session.
- Not included (the video says these are "Category 2"): e-mail sending, PayPal integration, contact-us form, deployment.
