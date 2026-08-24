# WebApp

A small Spring Boot sample application used as the System Under Test for a
Selenium/Java QA automation portfolio project.

## Running locally

```bash
mvn spring-boot:run
```

The app listens on `http://localhost:8080` and stores its data in a
SQLite file, `webapp.db`, created in the working directory on first run.
Delete that file to reset all data back to the seeded defaults.

## Seed users

All seeded users share the password `password`:

| username | Manage Users | Manage Stock | View All Orders |
|---|---|---|---|
| admin | yes | yes | yes |
| user | no | no | no |
| stockmanager | no | yes | no |
| orderviewer | no | no | yes |

## Pages

- `/login` - form login
- `/orders` - view your own orders (or all orders, if you have View All Orders) and create a new order
- `/stock` - view stock items; create/update items if you have Manage Stock
- `/users` - create users and manage their permissions (requires Manage Users)
