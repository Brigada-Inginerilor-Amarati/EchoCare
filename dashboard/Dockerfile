# dev: the stage compose.yaml runs; src/ is synced in by `docker compose watch`.
FROM node:26-alpine AS dev
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
EXPOSE 5173
CMD ["npx", "vp", "dev", "--host", "0.0.0.0", "--port", "5173"]
