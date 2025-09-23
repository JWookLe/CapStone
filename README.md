# CapStone

Consolidated workspace containing backend, Android, and Spring MVC modules.

## Projects

- `EmotionSyncServer/`: Spring Boot backend with Python-based recommendation service. Configure secrets via `EmotionSyncServer/.env` copied from `.env.example`.
- `capstone/`: Android client application. Provide your Firebase configuration by copying `capstone/app/google-services.example.json` to `capstone/app/google-services.json`.
- `SpringProject/`: Spring MVC registration/login sample. Supply datasource credentials through environment variables as described in `SpringProject/README.md`.

Each module ships without sensitive credentials; populate the documented environment variables before running locally.
